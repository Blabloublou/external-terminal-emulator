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

    val cursorShapeMap: Map<String, CursorShape> = mapOf(
        "block" to CursorShape.Block,
        "underline" to CursorShape.Underline,
        "bar" to CursorShape.Bar,
    )

    val commands: List<Pair<String, String>> = listOf(
        "/help, /h" to "Show this help",
        "/quit, /q" to "Exit the demo",
        "/clear" to "Clear screen and show banner",
        "/resize W H" to "Resize terminal (e.g. /resize 80 24)",
        "/color <name>" to "Set foreground color",
        "/background <name>" to "Set background color",
        "/style <name>" to "Add style (use 'off' to reset)",
        "/cursor <name>" to "Set cursor shape (block, underline, bar)",
        "/save [name]" to "Save current config (colors, style, cursor)",
        "/select <name>" to "Apply a saved configuration",
    )

    fun buildHelpText(): String = buildString {
        append("--- Commands ---\n")
        for ((cmd, desc) in commands) {
            append("  $cmd  $desc\n")
        }
        append("\n--- Colors (for /color and /background) ---\n")
        append("  ${colorMap.keys.sorted().joinToString(", ")}\n")
        append("\n--- Styles (for /style, use 'off' to reset) ---\n")
        append("  ${styleMap.keys.joinToString(", ")}\n")
        append("\n--- Cursor shapes (for /cursor) ---\n")
        append("  ${cursorShapeMap.keys.joinToString(", ")}\n")
        if (savedConfigurations.isNotEmpty()) {
            append("\n--- Saved configurations (/select <name>) ---\n")
            append("  ${savedConfigurations.keys.sorted().joinToString(", ")}\n")
        }
    }
}
