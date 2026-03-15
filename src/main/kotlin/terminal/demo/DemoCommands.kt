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
        line == "/clear" -> DemoView.clearAndShowBanner(buffer, config)
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
            config.colorMap[name]?.let { DemoView.setForeground(buffer, it) }
        }
        line.startsWith("/background ") -> {
            val name = line.removePrefix("/background ").trim().lowercase()
            config.colorMap[name]?.let { DemoView.setBackground(buffer, it) }
        }
        line.startsWith("/style ") -> {
            val name = line.removePrefix("/style ").trim().lowercase()
            when (name) {
                "off", "reset", "none" -> DemoView.clearStyles(buffer)
                else -> config.styleMap[name]?.let { DemoView.addStyle(buffer, it) }
            }
        }
        else -> DemoView.redraw(buffer)
    }
    return false
}
