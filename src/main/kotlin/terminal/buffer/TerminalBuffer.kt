package terminal.buffer

import terminal.model.Cell
import terminal.model.CharWidth
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
    maxScrollbackSize: Int,
) {
    internal var _width = width.coerceAtLeast(1)
    internal var _height = height.coerceAtLeast(1)
    internal var _maxScrollbackSize = maxScrollbackSize.coerceAtLeast(0)

    val width: Int get() = _width
    val height: Int get() = _height

    var maxScrollbackSize: Int
        get() = _maxScrollbackSize
        set(value) {
            val n = value.coerceAtLeast(0)
            _maxScrollbackSize = n
            while (scrollback.size > _maxScrollbackSize) scrollback.removeAt(0)
        }

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

    internal fun cellWithCurrentAttributes(char: Char?, role: WideCharRole = WideCharRole.Normal): Cell =
        Cell(char, currentForeground, currentBackground, currentStyles.toSet(), role)

    internal fun clearWideCharAt(row: Int, col: Int) {
        if (row !in 0 until _height || col !in 0 until _width) return
        val line = screen[row]
        when (line[col].wideCharRole) {
            WideCharRole.WideStart -> {
                line[col] = Cell.EMPTY
                if (col + 1 < _width) line[col + 1] = Cell.EMPTY
            }
            WideCharRole.WideContinuation -> {
                if (col > 0) line[col - 1] = Cell.EMPTY
                line[col] = Cell.EMPTY
            }
            WideCharRole.Normal -> { }
        }
    }

    internal fun snapCursorOffContinuation() {
        if (cursor.row !in 0 until _height || cursor.column !in 0 until _width) return
        if (screen[cursor.row][cursor.column].wideCharRole == WideCharRole.WideContinuation && cursor.column > 0) {
            cursor.column = cursor.column - 1
        }
    }

    private fun emptyLine(): MutableList<Cell> = MutableList(_width) { Cell.EMPTY }


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

    fun writeOnLine(row: Int, text: String) {
        fillLine(row, null)
        BufferWrite.performWriteOnLine(this, row, text)
    }

    fun fillLine(row: Int, char: Char?) {
        val r = row.coerceIn(0, _height - 1)
        val line = screen[r]
        if (char == null || CharWidth.of(char) == 1) {
            val cell = cellWithCurrentAttributes(char)
            for (c in 0 until _width) line[c] = cell
        } else {
            var c = 0
            while (c < _width) {
                if (c + 1 < _width) {
                    line[c] = cellWithCurrentAttributes(char, WideCharRole.WideStart)
                    line[c + 1] = Cell(null, TerminalColor.Default, currentBackground, emptySet(), WideCharRole.WideContinuation)
                    c += 2
                } else {
                    line[c] = cellWithCurrentAttributes(null)
                    c++
                }
            }
        }
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

    val scrollbackSize: Int get() = scrollback.size

    val totalLineCount: Int get() = scrollback.size + _height

    fun getDisplayColumn(screenRow: Int, bufferColumn: Int): Int {
        val lineIndex = scrollbackSize + screenRow.coerceIn(0, _height - 1)
        val col = bufferColumn.coerceIn(0, _width)
        var displayCol = 1
        for (c in 0 until col) {
            when (getAttributes(lineIndex, c)?.wideCharRole) {
                WideCharRole.WideContinuation -> {}
                WideCharRole.WideStart -> displayCol += 2
                else -> displayCol += 1
            }
        }
        return displayCol
    }
}
