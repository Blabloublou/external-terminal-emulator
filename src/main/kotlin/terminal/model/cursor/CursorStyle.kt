package terminal.model.cursor

import terminal.model.enum.CursorShape

/**
 * Cursor appearance options.
 */
data class CursorStyle(
    val visible: Boolean = true,
    val shape: CursorShape = CursorShape.Block,
    val blink: Boolean = true,
) {
    companion object {
        val Default = CursorStyle()
    }
}
