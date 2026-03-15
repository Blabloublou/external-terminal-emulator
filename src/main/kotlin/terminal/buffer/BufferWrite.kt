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

    fun performWriteOnLine(buffer: TerminalBuffer, row: Int, text: String) {
        buffer.cursor.column = 0
        buffer.cursor.row = row.coerceIn(0, buffer._height - 1)
        val r = buffer.cursor.row
        var i = 0
        while (i < text.length && buffer.cursor.column < buffer._width) {
            val c = text[i]
            when {
                CharWidth.of(c) == 2 -> {
                    if (buffer.cursor.column + 1 >= buffer._width) break
                    buffer.clearWideCharAt(r, buffer.cursor.column)
                    buffer.clearWideCharAt(r, buffer.cursor.column + 1)
                    buffer.screen[r][buffer.cursor.column] = buffer.cellWithCurrentAttributes(c, WideCharRole.WideStart)
                    buffer.screen[r][buffer.cursor.column + 1] =
                        Cell(null, TerminalColor.Default, buffer.currentBackground, emptySet(), WideCharRole.WideContinuation)
                    buffer.cursor.column += 2
                    i++
                }
                else -> {
                    buffer.clearWideCharAt(r, buffer.cursor.column)
                    buffer.screen[r][buffer.cursor.column] = buffer.cellWithCurrentAttributes(c)
                    buffer.cursor.column++
                    i++
                }
            }
        }
        val endCol = buffer.cursor.column
        while (buffer.cursor.column < buffer._width) {
            buffer.clearWideCharAt(r, buffer.cursor.column)
            buffer.screen[r][buffer.cursor.column] = buffer.cellWithCurrentAttributes(null)
            buffer.cursor.column++
        }
        buffer.cursor.column = endCol
    }
}
