package terminal.model

/**
 * Utility for determining the display width (in terminal columns) of a character.
 *
 * Most characters occupy 1 column. Characters in CJK, Hangul, fullwidth, and
 * several other Unicode blocks occupy 2 columns (wide characters).
 *
 * This covers the most common wide-character ranges. Supplementary plane code
 * points (emoji, etc.) represented as surrogate pairs are not handled here since
 * Kotlin/JVM Char is UTF-16; use [ofCodePoint] for those.
 */
object CharWidth {

    /** Returns 1 or 2: the number of terminal columns occupied by [c]. */
    fun of(c: Char): Int = of(c.code)

    /** Returns 1 or 2 for the given Unicode code point. */
    fun of(codePoint: Int): Int = when {
        codePoint < 0x1100 -> 1
        codePoint in 0x1100..0x115F -> 2   // Hangul Jamo
        codePoint == 0x2329 || codePoint == 0x232A -> 2
        codePoint in 0x2E80..0x303E -> 2   // CJK Radicals, Kangxi, Bopomofo, etc.
        codePoint in 0x3040..0x33FF -> 2   // Hiragana, Katakana, CJK Symbols & Punctuation, CJK Compatibility
        codePoint in 0x3400..0x4DBF -> 2   // CJK Unified Ideographs Extension A
        codePoint in 0x4E00..0x9FFF -> 2   // CJK Unified Ideographs
        codePoint in 0xA000..0xA4CF -> 2   // Yi Syllables & Radicals
        codePoint in 0xAC00..0xD7AF -> 2   // Hangul Syllables
        codePoint in 0xF900..0xFAFF -> 2   // CJK Compatibility Ideographs
        codePoint in 0xFE10..0xFE19 -> 2   // Vertical Forms
        codePoint in 0xFE30..0xFE4F -> 2   // CJK Compatibility Forms
        codePoint in 0xFF00..0xFF60 -> 2   // Fullwidth Forms
        codePoint in 0xFFE0..0xFFE6 -> 2   // Fullwidth Signs
        else -> 1
    }
}
