package terminal.buffer

import terminal.model.Cell
import terminal.model.CharWidth
import terminal.model.enum.TerminalColor
import terminal.model.enum.WideCharRole

/**
 * Insert operations.
 */
object BufferInsert {
    fun performInsert(buffer: TerminalBuffer, text: String) {
        for (c in text) {
            when {
                c == '\n' -> {
                    buffer.cursor.column = 0
                    buffer.cursor.row++
                    if (buffer.cursor.row >= buffer._height) {
                        buffer.scrollUp()
                        buffer.cursor.row = buffer._height - 1
                    }
                }
                else -> insertOneChar(buffer, c)
            }
        }
    }

    private fun insertOneChar(buffer: TerminalBuffer, c: Char) {
        var r = buffer.cursor.row
        var col = buffer.cursor.column
        var cell = buffer.cellWithCurrentAttributes(c)
        while (r < buffer._height) {
            val line = buffer.screen[r]
            val pushedOff = line[buffer._width - 1]
            for (i in buffer._width - 1 downTo col + 1) line[i] = line[i - 1]
            line[col] = cell
            if (pushedOff == Cell.EMPTY) {
                buffer.cursor.column = col + 1
                if (buffer.cursor.column >= buffer._width) {
                    buffer.cursor.column = 0
                    buffer.cursor.row = r + 1
                    if (buffer.cursor.row >= buffer._height) {
                        buffer.scrollUp()
                        buffer.cursor.row = buffer._height - 1
                    }
                } else {
                    buffer.cursor.row = r
                }
                return
            }
            cell = pushedOff
            col = 0
            r++
            if (r >= buffer._height) {
                buffer.scrollUp()
                r = buffer._height - 1
                buffer.screen[r][0] = cell
                buffer.cursor.column = 1
                buffer.cursor.row = r
                return
            }
        }
    }


}
