package terminal.demo

import terminal.model.enum.Style
import terminal.model.enum.TerminalColor

/**
 * Demo configuration: color and style name maps, help text.
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
}
