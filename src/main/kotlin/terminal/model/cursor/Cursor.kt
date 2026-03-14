package terminal.model.cursor

/**
 * Cursor state: position on the screen and optional appearance.
 */
class Cursor(
    column: Int = 0,
    row: Int = 0,
    style: CursorStyle = CursorStyle.Default,
) {
    var column: Int = column
    var row: Int = row

    var style: CursorStyle = style

    fun position(): Pair<Int, Int> = column to row
}
