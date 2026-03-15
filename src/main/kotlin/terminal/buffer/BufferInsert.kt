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
                CharWidth.of(c) == 2 -> insertWideChar(buffer, c)
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

    private fun insertWideChar(buffer: TerminalBuffer, c: Char) {
        var r = buffer.cursor.row
        var col = buffer.cursor.column
        if (col + 2 > buffer._width) {
            buffer.clearWideCharAt(r, col)
            buffer.screen[r][col] = buffer.cellWithCurrentAttributes(null)
            buffer.cursor.column = 0
            buffer.cursor.row = r + 1
            if (buffer.cursor.row >= buffer._height) {
                buffer.scrollUp()
                buffer.cursor.row = buffer._height - 1
            }
            insertWideChar(buffer, c)
            return
        }
        val line = buffer.screen[r]
        buffer.clearWideCharAt(r, col)
        buffer.clearWideCharAt(r, col + 1)
        val oldAtSecondLast = line[buffer._width - 2]
        val oldAtLast = line[buffer._width - 1]
        for (i in buffer._width - 1 downTo col + 2) line[i] = line[i - 2]
        line[col] = buffer.cellWithCurrentAttributes(c, WideCharRole.WideStart)
        line[col + 1] = Cell(null, TerminalColor.Default, buffer.currentBackground, emptySet(), WideCharRole.WideContinuation)
        buffer.cursor.column = col + 2
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
        if (oldAtSecondLast != Cell.EMPTY || oldAtLast != Cell.EMPTY) {
            var row = if (buffer.cursor.column > 0) r + 1 else buffer.cursor.row
            var cell1 = oldAtSecondLast
            var cell2 = oldAtLast
            if (row >= buffer._height) {
                buffer.scrollUp()
                row = buffer._height - 1
                buffer.screen[row][0] = cell1
                buffer.screen[row][1] = cell2
                buffer.cursor.column = 2
                buffer.cursor.row = row
                if (buffer.cursor.column >= buffer._width) {
                    buffer.cursor.column = 0
                    buffer.cursor.row = row + 1
                    if (buffer.cursor.row >= buffer._height) {
                        buffer.scrollUp()
                        buffer.cursor.row = buffer._height - 1
                    }
                }
                return
            }
            while (row < buffer._height) {
                val line2 = buffer.screen[row]
                val n1 = line2[buffer._width - 2]
                val n2 = line2[buffer._width - 1]
                for (i in buffer._width - 1 downTo 2) line2[i] = line2[i - 2]
                line2[0] = cell1
                line2[1] = cell2
                if (n1 == Cell.EMPTY && n2 == Cell.EMPTY) {
                    buffer.cursor.column = 2
                    buffer.cursor.row = row
                    if (buffer.cursor.column >= buffer._width) {
                        buffer.cursor.column = 0
                        buffer.cursor.row = row + 1
                        if (buffer.cursor.row >= buffer._height) {
                            buffer.scrollUp()
                            buffer.cursor.row = buffer._height - 1
                        }
                    }
                    return
                }
                cell1 = n1
                cell2 = n2
                row++
                if (row >= buffer._height) {
                    buffer.scrollUp()
                    row = buffer._height - 1
                    buffer.screen[row][0] = cell1
                    buffer.screen[row][1] = cell2
                    buffer.cursor.column = 2
                    buffer.cursor.row = row
                    if (buffer.cursor.column >= buffer._width) {
                        buffer.cursor.column = 0
                        buffer.cursor.row = row + 1
                        if (buffer.cursor.row >= buffer._height) {
                            buffer.scrollUp()
                            buffer.cursor.row = buffer._height - 1
                        }
                    }
                    return
                }
            }
        }
    }
}
