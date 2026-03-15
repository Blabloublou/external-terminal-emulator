package terminal.demo

/**
 * Key event from raw terminal input.
 */
sealed class KeyInput {
    data class Print(val char: Char) : KeyInput()
    object Backspace : KeyInput()
    object Enter : KeyInput()
    object ArrowUp : KeyInput()
    object ArrowDown : KeyInput()
    object ArrowLeft : KeyInput()
    object ArrowRight : KeyInput()
    object Eof : KeyInput()
}
