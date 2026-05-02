package com.davcoding.android.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * DAVCodingAISession - Bridges the Termux terminal engine with the DAVCoding AI CLI.
 *
 * This class provides the AI interaction layer for DAVCoding Android:
 * - Streams responses from the DAVCoding API (Claude Sonnet backend)
 * - Injects AI output directly into the terminal session
 * - Maintains conversation context across terminal sessions
 * - Supports /model, /autopilot, /feedback slash commands
 *
 * Usage:
 *   DAVCodingAISession session = new DAVCodingAISession(context, terminalClient);
 *   session.sendMessage("Fix the bug in MainActivity.java");
 */
public class DAVCodingAISession {

    private static final String TAG = "DAVCodingAI";
    private static final String DAVCODING_API_BASE = "https://api.davcoding.ai/v1";
    private static final String DEFAULT_MODEL = "claude-sonnet-4-5";

    public interface AIResponseListener {
        void onToken(String token);
        void onComplete(String fullResponse);
        void onError(String error);
    }

    private final Context context;
    private final List<JSONObject> conversationHistory = new ArrayList<>();
    private String currentModel = DEFAULT_MODEL;
    private boolean autopilotMode = false;
    private String githubToken;

    public DAVCodingAISession(Context context) {
        this.context = context;
    }

    /**
     * Send a message to the DAVCoding AI agent.
     * Streams tokens back via the listener (non-blocking).
     */
    public void sendMessage(String userMessage, AIResponseListener listener) {
        new Thread(() -> {
            try {
                // Add user message to history
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", userMessage);
                conversationHistory.add(userMsg);

                // Build request body
                JSONObject body = new JSONObject();
                body.put("model", currentModel);
                body.put("max_tokens", 4096);
                body.put("stream", true);
                body.put("messages", new JSONArray(conversationHistory));

                // System prompt: DAVCoding Android context
                body.put("system", buildSystemPrompt());

                // POST to API
                URL url = new URL(DAVCODING_API_BASE + "/messages");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-api-key", getApiKey());
                conn.setRequestProperty("anthropic-version", "2023-06-01");
                conn.setDoOutput(true);

                byte[] bodyBytes = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bodyBytes);
                }

                // Stream SSE response
                StringBuilder fullResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if (data.equals("[DONE]")) break;

                            JSONObject event = new JSONObject(data);
                            String eventType = event.optString("type");

                            if ("content_block_delta".equals(eventType)) {
                                JSONObject delta = event.optJSONObject("delta");
                                if (delta != null) {
                                    String token = delta.optString("text", "");
                                    if (!token.isEmpty()) {
                                        fullResponse.append(token);
                                        new Handler(Looper.getMainLooper()).post(
                                            () -> listener.onToken(token)
                                        );
                                    }
                                }
                            }
                        }
                    }
                }

                // Add assistant response to history
                JSONObject assistantMsg = new JSONObject();
                assistantMsg.put("role", "assistant");
                assistantMsg.put("content", fullResponse.toString());
                conversationHistory.add(assistantMsg);

                String finalResponse = fullResponse.toString();
                new Handler(Looper.getMainLooper()).post(
                    () -> listener.onComplete(finalResponse)
                );

            } catch (Exception e) {
                Log.e(TAG, "AI request failed", e);
                new Handler(Looper.getMainLooper()).post(
                    () -> listener.onError(e.getMessage())
                );
            }
        }).start();
    }

    /**
     * Handle DAVCoding slash commands (/model, /autopilot, /login, /feedback)
     */
    public String handleSlashCommand(String command) {
        if (command.startsWith("/model")) {
            String[] parts = command.split(" ");
            if (parts.length > 1) {
                currentModel = parts[1];
                return "✓ Model switched to: " + currentModel;
            }
            return "Current model: " + currentModel + "\nAvailable: claude-sonnet-4-5, claude-opus-4, claude-haiku-4";
        }

        if (command.equals("/autopilot")) {
            autopilotMode = !autopilotMode;
            return autopilotMode
                ? "⚡ Autopilot ON — DAVCoding will continue until task is complete"
                : "✋ Autopilot OFF — DAVCoding will pause for approval";
        }

        if (command.startsWith("/login")) {
            return "🔐 GitHub auth: Set GH_TOKEN env variable or use:\n  export GH_TOKEN=<your-pat>";
        }

        if (command.equals("/feedback")) {
            return "📢 Submit feedback: https://github.com/cptleftnut/DAVCoding/discussions";
        }

        if (command.equals("/clear")) {
            conversationHistory.clear();
            return "🗑️ Conversation history cleared.";
        }

        if (command.equals("/help")) {
            return "DAVCoding Android — AI-powered terminal\n\n" +
                   "Commands:\n" +
                   "  /model [name]   — Switch AI model\n" +
                   "  /autopilot      — Toggle autopilot mode\n" +
                   "  /login          — GitHub authentication\n" +
                   "  /clear          — Clear conversation\n" +
                   "  /feedback       — Submit feedback\n\n" +
                   "Powered by Claude Sonnet · github.com/cptleftnut/DAVCoding";
        }

        return null; // Not a slash command
    }

    private String buildSystemPrompt() {
        return "You are DAVCoding, an AI-powered coding assistant running inside an Android terminal. " +
               "You have full access to the terminal environment via Termux. " +
               "You can read files, execute commands, debug code, and interact with GitHub. " +
               "Be concise, precise, and action-oriented. " +
               "When you need to run a command, output it on its own line prefixed with '$ '. " +
               "Current mode: " + (autopilotMode ? "AUTOPILOT" : "APPROVAL") + ". " +
               "Model: " + currentModel + ".";
    }

    private String getApiKey() {
        // Read from environment or Android SharedPreferences
        String envKey = System.getenv("DAVCODING_API_KEY");
        if (envKey != null && !envKey.isEmpty()) return envKey;
        // Fallback: read from app preferences
        return context.getSharedPreferences("davcoding", Context.MODE_PRIVATE)
                      .getString("api_key", "");
    }

    public void setGithubToken(String token) {
        this.githubToken = token;
    }

    public boolean isAutopilotEnabled() {
        return autopilotMode;
    }

    public String getCurrentModel() {
        return currentModel;
    }

    public void clearHistory() {
        conversationHistory.clear();
    }
}
