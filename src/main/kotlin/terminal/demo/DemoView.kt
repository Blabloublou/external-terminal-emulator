package terminal.demo

import terminal.buffer.TerminalBuffer
import terminal.model.CharWidth
import terminal.model.enum.CursorShape
import terminal.model.enum.Style
import terminal.model.enum.TerminalColor
import terminal.render.AnsiRenderer

/** Used when redraw is called without explicit blink phase. */
var blinkPhase: Boolean = true

private fun columnFromCharIndex(s: String, charIndex: Int): Int {
    val i = charIndex.coerceIn(0, s.length)
    var col = 0
    for (j in 0 until i) col += CharWidth.of(s[j])
    return col
}

private fun cursorShapeSequence(shape: CursorShape, blink: Boolean): String {
    val base = when (shape) {
        CursorShape.Block -> if (blink) 1 else 2
        CursorShape.Underline -> if (blink) 3 else 4
        CursorShape.Bar -> if (blink) 5 else 6
    }
    return "\u001b[$base q"
}

/**
 * Renders the demo screen.
 */
object DemoView {

    fun redraw(buffer: TerminalBuffer, blinkCursorVisible: Boolean? = null, scrollOffset: Int = 0) {
        val phase = blinkCursorVisible ?: blinkPhase
        val cursorStyle = buffer.getCursorStyle()
        val showCursor = scrollOffset == 0 && cursorStyle.visible && (!cursorStyle.blink || phase)
        val cursorRow = buffer.getCursorRow()
        val cursorCol = buffer.getCursorColumn()
        val displayCol = 1 + cursorCol
        val cursorChar = when (cursorStyle.shape) {
            CursorShape.Block -> '▌'
            CursorShape.Underline -> '▁'
            CursorShape.Bar -> '|'
        }
        print(buildString {
            append("\u001b[?25l")
            append("\u001b[H\u001b[2J")
            if (scrollOffset > 0) {
                val effectiveOffset = scrollOffset.coerceIn(0, buffer.scrollbackSize)
                val fromLine = buffer.scrollbackSize - effectiveOffset
                val linesText = AnsiRenderer.renderLines(buffer, fromLine, buffer.height)
                val lineList = linesText.split("\n").take(buffer.height)
                for (row in 0 until buffer.height) {
                    append("\u001b[${row + 1};1H")
                    if (row < lineList.size) append(lineList[row])
                    else append("\u001b[K")
                }
            } else {
                for (row in 0 until buffer.height) {
                    append("\u001b[${row + 1};1H")
                    if (showCursor) {
                        append(AnsiRenderer.renderLine(buffer, row))
                    } else {
                        append(AnsiRenderer.renderLine(buffer, row, cursorRow, cursorCol, cursorChar))
                    }
                }
                append("\u001b[${cursorRow + 1};${displayCol}H")
                if (showCursor) {
                    append(cursorShapeSequence(cursorStyle.shape, cursorStyle.blink))
                    append("\u001b[?25h")
                }
            }
        })
        System.out.flush()
    }

    fun clearAndShowBanner(buffer: TerminalBuffer, config: DemoConfig) {
        buffer.clearScreen()
        buffer.setForeground(TerminalColor.BrightCyan)
        buffer.write("Terminal Buffer\n")
        buffer.setForeground(TerminalColor.BrightBlack)
        buffer.write("Type text and press Enter. Use /help for all commands.\n")
        buffer.setForeground(TerminalColor.Default)
        redraw(buffer)
    }

    fun showHelp(buffer: TerminalBuffer, config: DemoConfig) {
        buffer.write("\n")
        buffer.setForeground(TerminalColor.BrightCyan)
        buffer.write(config.buildHelpText(buffer.width))
        buffer.setForeground(TerminalColor.Default)
        buffer.write("\n")
        redraw(buffer)
    }

    fun setForeground(buffer: TerminalBuffer, color: TerminalColor) {
        buffer.setForeground(color)
        redraw(buffer)
    }

    fun setBackground(buffer: TerminalBuffer, color: TerminalColor) {
        buffer.setBackground(color)
        redraw(buffer)
    }

    fun clearStyles(buffer: TerminalBuffer) {
        buffer.setStyles(emptySet())
        redraw(buffer)
    }

    fun addStyle(buffer: TerminalBuffer, style: Style) {
        buffer.setStyles(buffer.getStyles() + style)
        redraw(buffer)
    }

    fun applySavedConfig(buffer: TerminalBuffer, config: SavedDemoConfig) {
        buffer.setForeground(config.foreground)
        buffer.setBackground(config.background)
        buffer.setStyles(config.styles)
        buffer.setCursorStyle(config.cursorStyle)
        redraw(buffer)
    }

    fun appendLine(buffer: TerminalBuffer, line: String) {
        buffer.write(line + "\n")
        redraw(buffer)
    }

    fun refreshInputLine(buffer: TerminalBuffer, lineContent: String, cursorCharIndex: Int) {
        val lastRow = buffer.height - 1
        buffer.writeOnLine(lastRow, lineContent)
        val col = columnFromCharIndex(lineContent, cursorCharIndex).coerceIn(0, (buffer.width - 1).coerceAtLeast(0))
        buffer.setCursor(col, lastRow)
        redraw(buffer)
    }

    fun restore() {
        print("\u001b[2J\u001b[H\u001b[?25h\u001b[1 q")
        System.out.flush()
    }
}
