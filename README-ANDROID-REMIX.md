# DAVCoding Android — Terminal × AI Remix

> **Branch:** `davcoding-android-remix`  
> **Base:** DAVCoding CLI × Termux terminal engine  
> **Status:** 🚧 Active development

---

## What is this?

This branch remixes two projects:

| Project | Role |
|---|---|
| **DAVCoding CLI** | AI-powered coding assistant (Claude Sonnet, GitHub MCP, LSP) |
| **Termux** | Battle-tested Android terminal emulator engine |

The result: **DAVCoding Android** — a full Android terminal app with a built-in AI coding agent overlay. Run `davcoding` commands natively from your phone/tablet, with an AI panel that streams responses directly into your terminal sessions.

---

## Architecture

```
DAVCoding Android
├── android-app/                  ← DAVCoding Android app (rebranded from Termux)
│   └── src/main/java/com/davcoding/android/
│       └── ai/
│           ├── DAVCodingAISession.java   ← AI API client (streaming, slash commands)
│           └── DAVCodingAIOverlay.java   ← Floating AI chat panel UI
├── terminal-emulator/            ← Termux VT100/xterm terminal emulator core
├── terminal-view/                ← Termux Android terminal view widget
├── termux-shared/                ← Termux shared utilities (file, net, shell)
├── install.sh                    ← DAVCoding CLI installer (Linux/macOS)
└── README.md                     ← DAVCoding CLI docs
```

---

## DAVCoding AI Features (Android)

- **Streaming AI chat** — Claude Sonnet 4.5 responses stream token-by-token into the overlay panel
- **Terminal injection** — AI can output shell commands prefixed with `$ ` for one-tap execution
- **Slash commands** — `/model`, `/autopilot`, `/login`, `/clear`, `/help`, `/feedback`
- **Conversation history** — Full multi-turn context across your terminal session
- **Autopilot mode** — Agent continues without confirmation (`Shift+Tab` to toggle)
- **GitHub integration** — Set `GH_TOKEN` to activate repo/issue/PR awareness
- **LSP support** — Language Server Protocol via DAVCoding CLI backend

---

## Getting Started

### Build (Android)

```bash
git clone https://github.com/cptleftnut/DAVCoding.git -b davcoding-android-remix
cd DAVCoding
./gradlew :android-app:assembleDebug
```

APK output: `android-app/build/outputs/apk/debug/davcoding-app_*.apk`

### Install DAVCoding CLI (inside the terminal)

```bash
curl -fsSL https://raw.githubusercontent.com/cptleftnut/DAVCoding/main/install.sh | bash
davcoding
```

### Configure AI

```bash
export DAVCODING_API_KEY=<your-key>
export GH_TOKEN=<your-github-pat>
davcoding --model claude-sonnet-4-5
```

---

## Branch Map

| Branch | Contents |
|---|---|
| `main` | DAVCoding CLI docs & installer |
| `master` | Termux source (unmodified snapshot) |
| `termux-features` | Termux source (tagged for reference) |
| `davcoding-android-remix` | **This branch** — remixed DAVCoding Android |

---

## Credits

- **Termux** — [github.com/termux/termux-app](https://github.com/termux/termux-app) (GPL-3.0)
- **DAVCoding** — [github.com/cptleftnut/DAVCoding](https://github.com/cptleftnut/DAVCoding)
- **Claude** — Anthropic (AI backbone)

---

*DAVCoding Android — The power of AI-assisted development. Now in your pocket.*
