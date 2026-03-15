package terminal.demo

import terminal.buffer.TerminalBuffer
import terminal.model.enum.Style
import terminal.model.enum.TerminalColor
import terminal.render.AnsiRenderer

/**
 * Renders the demo screen.
 */
object DemoView {

    fun redraw(buffer: TerminalBuffer) {
        val row = buffer.getCursorRow() + 1
        val col = buffer.getCursorColumn() + 1
        print(buildString {
            append("\u001b[?25l")
            append("\u001b[2J\u001b[H")
            append(AnsiRenderer.renderScreen(buffer))
            append("\u001b[${row};${col}H")
            append("\u001b[?25h")
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

    fun appendLine(buffer: TerminalBuffer, line: String) {
        buffer.write(line + "\n")
        redraw(buffer)
    }

    fun restore() {
        print("\u001b[2J\u001b[H\u001b[?25h")
        System.out.flush()
    }
}
