package terminal.buffer

import terminal.model.Cell
import terminal.model.enum.WideCharRole

/**
 * Content access.
 */
object BufferContentAccess {
    fun getChar(buffer: TerminalBuffer, lineIndex: Int, col: Int): Char? {
        val line = buffer.lineAt(lineIndex) ?: return null
        if (col !in 0 until buffer.width) return null
        return line[col].char
    }

    fun getAttributes(buffer: TerminalBuffer, lineIndex: Int, col: Int): Cell? {
        val line = buffer.lineAt(lineIndex) ?: return null
        if (col !in 0 until buffer.width) return null
        return line[col]
    }

    /** Line as string. Continuation cells are skipped. */
    fun getLineAsString(buffer: TerminalBuffer, lineIndex: Int): String {
        val line = buffer.lineAt(lineIndex) ?: return ""
        return buildString {
            for (cell in line) {
                if (cell.wideCharRole != WideCharRole.WideContinuation) append(cell.charForDisplay())
            }
        }
    }

    fun getScreenContent(buffer: TerminalBuffer): String =
        (0 until buffer.height).joinToString("\n") { row ->
            getLineAsString(buffer, buffer.scrollbackSize + row)
        }

    fun getFullContent(buffer: TerminalBuffer): String =
        (0 until buffer.totalLineCount).joinToString("\n") { getLineAsString(buffer, it) }
}
