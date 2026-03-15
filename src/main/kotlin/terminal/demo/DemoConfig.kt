package terminal.demo

import terminal.model.enum.Style
import terminal.model.enum.TerminalColor

/**
 * Demo configuration: color and style name maps, commands, help text.
 */
object DemoConfig {
    val colorMap: Map<String, TerminalColor> = mapOf(
        "default" to TerminalColor.Default,
        "black" to TerminalColor.Black,
        "red" to TerminalColor.Red,
        "green" to TerminalColor.Green,
        "blue" to TerminalColor.Blue,
        "yellow" to TerminalColor.Yellow,
        "cyan" to TerminalColor.Cyan,
        "magenta" to TerminalColor.Magenta,
        "white" to TerminalColor.White,
        "bright_black" to TerminalColor.BrightBlack,
        "bright_red" to TerminalColor.BrightRed,
        "bright_green" to TerminalColor.BrightGreen,
        "bright_yellow" to TerminalColor.BrightYellow,
        "bright_blue" to TerminalColor.BrightBlue,
        "bright_magenta" to TerminalColor.BrightMagenta,
        "bright_cyan" to TerminalColor.BrightCyan,
        "bright_white" to TerminalColor.BrightWhite,
    )

    val styleMap: Map<String, Style> = mapOf(
        "bold" to Style.Bold,
        "italic" to Style.Italic,
        "underline" to Style.Underline,
    )

    /** Command list for help. */
    val commands: List<Pair<String, String>> = listOf(
        "/help, /h" to "Show this help",
        "/quit, /q" to "Exit the demo",
        "/clear" to "Clear screen and show banner",
        "/resize W H" to "Resize terminal (e.g. /resize 80 24)",
        "/color <name>" to "Set foreground color",
        "/background <name>" to "Set background color",
        "/style <name>" to "Add style (use 'off' to reset)",
    )

    /** Build full help text from config (commands + color/style names). */
    fun buildHelpText(): String = buildString {
        append("--- Commands ---\n")
        for ((cmd, desc) in commands) {
            append("  $cmd  $desc\n")
        }
        append("\n--- Colors (for /color and /background) ---\n")
        append("  ${colorMap.keys.sorted().joinToString(", ")}\n")
        append("\n--- Styles (for /style, use 'off' to reset) ---\n")
        append("  ${styleMap.keys.joinToString(", ")}\n")
    }
}
