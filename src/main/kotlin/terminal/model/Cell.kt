package terminal.model

import terminal.model.enum.TerminalColor
import terminal.model.enum.Style
import terminal.model.enum.WideCharRole

/**
 * One character cell in the terminal buffer.
 */
data class Cell(
    val char: Char?,
    val foreground: TerminalColor,
    val background: TerminalColor,
    val styles: Set<Style> = emptySet(),
    val wideCharRole: WideCharRole = WideCharRole.Normal,
) {
    companion object {
        /** Empty cell with default configuration. */
        val EMPTY = Cell(
            char = null,
            foreground = TerminalColor.Default,
            background = TerminalColor.Default,
            styles = emptySet(),
            wideCharRole = WideCharRole.Normal,
        )

        /** Empty cell represented as space for string output. */
        const val EMPTY_CHAR_REPR = ' '
    }

    /** Character for string output: null (including continuation) becomes EMPTY_CHAR_REPR. */
    fun charForDisplay(): Char = char ?: EMPTY_CHAR_REPR
}
