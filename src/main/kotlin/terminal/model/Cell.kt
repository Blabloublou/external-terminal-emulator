package terminal.model

import terminal.model.enum.TerminalColor
import terminal.model.enum.Style

/**
 * One character cell in the terminal buffer.
 */
data class Cell(
    val char: Char?,
    val foreground: TerminalColor,
    val background: TerminalColor,
    val styles: Set<Style> = emptySet(),
) {
    companion object {
        /** Empty cell with default configuration. */
        val EMPTY = Cell(
            char = null,
            foreground = TerminalColor.Default,
            background = TerminalColor.Default,
            styles = emptySet(),
        )

        /** Placeholder right-half cell of a wide character. */
        val CONTINUATION = Cell(
            char = null,
            foreground = TerminalColor.Default,
            background = TerminalColor.Default,
        )

        /** Empty cell represented as space for string output. */
        const val EMPTY_CHAR_REPR = ' '
    }

    /** Character for string output: null (including continuation) becomes EMPTY_CHAR_REPR. */
    fun charForDisplay(): Char = char ?: EMPTY_CHAR_REPR
}
