package terminal.demo

import terminal.buffer.TerminalBuffer
import terminal.model.enum.CursorShape
import terminal.model.enum.Style
import terminal.model.enum.TerminalColor
import terminal.render.AnsiRenderer

/** Used when redraw is called without explicit blink phase (e.g. after Enter). */
var blinkPhase: Boolean = true

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

    fun redraw(buffer: TerminalBuffer, blinkCursorVisible: Boolean? = null) {
        val phase = blinkCursorVisible ?: blinkPhase
        val cursorStyle = buffer.getCursorStyle()
        val showCursor = cursorStyle.visible && (!cursorStyle.blink || phase)
        val row = buffer.getCursorRow() + 1
        val displayCol = buffer.getDisplayColumn(buffer.getCursorRow(), buffer.getCursorColumn())
        print(buildString {
            append("\u001b[?25l")
            append("\u001b[2J\u001b[H")
            if (showCursor) {
                append(AnsiRenderer.renderScreen(buffer))
                append("\u001b[${row};${displayCol}H")
                append(cursorShapeSequence(cursorStyle.shape, cursorStyle.blink))
                append("\u001b[?25h")
            } else {
                append(AnsiRenderer.renderScreenWithCursor(buffer, showCursorNow = false))
                append("\u001b[${row};${displayCol}H")
            }
        })
        System.out.flush()
    }

    fun clearAndShowBanner(buffer: TerminalBuffer, config: DemoConfig) {
        buffer.clearScreen()
        buffer.setForeground(TerminalColor.BrightCyan)
        buffer.write("Terminal Buffer\n")
        buffer.setForeground(TerminalColor.BrightBlack)
        buffer.write("Type text and press Enter. Use /help for all commands.\n\n")
        buffer.setForeground(TerminalColor.Default)
        redraw(buffer)
    }

    fun showHelp(buffer: TerminalBuffer, config: DemoConfig) {
        buffer.write("\n")
        buffer.setForeground(TerminalColor.BrightCyan)
        buffer.write(config.buildHelpText())
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

    fun restore() {
        print("\u001b[2J\u001b[H\u001b[?25h")
        System.out.flush()
    }
}
