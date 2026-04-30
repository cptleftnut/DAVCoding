# DAVCoding CLI

The power of AI-assisted development, now in your terminal.

DAVCoding CLI brings AI-powered coding assistance directly to your command line, enabling you to build, debug, and understand code through natural language conversations. Powered by an advanced agentic harness, it provides intelligent assistance while staying deeply integrated with your GitHub workflow.

![Image of the splash screen for the DAVCoding CLI](https://github.com/user-attachments/assets/f40aa23d-09dd-499e-9457-1d57d3368887)


## 🚀 Introduction and Overview

We're bringing the power of AI coding agents directly to your terminal. With DAVCoding CLI, you can work locally and synchronously with an AI agent that understands your code and GitHub context.

- **Terminal-native development:** Work with the DAVCoding agent directly in your command line — no context switching required.
- **GitHub integration out of the box:** Access your repositories, issues, and pull requests using natural language, all authenticated with your existing GitHub account.
- **Agentic capabilities:** Build, edit, debug, and refactor code with an AI collaborator that can plan and execute complex tasks.
- **MCP-powered extensibility:** Take advantage of the fact that the coding agent ships with GitHub's MCP server by default and supports custom MCP servers to extend capabilities.
- **Full control:** Preview every action before execution — nothing happens without your explicit approval.

We're still early in our journey, but with your feedback, we're rapidly iterating to make DAVCoding CLI the best possible companion in your terminal.

## 📦 Getting Started

### Supported Platforms

- **Linux**
- **macOS**
- **Windows**

### Prerequisites

- (On Windows) **PowerShell** v6 or higher
- A **GitHub account** with access to DAVCoding CLI.

### Installation

Install with the install script (macOS and Linux):

```bash
curl -fsSL https://raw.githubusercontent.com/cptleftnut/DAVCoding/main/install.sh | bash
```

Or

```bash
wget -qO- https://raw.githubusercontent.com/cptleftnut/DAVCoding/main/install.sh | bash
```

Use `| sudo bash` to run as root and install to `/usr/local/bin`.

Set `PREFIX` to install to `$PREFIX/bin/` directory. Defaults to `/usr/local`
when run as root or `$HOME/.local` when run as a non-root user.

Set `VERSION` to install a specific version. Defaults to the latest version.

For example, to install version `v1.0.39` to a custom directory:

```bash
curl -fsSL https://raw.githubusercontent.com/cptleftnut/DAVCoding/main/install.sh | VERSION="v1.0.39" PREFIX="$HOME/custom" bash
```

Install with [Homebrew](https://brew.sh) (macOS and Linux):

```bash
brew install davcoding-cli
```

```bash
brew install davcoding-cli@prerelease
```


Install with [WinGet](https://github.com/microsoft/winget-cli) (Windows):

```bash
winget install DAVCoding.CLI
```

```bash
winget install DAVCoding.CLI.Prerelease
```


Install with [npm](https://www.npmjs.com) (macOS, Linux, and Windows):

```bash
npm install -g @davcoding/cli
```

```bash
npm install -g @davcoding/cli@prerelease
```


### Launching the CLI

```bash
davcoding
```

On first launch, you'll be greeted with an animated banner! If you'd like to see this banner again, launch `davcoding` with the `--banner` flag.

If you're not currently logged in to GitHub, you'll be prompted to use the `/login` slash command. Enter this command and follow the on-screen instructions to authenticate.

#### Authenticate with a Personal Access Token (PAT)

You can also authenticate using a fine-grained PAT with the "DAVCoding Requests" permission enabled.

1. Visit https://github.com/settings/personal-access-tokens/new
2. Under "Permissions," click "add permissions" and select "DAVCoding Requests"
3. Generate your token
4. Add the token to your environment via the environment variable `GH_TOKEN` or `GITHUB_TOKEN` (in order of precedence)

### Using the CLI

Launch `davcoding` in a folder that contains code you want to work with.

By default, `davcoding` utilizes Claude Sonnet 4.5. Run the `/model` slash command to choose from other available models, including Claude Sonnet 4 and GPT-5.

### Experimental Mode

Experimental mode enables access to new features that are still in development. You can activate experimental mode by:

- Launching with the `--experimental` flag: `davcoding --experimental`
- Using the `/experimental` slash command from within the CLI

Once activated, the setting is persisted in your config, so the `--experimental` flag is no longer needed on subsequent launches.

#### Experimental Features

- **Autopilot mode:** Autopilot is a new mode (press `Shift+Tab` to cycle through modes), which encourages the agent to continue working until a task is completed.

For more information about how to use DAVCoding CLI, see [our repository](https://github.com/cptleftnut/DAVCoding).

## 🔧 Configuring LSP Servers

DAVCoding CLI supports Language Server Protocol (LSP) for enhanced code intelligence. This feature provides intelligent code features like go-to-definition, hover information, and diagnostics.

### Installing Language Servers

DAVCoding CLI does not bundle LSP servers. You need to install them separately. For example, to set up TypeScript support:

```bash
npm install -g typescript-language-server
```

For other languages, install the corresponding LSP server and configure it following the same pattern shown below.

### Configuring LSP Servers

LSP servers are configured through a dedicated LSP configuration file. You can configure LSP servers at the user level or repository level:

**User-level configuration** (applies to all projects):
Edit `~/.davcoding/lsp-config.json`

**Repository-level configuration** (applies to specific project):
Create `.github/lsp.json` in your repository root

Example configuration:

```json
{
  "lspServers": {
    "typescript": {
      "command": "typescript-language-server",
      "args": ["--stdio"],
      "fileExtensions": {
        ".ts": "typescript",
        ".tsx": "typescript"
      }
    }
  }
}
```

### Viewing LSP Server Status

Check configured LSP servers using the `/lsp` command in an interactive session, or view your configuration files directly.

For more information, see the [changelog](./changelog.md).

## 📢 Feedback and Participation

We're excited to have you join us early in the DAVCoding CLI journey.

We're building quickly. Expect frequent updates — please keep your client up to date for the latest features and fixes!

Your insights are invaluable! Open an issue in this repo, join Discussions, and run `/feedback` from the CLI to submit a confidential feedback survey!
