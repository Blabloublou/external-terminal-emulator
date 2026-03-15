package terminal.demo

import java.io.PushbackInputStream
import java.io.InputStream

/**
 * Reads key-by-key from an input stream.
 */
class RawKeyReader(input: InputStream) {

    private val input = PushbackInputStream(input.buffered(), 4)


    fun readKey(): KeyInput {
        val b = input.read()
        if (b < 0) return KeyInput.Eof

        when (b) {
            0x0d, 0x0a -> return KeyInput.Enter
            0x7f, 0x08 -> return KeyInput.Backspace
            0x1b -> return readEscapeSequence()
            else -> return KeyInput.Print(b.toChar())
        }
    }

    private fun readEscapeSequence(): KeyInput {
        val b1 = input.read()
        if (b1 < 0) return KeyInput.Eof
        if (b1 != '['.code) {
            input.unread(b1)
            return KeyInput.Print(0x1b.toChar())
        }
        val b2 = input.read()
        if (b2 < 0) return KeyInput.Eof
        return when (b2) {
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
