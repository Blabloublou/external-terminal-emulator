package terminal.demo

import java.io.FileOutputStream
import java.io.PrintStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import terminal.buffer.TerminalBuffer
import terminal.model.enum.TerminalColor

/**
 * Interactive demo.
 */
fun main() {
    System.setOut(PrintStream(FileOutputStream(java.io.FileDescriptor.out), true, StandardCharsets.UTF_8))
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
    val reader = InputStreamReader(System.`in`, StandardCharsets.UTF_8).buffered()
    while (true) {
        val line = reader.readLine() ?: break
        if (processLine(buffer, line, config)) break
    }
}

private fun runRawLoop(buffer: TerminalBuffer, config: DemoConfig) {
    val keyReader = RawKeyReader(InputStreamReader(System.`in`, StandardCharsets.UTF_8))
    val history = mutableListOf<String>()
    var historyIndex = -1
    val inputLine = StringBuilder()
    var cursorIndex = 0
    var scrollOffset = 0

    fun lastRow(): Int = buffer.height - 1

    fun refreshInput() {
        DemoView.refreshInputLine(buffer, inputLine.toString(), cursorIndex)
        scrollOffset = 0
    }

    fun redrawWithScroll() {
        DemoView.redraw(buffer, scrollOffset = scrollOffset)
    }

    refreshInput()
    while (true) {
        when (val key = keyReader.readKey()) {
            is KeyInput.Eof -> break
            is KeyInput.Enter -> {
                val line = inputLine.toString()
                buffer.setCursor(0, lastRow())
                if (processLine(buffer, line, config)) break
                if (line.isNotBlank()) {
                    history.add(line)
                    historyIndex = -1
                }
                inputLine.clear()
                cursorIndex = 0
                scrollOffset = 0
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
                if (!config.scrollModeEnabled) {
                    if (historyIndex >= 0) {
                        historyIndex = (historyIndex - 1).coerceAtLeast(0)
                        inputLine.clear()
                        inputLine.append(history[historyIndex])
                        cursorIndex = inputLine.length
                        refreshInput()
                    } else if (history.isNotEmpty() && inputLine.isEmpty()) {
                        historyIndex = history.size - 1
                        inputLine.append(history[historyIndex])
                        cursorIndex = inputLine.length
                        refreshInput()
                    } else if (inputLine.isNotEmpty()) {
                        buffer.moveCursor(0, -1)
                        DemoView.redraw(buffer)
                    }
                } else {
                    if (scrollOffset < buffer.scrollbackSize) {
                        scrollOffset = (scrollOffset + 1).coerceAtMost(buffer.scrollbackSize)
                        redrawWithScroll()
                    } else if (inputLine.isNotEmpty()) {
                        buffer.moveCursor(0, -1)
                        DemoView.redraw(buffer)
                    }
                }
            }
            KeyInput.ArrowDown -> {
                if (!config.scrollModeEnabled) {
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
                } else {
                    if (scrollOffset > 0) {
                        scrollOffset = (scrollOffset - 1).coerceAtLeast(0)
                        redrawWithScroll()
                    } else if (inputLine.isNotEmpty()) {
                        buffer.moveCursor(0, 1)
                        DemoView.redraw(buffer)
                    }
                }
            }
        }
    }
}
