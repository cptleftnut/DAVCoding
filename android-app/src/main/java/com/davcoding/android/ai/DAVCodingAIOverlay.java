package com.davcoding.android.ai;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * DAVCodingAIOverlay — Floating AI chat panel for DAVCoding Android.
 *
 * Renders on top of the Termux terminal view. The user can:
 * - Type natural language prompts to the AI
 * - See streamed AI responses with DAVCoding branding
 * - Use slash commands (/model, /autopilot, etc.)
 * - Toggle the overlay with the DAVCoding button in the terminal toolbar
 *
 * Design language: Dark editorial aesthetic matching DAVCoding CLI splash screen.
 * Colors: #0d1117 background, #58a6ff accent, #f0f6fc primary text.
 */
public class DAVCodingAIOverlay extends LinearLayout {

    // DAVCoding color palette (matches CLI splash screen)
    private static final int COLOR_BG         = 0xFF0D1117; // GitHub dark bg
    private static final int COLOR_SURFACE     = 0xFF161B22; // Card surface
    private static final int COLOR_BORDER      = 0xFF30363D; // Subtle border
    private static final int COLOR_ACCENT      = 0xFF58A6FF; // DAVCoding blue
    private static final int COLOR_SUCCESS     = 0xFF3FB950; // Green
    private static final int COLOR_TEXT_PRI    = 0xFFF0F6FC; // Primary text
    private static final int COLOR_TEXT_SEC    = 0xFF8B949E; // Secondary text
    private static final int COLOR_AI_BUBBLE   = 0xFF1C2128; // AI response bg
    private static final int COLOR_USER_BUBBLE = 0xFF0D419D; // User bubble

    private final DAVCodingAISession aiSession;
    private TextView chatLog;
    private EditText inputField;
    private ScrollView scrollView;
    private StringBuilder chatBuffer = new StringBuilder();

    public DAVCodingAIOverlay(Context context) {
        super(context);
        this.aiSession = new DAVCodingAISession(context);
        setupUI(context);
        showWelcomeBanner();
    }

