package terminal.buffer

import terminal.model.Cell
import terminal.model.enum.WideCharRole

/**
 * Resize logic.
 */
object BufferResize {
    fun apply(buffer: TerminalBuffer, newWidth: Int, newHeight: Int) {
        val w = newWidth.coerceAtLeast(1)
        val h = newHeight.coerceAtLeast(1)

        if (w != buffer._width) {
            if (w < buffer._width) {
                for (line in buffer.screen) {
                    if (line.size > w && w > 0 && line[w - 1].wideCharRole == WideCharRole.WideStart) {
                        line[w - 1] = Cell.EMPTY
                    }
                    while (line.size > w) line.removeAt(line.size - 1)
                }
                for (i in buffer.scrollback.indices) {
                    buffer.scrollback[i] = buffer.scrollback[i].take(w)
                }
            } else {
                for (line in buffer.screen) {
                    repeat(w - line.size) { line.add(Cell.EMPTY) }
                }
                for (i in buffer.scrollback.indices) {
                    val extra = w - buffer.scrollback[i].size
                    if (extra > 0) buffer.scrollback[i] = buffer.scrollback[i] + List(extra) { Cell.EMPTY }
                }
            }
            buffer._width = w
        }

        if (h != buffer._height) {
            if (h < buffer._height) {
                repeat(buffer._height - h) {
                    val topLine = buffer.screen.removeAt(0).toList()
                    if (buffer._maxScrollbackSize > 0) {
                        buffer.scrollback.add(topLine)
                        if (buffer.scrollback.size > buffer._maxScrollbackSize) buffer.scrollback.removeAt(0)
                    }
                }
            } else {
                repeat(h - buffer._height) {
                    buffer.screen.add(MutableList(buffer._width) { Cell.EMPTY })
                }
            }
            buffer._height = h
        }

        buffer.clampCursor()
    }
}
