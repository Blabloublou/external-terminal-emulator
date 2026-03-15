package terminal.demo

import terminal.model.cursor.CursorStyle
import terminal.model.enum.CursorShape
import terminal.model.enum.Style
import terminal.model.enum.TerminalColor

/**
 * Snapshot of current display attributes and cursor style for /save and /select.
 */
data class SavedDemoConfig(
    val foreground: TerminalColor,
    val background: TerminalColor,
    val styles: Set<Style>,
    val cursorStyle: CursorStyle,
)

/**
 * Demo configuration: color and style name maps, commands, help text.
 */
object DemoConfig {
    var scrollModeEnabled: Boolean = false

    val savedConfigurations: MutableMap<String, SavedDemoConfig> = mutableMapOf()
    val colorMap: Map<String, TerminalColor> = mapOf(
        "default" to TerminalColor.Default,
        "black" to TerminalColor.Black,
        "red" to TerminalColor.Red,
        "green" to TerminalColor.Green,
        "blue" to TerminalColor.Blue,
        "yellow" to TerminalColor.Yellow,
        "cyan" to TerminalColor.Cyan,
        "magenta" to TerminalColor.Magenta,
        "purple" to TerminalColor.Magenta,
        "white" to TerminalColor.White,
        "bright_black" to TerminalColor.BrightBlack,
        "bright_red" to TerminalColor.BrightRed,
        "bright_green" to TerminalColor.BrightGreen,
        "bright_yellow" to TerminalColor.BrightYellow,
        "bright_blue" to TerminalColor.BrightBlue,
        "bright_magenta" to TerminalColor.BrightMagenta,
        "bright_purple" to TerminalColor.BrightMagenta,
        "bright_cyan" to TerminalColor.BrightCyan,
        "bright_white" to TerminalColor.BrightWhite,
    )

    val styleMap: Map<String, Style> = mapOf(
        "bold" to Style.Bold,
        "italic" to Style.Italic,
        "underline" to Style.Underline,
    )

    val cursorShapeMap: Map<String, CursorShape> = mapOf(
        "block" to CursorShape.Block,
        "underline" to CursorShape.Underline,
        "bar" to CursorShape.Bar,
    )

    val commands: List<Pair<String, String>> = listOf(
        "/help, /h" to "Show this help",
        "/quit, /q" to "Exit the demo",
        "← →" to "Move cursor on the line (real terminal only)",
        "↑ ↓ (line empty)" to "History or scrollback (see /scroll)",
        "/scroll [on|off]" to "Use ↑/↓ to scroll buffer history (on) or command history (off, default)",
        "/scrollback <N>" to "Set max scrollback size in lines (e.g. /scrollback 1000)",
        "/clear" to "Clear screen and show banner",
        "/resize W H" to "Resize terminal (e.g. /resize 80 24)",
        "/color <name>" to "Set foreground color",
        "/background <name>" to "Set background color",
        "/style <name>" to "Add style (use 'off' to reset)",
        "/cursor <name>" to "Shape: block, underline, bar. Visibility: hide, show. Blink: blink, noblink",
        "/save [name]" to "Save current config (colors, style, cursor)",
        "/select <name>" to "Apply a saved configuration",
    )

    private fun wrap(line: String, width: Int): String {
        if (width < 1 || line.length <= width) return line
        return buildString {
            var remaining = line
            while (remaining.length > width) {
                val chunk = remaining.take(width + 1)
                val lastSpace = chunk.lastIndexOf(' ')
                val breakAt = if (lastSpace > 0) lastSpace else width
                append(remaining.take(breakAt)).append("\n")
                remaining = remaining.drop(breakAt).trimStart()
            }
            if (remaining.isNotEmpty()) append(remaining)
        }
    }

    fun buildHelpText(width: Int = 80): String = buildString {
        append("--- Commands ---\n")
        for ((cmd, desc) in commands) {
            append(wrap("  $cmd  $desc", width)).append("\n")
        }
        append("\n--- Colors (for /color and /background) ---\n")
        append(wrap("  ${colorMap.keys.sorted().joinToString(", ")}", width)).append("\n")
        append("\n--- Styles (for /style, use 'off' to reset) ---\n")
        append(wrap("  ${styleMap.keys.joinToString(", ")}", width)).append("\n")
        append("\n--- Cursor (/cursor) ---\n")
        append(wrap("  Shapes: ${cursorShapeMap.keys.joinToString(", ")}", width)).append("\n")
        append(wrap("  Visibility: hide, show. Blink: blink, noblink", width)).append("\n")
        if (savedConfigurations.isNotEmpty()) {
            append("\n--- Saved configurations (/select <name>) ---\n")
            append(wrap("  ${savedConfigurations.keys.sorted().joinToString(", ")}", width)).append("\n")
        }
    }
}
