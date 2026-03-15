package terminal.demo.input

import java.io.InputStream

/**
 * Key event from raw terminal input (after parsing escape sequences).
 */
sealed class KeyEvent {
    data class Char(val char: kotlin.Char) : KeyEvent()
    object Enter : KeyEvent()
    object Backspace : KeyEvent()
    object ArrowUp : KeyEvent()
    object ArrowDown : KeyEvent()
}

/**
 * Reads from input.
 */
class RawKeyReader(private val input: InputStream = System.`in`) {
    private val ESC = 27
    private val BRACKET = '['.code

    fun readKey(): KeyEvent? {
        val b = input.read()
        if (b < 0) return null
        if (b == ESC) return readEscapeSequence()
        return when (b) {
            13, 10 -> KeyEvent.Enter
            127, 8 -> KeyEvent.Backspace
            else -> KeyEvent.Char(b.toChar())
        }
    }

    private fun readEscapeSequence(): KeyEvent? {
        val b2 = input.read()
        if (b2 < 0) return null
        if (b2 != BRACKET) return KeyEvent.Char(ESC.toChar())
        while (true) {
            val b = input.read()
            if (b < 0) return null
            when (b) {
                'A'.code -> return KeyEvent.ArrowUp
                'B'.code -> return KeyEvent.ArrowDown
                'C'.code, 'D'.code -> return null
                in '0'.code..'9'.code, ';'.code -> { }
                else -> return null
            }
        }
    }
}

/**
 * Puts the terminal in raw mode.
 */
fun enableRawMode(): Boolean {
    return try {
        Runtime.getRuntime().exec(arrayOf("stty", "raw", "-echo", "-icanon", "min", "1")).waitFor() == 0
    } catch (e: Exception) {
        false
    }
}

/**
 * Restores the terminal to normal mode.
 */
fun restoreTerminalMode() {
    try {
        Runtime.getRuntime().exec(arrayOf("stty", "sane")).waitFor()
    } catch (_: Exception) { }
}
