package terminal.model

/**
 * Utility for determining the display width of a character.
 */
object CharWidth {

    fun of(c: Char): Int = of(c.code)

    fun of(codePoint: Int): Int = when {
        codePoint < 0x1100 -> 1
        codePoint in 0x1100..0x115F -> 2   
        codePoint == 0x2329 || codePoint == 0x232A -> 2
        codePoint in 0x2E80..0x303E -> 2    
        codePoint in 0x3040..0x33FF -> 2   
        codePoint in 0x3400..0x4DBF -> 2  
        codePoint in 0x4E00..0x9FFF -> 2  
        codePoint in 0xA000..0xA4CF -> 2   
        codePoint in 0xAC00..0xD7AF -> 2   
        codePoint in 0xF900..0xFAFF -> 2   
        codePoint in 0xFE10..0xFE19 -> 2  
        codePoint in 0xFE30..0xFE4F -> 2  
        codePoint in 0xFF00..0xFF60 -> 2   
        codePoint in 0xFFE0..0xFFE6 -> 2   
        else -> 1
    }
}
