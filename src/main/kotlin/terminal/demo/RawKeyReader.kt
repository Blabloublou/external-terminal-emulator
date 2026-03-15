package terminal.demo

import java.io.PushbackReader
import java.io.Reader

/**
 */
class RawKeyReader(reader: Reader) {

    private val input = PushbackReader(reader.buffered(), 4)

    fun readKey(): KeyInput {
        val c = input.read()
        if (c < 0) return KeyInput.Eof

        when (c) {
            0x0d, 0x0a -> return KeyInput.Enter
            0x7f, 0x08 -> return KeyInput.Backspace
            0x1b -> return readEscapeSequence()
            else -> return KeyInput.Print(c.toChar())
        }
    }

    private fun readEscapeSequence(): KeyInput {
        val c1 = input.read()
        if (c1 < 0) return KeyInput.Eof
        if (c1 != '['.code) {
            input.unread(c1)
            return KeyInput.Print(0x1b.toChar())
        }
        val c2 = input.read()
        if (c2 < 0) return KeyInput.Eof
        return when (c2) {
            'A'.code -> KeyInput.ArrowUp
            'B'.code -> KeyInput.ArrowDown
            'C'.code -> KeyInput.ArrowRight
            'D'.code -> KeyInput.ArrowLeft
            else -> KeyInput.Print(0x1b.toChar()) 
        }
    }
}


fun tryEnableRawMode(): Boolean {
    return try {
        val pb = ProcessBuilder("stty", "raw", "-echo", "min", "1")
        pb.inheritIO()
        val p = pb.start()
        p.waitFor()
        p.exitValue() == 0
    } catch (_: Exception) {
        false
    }
}


fun restoreTerminal() {
    try {
        ProcessBuilder("stty", "sane").inheritIO().start().waitFor()
    } catch (_: Exception) { }
}
