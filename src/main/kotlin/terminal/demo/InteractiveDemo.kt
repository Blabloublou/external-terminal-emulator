package terminal.demo

import terminal.buffer.TerminalBuffer
import terminal.model.enum.TerminalColor

/**
 * Interactive demo.
 */
fun main() {
    val buffer = TerminalBuffer(width = 80, height = 24, maxScrollbackSize = 500)
    buffer.setForeground(TerminalColor.BrightCyan)
    buffer.write("Terminal Buffer\n")
    buffer.setForeground(TerminalColor.BrightBlack)
    buffer.write("Type text and press Enter. Use /help for all commands.\n")
    buffer.setForeground(TerminalColor.Default)

    val config = DemoConfig

    val rawMode = tryEnableRawMode()
    if (rawMode) {
        runRawLoop(buffer, config)
        restoreTerminal()
    } else {
        DemoView.redraw(buffer)
        runLineLoop(buffer, config)
    }
    DemoView.restore()
}

private fun runLineLoop(buffer: TerminalBuffer, config: DemoConfig) {
    val reader = System.`in`.bufferedReader()
    while (true) {
        val line = reader.readLine() ?: break
        if (processLine(buffer, line, config)) break
    }
}

private fun runRawLoop(buffer: TerminalBuffer, config: DemoConfig) {
    val keyReader = RawKeyReader(System.`in`)
    val history = mutableListOf<String>()
    var historyIndex = -1
    val inputLine = StringBuilder()
    var cursorIndex = 0
    val lastRow = buffer.height - 1

    fun refreshInput() {
        DemoView.refreshInputLine(buffer, inputLine.toString(), cursorIndex)
    }

    refreshInput()
    while (true) {
        when (val key = keyReader.readKey()) {
            is KeyInput.Eof -> break
            is KeyInput.Enter -> {
                val line = inputLine.toString()
                buffer.setCursor(0, lastRow)
                if (processLine(buffer, line, config)) break
                if (line.isNotBlank()) {
                    history.add(line)
                    historyIndex = -1
                }
                inputLine.clear()
                cursorIndex = 0
                refreshInput()
            }
            is KeyInput.Print -> {
                if (!Character.isISOControl(key.char)) {
                    inputLine.insert(cursorIndex, key.char)
                    cursorIndex++
                    refreshInput()
                }
            }
            KeyInput.Backspace -> {
                if (cursorIndex > 0) {
                    cursorIndex--
                    inputLine.deleteAt(cursorIndex)
                    refreshInput()
                }
            }
            KeyInput.ArrowLeft -> {
                if (cursorIndex > 0) {
                    cursorIndex--
                    refreshInput()
                }
            }
            KeyInput.ArrowRight -> {
                if (cursorIndex < inputLine.length) {
                    cursorIndex++
                    refreshInput()
                }
            }
            KeyInput.ArrowUp -> {
                if (history.isNotEmpty()) {
                    historyIndex = if (historyIndex < 0) history.size - 1 else (historyIndex - 1).coerceAtLeast(0)
                    inputLine.clear()
                    inputLine.append(history[historyIndex])
                    cursorIndex = inputLine.length
                    refreshInput()
                } else if (inputLine.isNotEmpty()) {
                    buffer.moveCursor(0, -1)
                    DemoView.redraw(buffer)
                }
            }
            KeyInput.ArrowDown -> {
                if (historyIndex >= 0) {
                    historyIndex++
                    if (historyIndex >= history.size) {
                        historyIndex = -1
                        inputLine.clear()
                        cursorIndex = 0
                    } else {
                        inputLine.clear()
                        inputLine.append(history[historyIndex])
                        cursorIndex = inputLine.length
                    }
                    refreshInput()
                } else if (inputLine.isNotEmpty()) {
                    buffer.moveCursor(0, 1)
                    DemoView.redraw(buffer)
                }
            }
        }
    }
}
