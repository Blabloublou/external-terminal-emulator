package terminal.demo

import terminal.buffer.TerminalBuffer
import terminal.demo.input.RawKeyReader
import terminal.demo.input.enableRawMode
import terminal.demo.input.restoreTerminalMode
import terminal.demo.input.runLineEditor
import terminal.model.enum.TerminalColor

/**
 * Interactive demo: type text and see it appear in the buffer with colors.
 */
fun main() {
    val buffer = TerminalBuffer(width = 80, height = 24, maxScrollbackSize = 500)
    buffer.setForeground(TerminalColor.BrightCyan)
    buffer.write("Terminal Buffer — Interactive demo\n")
    buffer.setForeground(TerminalColor.BrightBlack)
    buffer.write("Type text and press Enter. Commands: /color, /background, /style\n\n")
    buffer.setForeground(TerminalColor.Default)

    val config = DemoConfig
    fun redraw() = DemoView.redraw(buffer)

    redraw()

    fun handleLine(line: String): Boolean = processLine(buffer, line, config)

    if (!enableRawMode()) {
        println("Raw mode not available (run in a terminal).")
        return
    }
    try {
        runLineEditor(
            buffer = buffer,
            reader = RawKeyReader(),
            onLine = { handleLine(it) },
            redraw = ::redraw,
        )
    } finally {
        restoreTerminalMode()
    }

    DemoView.restore()
}