    private void setupUI(Context context) {
        setOrientation(VERTICAL);
        setBackgroundColor(COLOR_BG);
        setPadding(0, 0, 0, 0);

        // ── Header bar ──────────────────────────────────────────────────────
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(HORIZONTAL);
        header.setBackgroundColor(COLOR_SURFACE);
        header.setPadding(dp(16), dp(12), dp(16), dp(12));
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(context);
        title.setText("⬡ DAVCoding AI");
        title.setTextColor(COLOR_ACCENT);
        title.setTextSize(15f);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        TextView modelBadge = new TextView(context);
        modelBadge.setText("claude-sonnet-4-5");
        modelBadge.setTextColor(COLOR_TEXT_SEC);
        modelBadge.setTextSize(11f);
        modelBadge.setTypeface(Typeface.MONOSPACE);

        header.addView(title);
        header.addView(modelBadge);

        // Thin accent line under header
        View accentLine = new View(context);
        accentLine.setBackgroundColor(COLOR_ACCENT);
        accentLine.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp(1)));

        // ── Chat log ─────────────────────────────────────────────────────────
        scrollView = new ScrollView(context);
        scrollView.setBackgroundColor(COLOR_BG);
        scrollView.setFillViewport(true);
        LayoutParams scrollParams = new LayoutParams(
            LayoutParams.MATCH_PARENT, 0, 1f);
        scrollView.setLayoutParams(scrollParams);

        chatLog = new TextView(context);
        chatLog.setTextColor(COLOR_TEXT_PRI);
        chatLog.setTextSize(13f);
        chatLog.setTypeface(Typeface.MONOSPACE);
        chatLog.setPadding(dp(16), dp(12), dp(16), dp(12));
        chatLog.setLineSpacing(dp(2), 1.1f);
        scrollView.addView(chatLog);

        // ── Input area ───────────────────────────────────────────────────────
        LinearLayout inputRow = new LinearLayout(context);
        inputRow.setOrientation(HORIZONTAL);
        inputRow.setBackgroundColor(COLOR_SURFACE);
        inputRow.setPadding(dp(12), dp(8), dp(12), dp(8));
        inputRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView prompt = new TextView(context);
        prompt.setText("❯ ");
        prompt.setTextColor(COLOR_ACCENT);
        prompt.setTextSize(15f);
        prompt.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);

        inputField = new EditText(context);
        inputField.setHint("Ask DAVCoding anything…");
        inputField.setHintTextColor(COLOR_TEXT_SEC);
        inputField.setTextColor(COLOR_TEXT_PRI);
        inputField.setTextSize(13f);
        inputField.setTypeface(Typeface.MONOSPACE);
        inputField.setBackground(null);
        inputField.setSingleLine(true);
        inputField.setImeOptions(EditorInfo.IME_ACTION_SEND);
        inputField.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        inputField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                handleSend();
                return true;
            }
            return false;
        });

        inputRow.addView(prompt);
        inputRow.addView(inputField);

        // Assemble
        addView(header);
        addView(accentLine);
        addView(scrollView);
        addView(inputRow);
    }

    private void handleSend() {
        String message = inputField.getText().toString().trim();
        if (message.isEmpty()) return;
        inputField.setText("");

        // Check for slash commands
        if (message.startsWith("/")) {
            String result = aiSession.handleSlashCommand(message);
            if (result != null) {
                appendSystem(result);
                return;
            }
        }

        // Show user message
        appendUser(message);

        // Stream AI response
        appendAIPrefix();
        aiSession.sendMessage(message, new DAVCodingAISession.AIResponseListener() {
            @Override
            public void onToken(String token) {
                post(() -> appendToken(token));
            }

            @Override
            public void onComplete(String fullResponse) {
                post(() -> {
                    appendNewline();
                    scrollToBottom();
                });
            }

            @Override
            public void onError(String error) {
                post(() -> appendError(error));
            }
        });
    }

    private void showWelcomeBanner() {
        appendRaw(colorize("\n  ██████╗  █████╗ ██╗   ██╗\n" +
                           "  ██╔══██╗██╔══██╗██║   ██║\n" +
                           "  ██║  ██║███████║██║   ██║\n" +
                           "  ██║  ██║██╔══██║╚██╗ ██╔╝\n" +
                           "  ██████╔╝██║  ██║ ╚████╔╝\n" +
                           "  ╚═════╝ ╚═╝  ╚═╝  ╚═══╝  ", COLOR_ACCENT));
        appendRaw(colorize("  DAVCoding Android  v1.0.0\n", COLOR_TEXT_SEC));
        appendRaw(colorize("  AI terminal · Powered by Claude Sonnet\n\n", COLOR_TEXT_SEC));
        appendSystem("Type a message or a slash command. /help for commands.");
    }

    // ── Rendering helpers ─────────────────────────────────────────────────────

    private void appendUser(String message) {
        chatBuffer.append("\n");
        chatLog.setText(chatBuffer);
        // Could use SpannableString for colored bubbles — simplified here
        chatBuffer.append("  ▶ You: ").append(message).append("\n");
        chatLog.setText(chatBuffer);
        scrollToBottom();
    }

    private void appendAIPrefix() {
        chatBuffer.append("\n  ⬡ DAVCoding: ");
        chatLog.setText(chatBuffer);
    }

    private void appendToken(String token) {
        chatBuffer.append(token);
        chatLog.setText(chatBuffer);
        scrollToBottom();
    }

    private void appendSystem(String msg) {
        chatBuffer.append("\n  ℹ ").append(msg).append("\n");
        chatLog.setText(chatBuffer);
        scrollToBottom();
    }

    private void appendError(String error) {
        chatBuffer.append("\n  ✗ Error: ").append(error).append("\n");
        chatLog.setText(chatBuffer);
        scrollToBottom();
    }

    private void appendNewline() {
        chatBuffer.append("\n");
        chatLog.setText(chatBuffer);
    }

    private void appendRaw(String text) {
        chatBuffer.append(text);
        chatLog.setText(chatBuffer);
    }

    private String colorize(String text, int color) {
        // Returns plain text for buffer (color applied via SpannableString in production)
        return text;
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }
}
