package terminal.demo

import terminal.buffer.TerminalBuffer
import terminal.model.enum.TerminalColor

/**
 * Interactive demo.
 * No blink thread: redraw only on Enter, so typed text (terminal echo) stays visible until you submit.
 */
fun main() {
    val buffer = TerminalBuffer(width = 80, height = 24, maxScrollbackSize = 500)
    buffer.setForeground(TerminalColor.BrightCyan)
    buffer.write("Terminal Buffer — Interactive demo\n")
    buffer.setForeground(TerminalColor.BrightBlack)
    buffer.write("Type text and press Enter. Use /help for all commands.\n\n")
    buffer.setForeground(TerminalColor.Default)

    val config = DemoConfig

    DemoView.redraw(buffer)

    val reader = System.`in`.bufferedReader()
    while (true) {
        val line = reader.readLine() ?: break
        if (processLine(buffer, line, config)) break
    }
    DemoView.restore()
}
