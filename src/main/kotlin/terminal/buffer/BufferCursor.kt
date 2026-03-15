package terminal.buffer

/**
 * Cursor operations.
 */
object BufferCursor {
    fun setCursor(buffer: TerminalBuffer, column: Int, row: Int) {
        buffer.cursor.column = column.coerceIn(0, buffer._width - 1)
        buffer.cursor.row = row.coerceIn(0, buffer._height - 1)
        buffer.snapCursorOffContinuation()
    }

    fun move(buffer: TerminalBuffer, deltaColumn: Int, deltaRow: Int) {
        buffer.cursor.column = (buffer.cursor.column + deltaColumn).coerceIn(0, buffer._width - 1)
        buffer.cursor.row = (buffer.cursor.row + deltaRow).coerceIn(0, buffer._height - 1)
        buffer.snapCursorOffContinuation()
    }
}
