package terminal.buffer

import terminal.model.Cell
import terminal.model.CharWidth
import terminal.model.enum.TerminalColor
import terminal.model.enum.WideCharRole

/**
 * Write operations.
 */
object BufferWrite {
    fun performWrite(buffer: TerminalBuffer, text: String) {
        for (c in text) {
            when {
                c == '\n' -> {
                    buffer.cursor.column = 0
                    buffer.cursor.row++
                    if (buffer.cursor.row >= buffer._height) {
                        buffer.cursor.row = buffer._height - 1
                        buffer.scrollUp()
                    }
                }
                CharWidth.of(c) == 2 -> writeWideChar(buffer, c)
                else -> {
                    buffer.clearWideCharAt(buffer.cursor.row, buffer.cursor.column)
                    buffer.screen[buffer.cursor.row][buffer.cursor.column] = buffer.cellWithCurrentAttributes(c)
                    buffer.cursor.column++
                    if (buffer.cursor.column >= buffer._width) {
                        buffer.cursor.column = 0
                        buffer.cursor.row++
                        if (buffer.cursor.row >= buffer._height) {
                            buffer.cursor.row = buffer._height - 1
                            buffer.scrollUp()
                        }
                    }
                }
            }
        }
    }

    fun writeWideChar(buffer: TerminalBuffer, c: Char) {
        if (buffer.cursor.column + 1 >= buffer._width) {
            buffer.clearWideCharAt(buffer.cursor.row, buffer.cursor.column)
            buffer.screen[buffer.cursor.row][buffer.cursor.column] = buffer.cellWithCurrentAttributes(null)
            buffer.cursor.column = 0
            buffer.cursor.row++
            if (buffer.cursor.row >= buffer._height) {
                buffer.cursor.row = buffer._height - 1
                buffer.scrollUp()
            }
        }
        buffer.clearWideCharAt(buffer.cursor.row, buffer.cursor.column)
        buffer.clearWideCharAt(buffer.cursor.row, buffer.cursor.column + 1)
        buffer.screen[buffer.cursor.row][buffer.cursor.column] = buffer.cellWithCurrentAttributes(c, WideCharRole.WideStart)
        buffer.screen[buffer.cursor.row][buffer.cursor.column + 1] =
            Cell(null, TerminalColor.Default, buffer.currentBackground, emptySet(), WideCharRole.WideContinuation)
        buffer.cursor.column += 2
        if (buffer.cursor.column >= buffer._width) {
            buffer.cursor.column = 0
            buffer.cursor.row++
            if (buffer.cursor.row >= buffer._height) {
                buffer.cursor.row = buffer._height - 1
                buffer.scrollUp()
            }
        }
    }
}
