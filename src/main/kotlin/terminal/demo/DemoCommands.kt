package terminal.demo

import terminal.buffer.TerminalBuffer

/**
 * Parses demo input and delegates each command to DemoView.
 */
fun processLine(
    buffer: TerminalBuffer,
    line: String,
    config: DemoConfig,
): Boolean {
    if (line == "/quit" || line == "/q") return true
    when {
        line == "/help" || line == "/h" -> DemoView.showHelp(buffer, config)
        line == "/clear" -> DemoView.clearAndShowBanner(buffer, config)
        line.startsWith("/scrollback") -> {
            val arg = line.removePrefix("/scrollback").trim()
            if (arg.isBlank()) {
                buffer.write("Max scrollback: ${buffer.maxScrollbackSize} lines. Use /scrollback <N> to change.\n")
            } else {
                val n = arg.toIntOrNull()
                if (n != null && n >= 0) {
                    buffer.maxScrollbackSize = n
                    buffer.write("Max scrollback set to $n lines.\n")
                } else {
                    buffer.write("Usage: /scrollback <N> (N = non-negative integer)\n")
                }
            }
            DemoView.redraw(buffer)
        }
        line.startsWith("/scroll") -> {
            val arg = line.removePrefix("/scroll").trim().lowercase()
            when (arg) {
                "on", "1", "yes" -> {
                    config.scrollModeEnabled = true
                    buffer.write("Scroll mode on: ↑/↓ will scroll buffer history.\n")
                }
                "off", "0", "no" -> {
                    config.scrollModeEnabled = false
                    buffer.write("Scroll mode off: ↑/↓ will browse command history when line is empty.\n")
                }
                "" -> {
                    val state = if (config.scrollModeEnabled) "on" else "off"
                    buffer.write("Scroll mode is $state. Use /scroll on or /scroll off to change.\n")
                }
                else -> {
                    buffer.write("Usage: /scroll [on|off]\n")
                }
            }
            DemoView.redraw(buffer)
        }
        line.startsWith("/resize ") -> {
            val parts = line.removePrefix("/resize ").trim().split(Regex("\\s+"))
            val w = parts.getOrNull(0)?.toIntOrNull()
            val h = parts.getOrNull(1)?.toIntOrNull()
            if (w != null && h != null) {
                buffer.resize(w, h)
                DemoView.redraw(buffer)
            } else {
                buffer.write("Expected format: /resize W H (two integers)\n")
                DemoView.redraw(buffer)
            }
        }
        line.startsWith("/color ") -> {
            val name = line.removePrefix("/color ").trim().lowercase()
            config.colorMap[name]?.let { DemoView.setForeground(buffer, it) } ?: DemoView.redraw(buffer)
        }
        line.startsWith("/background ") -> {
            val name = line.removePrefix("/background ").trim().lowercase()
            config.colorMap[name]?.let { DemoView.setBackground(buffer, it) } ?: DemoView.redraw(buffer)
        }
        line.startsWith("/style ") -> {
            val name = line.removePrefix("/style ").trim().lowercase()
            when (name) {
                "off", "reset", "none" -> DemoView.clearStyles(buffer)
                else -> config.styleMap[name]?.let { DemoView.addStyle(buffer, it) } ?: DemoView.redraw(buffer)
            }
        }
        line.startsWith("/cursor ") -> {
            val name = line.removePrefix("/cursor ").trim().lowercase()
            val style = buffer.getCursorStyle()
            when (name) {
                "hide" -> {
                    buffer.setCursorStyle(style.copy(visible = false))
                    DemoView.redraw(buffer)
                }
                "show" -> {
                    buffer.setCursorStyle(style.copy(visible = true))
                    DemoView.redraw(buffer)
                }
                "blink" -> {
                    buffer.setCursorStyle(style.copy(blink = true))
                    DemoView.redraw(buffer)
                }
                "noblink" -> {
                    buffer.setCursorStyle(style.copy(blink = false))
                    DemoView.redraw(buffer)
                }
                else -> config.cursorShapeMap[name]?.let { shape ->
                    buffer.setCursorStyle(style.copy(shape = shape))
                    DemoView.redraw(buffer)
                } ?: DemoView.redraw(buffer)
            }
        }
        line == "/save" || line.startsWith("/save ") -> {
            val name = line.removePrefix("/save").trim().ifBlank { "default" }
            val saved = SavedDemoConfig(
                foreground = buffer.getForeground(),
                background = buffer.getBackground(),
                styles = buffer.getStyles(),
                cursorStyle = buffer.getCursorStyle(),
            )
            config.savedConfigurations[name] = saved
            buffer.write("Saved configuration \"$name\".\n")
            DemoView.redraw(buffer)
        }
        line == "/select" || line.startsWith("/select ") -> {
            val name = line.removePrefix("/select").trim()
            if (name.isBlank()) {
                if (config.savedConfigurations.isEmpty()) {
                    buffer.write("No saved configurations. Use /save <name> to save one.\n")
                } else {
                    buffer.write("Saved configurations: ${config.savedConfigurations.keys.sorted().joinToString(", ")}\n")
                    buffer.write("Use /select <name> to apply one.\n")
                }
                DemoView.redraw(buffer)
            } else {
                val saved = config.savedConfigurations[name]
                if (saved != null) {
                    DemoView.applySavedConfig(buffer, saved)
                    buffer.write("Applied configuration \"$name\".\n")
                    DemoView.redraw(buffer)
                } else {
                    val available = config.savedConfigurations.keys.sorted().joinToString(", ").ifEmpty { "(none)" }
                    buffer.write("Unknown configuration \"$name\". Available: $available\n")
                    DemoView.redraw(buffer)
                }
            }
        }
        line.startsWith("/") -> {
            buffer.write("Unknown command: $line  (use /help for the list of commands)\n")
            DemoView.redraw(buffer)
        }
        else -> {
            buffer.write(line + "\n")
            DemoView.redraw(buffer)
        }
    }
    return false
}
