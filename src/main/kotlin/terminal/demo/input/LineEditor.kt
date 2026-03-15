package terminal.demo.input

import terminal.buffer.TerminalBuffer
import terminal.model.enum.TerminalColor

/**
 * Line editor
 */
fun runLineEditor(
    buffer: TerminalBuffer,
    reader: RawKeyReader,
    onLine: (String) -> Boolean,
    redraw: () -> Unit,
) {
    val history = mutableListOf<String>()
    var historyIndex = -1
    var savedCurrentLine = ""
    val currentLine = StringBuilder()

    fun refreshCurrentRow() {
        val r = buffer.getCursorRow()
        buffer.setForeground(TerminalColor.Default)
        buffer.setBackground(TerminalColor.Default)
        buffer.setStyles(emptySet())
        buffer.setCursor(0, r)
        buffer.fillLine(r, null)
        buffer.write(currentLine.toString())
    }

    while (true) {
        when (val key = reader.readKey()) {
            null -> break
            is KeyEvent.Enter -> {
                val line = currentLine.toString()
                buffer.write("\n")
                if (onLine(line)) break
                if (line.isNotBlank() && (history.isEmpty() || history.last() != line)) {
                    history.add(line)
                }
                historyIndex = -1
                currentLine.clear()
                savedCurrentLine = ""
                redraw()
            }
            is KeyEvent.Backspace -> {
                if (currentLine.isNotEmpty()) {
                    currentLine.deleteCharAt(currentLine.length - 1)
                    refreshCurrentRow()
                    redraw()
                }
            }
            is KeyEvent.ArrowUp -> {
                if (history.isNotEmpty()) {
                    if (historyIndex < 0) {
                        savedCurrentLine = currentLine.toString()
                        historyIndex = history.size - 1
                    } else if (historyIndex > 0) {
                        historyIndex--
                    }
                    currentLine.clear()
                    currentLine.append(history[historyIndex])
                    refreshCurrentRow()
                    redraw()
                }
            }
            is KeyEvent.ArrowDown -> {
                if (historyIndex >= 0) {
                    historyIndex++
                    if (historyIndex >= history.size) {
                        historyIndex = -1
                        currentLine.clear()
                        currentLine.append(savedCurrentLine)
                    } else {
                        currentLine.clear()
                        currentLine.append(history[historyIndex])
                    }
                    refreshCurrentRow()
                    redraw()
                }
            }
            is KeyEvent.Char -> {
                currentLine.append(key.char)
                refreshCurrentRow()
                redraw()
            }
        }
    }
}
