package terminal.buffer

import terminal.model.Cell
import terminal.model.cursor.Cursor
import terminal.model.cursor.CursorStyle
import terminal.model.enum.Style
import terminal.model.enum.TerminalColor
import terminal.model.enum.WideCharRole

/**
 * Terminal text buffer: a grid of cells.
 */
class TerminalBuffer(
    width: Int,
    height: Int,
    private val maxScrollbackSize: Int,
) {
    internal var _width = width.coerceAtLeast(1)
    internal var _height = height.coerceAtLeast(1)
    internal val _maxScrollbackSize = maxScrollbackSize.coerceAtLeast(0)

    val width: Int get() = _width
    val height: Int get() = _height

    internal val screen: MutableList<MutableList<Cell>> = MutableList(_height) {
        MutableList(_width) { Cell.EMPTY }
    }

    internal val scrollback: MutableList<List<Cell>> = mutableListOf()

    internal val cursor: Cursor = Cursor(0, 0)

    internal var currentForeground: TerminalColor = TerminalColor.Default
        private set
    internal var currentBackground: TerminalColor = TerminalColor.Default
        private set
    internal var currentStyles: MutableSet<Style> = mutableSetOf()
        private set

    init {
        clampCursor()
    }


    internal fun clampCursor() {
        cursor.column = cursor.column.coerceIn(0, _width - 1)
        cursor.row = cursor.row.coerceIn(0, _height - 1)
    }

  

    private fun emptyLine(): MutableList<Cell> = MutableList(_width) { Cell.EMPTY }


    fun getCursor(): Cursor = cursor
    fun getCursorColumn(): Int = cursor.column
    fun getCursorRow(): Int = cursor.row
    fun getCursorStyle(): CursorStyle = cursor.style
    fun setCursorStyle(style: CursorStyle) { cursor.style = style }

    fun setCursor(column: Int, row: Int) = BufferCursor.setCursor(this, column, row)
    fun moveCursor(deltaColumn: Int, deltaRow: Int) = BufferCursor.move(this, deltaColumn, deltaRow)

    fun getForeground(): TerminalColor = currentForeground
    fun setForeground(color: TerminalColor) { currentForeground = color }

    fun getBackground(): TerminalColor = currentBackground
    fun setBackground(color: TerminalColor) { currentBackground = color }

    fun getStyles(): Set<Style> = currentStyles.toSet()
    fun setStyles(styles: Set<Style>) { currentStyles = styles.toMutableSet() }


    internal fun scrollUp() {
        val topLine = screen.removeAt(0).toList()
        scrollback.add(topLine)
        if (scrollback.size > _maxScrollbackSize) scrollback.removeAt(0)
        screen.add(emptyLine())
    }



    fun write(text: String) {
        BufferWrite.performWrite(this, text)
    }

    fun insert(text: String) {
        BufferInsert.performInsert(this, text)
    }

    fun fillLine(row: Int, char: Char?) {
        val r = row.coerceIn(0, _height - 1)
        val cell = cellWithCurrentAttributes(char)
        for (c in 0 until _width) screen[r][c] = cell
    }

    fun insertLineAtBottom() {
        scrollUp()
    }

    fun clearScreen() {
        for (row in screen) {
            for (i in row.indices) row[i] = Cell.EMPTY
        }
        cursor.column = 0
        cursor.row = 0
    }

    fun clearAll() {
        clearScreen()
        scrollback.clear()
    }

    /** Resize the screen to [newWidth]×[newHeight]. Content is truncated or padded; cursor is clamped. */
    fun resize(newWidth: Int, newHeight: Int) {
        BufferResize.apply(this, newWidth, newHeight)
    }

    internal fun lineAt(lineIndex: Int): List<Cell>? = when {
        lineIndex < 0 -> null
        lineIndex < scrollback.size -> scrollback[lineIndex]
        lineIndex < scrollback.size + _height -> screen[lineIndex - scrollback.size]
        else -> null
    }

    fun getChar(lineIndex: Int, col: Int): Char? = BufferContentAccess.getChar(this, lineIndex, col)

    fun getAttributes(lineIndex: Int, col: Int): Cell? = BufferContentAccess.getAttributes(this, lineIndex, col)

    fun getLineAsString(lineIndex: Int): String = BufferContentAccess.getLineAsString(this, lineIndex)

    fun getScreenContent(): String = BufferContentAccess.getScreenContent(this)

    fun getFullContent(): String = BufferContentAccess.getFullContent(this)

    fun scrollbackSize(): Int = scrollback.size

    fun totalLineCount(): Int = scrollback.size + _height
}
