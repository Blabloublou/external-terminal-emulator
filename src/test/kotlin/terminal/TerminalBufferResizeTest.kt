package terminal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import terminal.buffer.TerminalBuffer

/**
 * Tests for the resize operation.
 *
 * Resize strategy:
 *   - Narrowing (width decrease): lines are truncated on the right.
 *   - Widening  (width increase): lines are padded with empty cells on the right.
 *   - Shortening (height decrease): excess top lines are pushed to scrollback.
 *   - Talling   (height increase): empty lines are appended at the bottom.
 *
 * The cursor is clamped to the new bounds after resizing.
 */
class TerminalBufferResizeTest {

    // ── width: properties after resize ──────────────────────────────────────

    @Test
    fun `resize updates width and height properties`() {
        val b = TerminalBuffer(10, 5, 0)
        b.resize(20, 10)
        assertEquals(20, b.width)
        assertEquals(10, b.height)
    }

    @Test
    fun `resize clamps dimensions to at least 1`() {
        val b = TerminalBuffer(5, 5, 0)
        b.resize(0, -3)
        assertEquals(1, b.width)
        assertEquals(1, b.height)
    }

    // ── widening ─────────────────────────────────────────────────────────────

    @Test
    fun `widen screen pads lines with empty cells on the right`() {
        val b = TerminalBuffer(5, 3, 0)
        b.write("Hello")
        b.resize(10, 3)
        val line = b.getLineAsString(b.scrollbackSize)
        assertEquals("Hello     ", line)  // 5 original chars + 5 new spaces
        assertEquals(10, line.length)
    }

    @Test
    fun `widen does not change screen height`() {
        val b = TerminalBuffer(5, 3, 0)
        b.resize(8, 3)
        assertEquals(3, b.height)
    }

    @Test
    fun `widen also pads scrollback lines`() {
        val b = TerminalBuffer(3, 2, 10)
        b.write("ab\ncd\nef\n")  // forces scroll; "ab" goes to scrollback
        val scrollbackIdx = 0
        b.resize(5, 2)
        assertEquals(5, b.getLineAsString(scrollbackIdx).length)
    }

    // ── narrowing ────────────────────────────────────────────────────────────

    @Test
    fun `narrow screen truncates lines on the right`() {
        val b = TerminalBuffer(10, 3, 0)
        b.write("Hello")
        b.resize(3, 3)
        assertEquals("Hel", b.getLineAsString(b.scrollbackSize))
    }

    @Test
    fun `narrow also truncates scrollback lines`() {
        val b = TerminalBuffer(5, 2, 10)
        b.write("abcde\nfghij\nklmno\n")
        b.resize(3, 2)
        val scrollbackLine = b.getLineAsString(0)
        assertEquals(3, scrollbackLine.length, "scrollback lines should be truncated too")
    }

    @Test
    fun `cursor is clamped after narrowing`() {
        val b = TerminalBuffer(10, 5, 0)
        b.setCursor(9, 4)
        b.resize(4, 3)
        assertTrue(b.getCursorColumn() <= 3)
        assertTrue(b.getCursorRow() <= 2)
    }

    // ── talling (increase height) ─────────────────────────────────────────────

    @Test
    fun `increase height appends empty lines at bottom`() {
        val b = TerminalBuffer(5, 2, 0)
        b.write("Hello")
        b.resize(5, 5)
        assertEquals(5, b.height)
        // Last 3 new lines should be empty
        for (row in 2..4) {
            assertEquals("     ", b.getLineAsString(b.scrollbackSize + row))
        }
    }

    @Test
    fun `increase height does not affect scrollback`() {
        val b = TerminalBuffer(5, 2, 10)
        b.write("aaa\nbbb\nccc\n")
        val scrollBefore = b.scrollbackSize
        b.resize(5, 5)
        assertEquals(scrollBefore, b.scrollbackSize)
    }

    // ── shortening (decrease height) ─────────────────────────────────────────

    @Test
    fun `decrease height pushes top lines to scrollback`() {
        val b = TerminalBuffer(5, 4, 20)
        b.write("line0\nline1\nline2\nline3")
        b.setCursor(0, 0)
        // 4-line screen becomes 2-line screen → 2 top lines go to scrollback
        b.resize(5, 2)
        assertEquals(2, b.height)
        // The scrollback should contain the lines that were on screen rows 0 and 1
        assertTrue(b.scrollbackSize >= 2)
    }

    @Test
    fun `decrease height scrollback respects maxScrollbackSize`() {
        val b = TerminalBuffer(5, 4, 1)
        b.write("aaaaa\nbbbbb\nccccc\nddddd")
        b.resize(5, 2)
        // Only 1 line can be kept in scrollback
        assertEquals(1, b.scrollbackSize)
    }

    @Test
    fun `decrease height with maxScrollbackSize 0 discards pushed lines`() {
        val b = TerminalBuffer(5, 4, 0)
        b.write("aaaaa\nbbbbb\nccccc\nddddd")
        b.resize(5, 2)
        assertEquals(0, b.scrollbackSize)
        assertEquals(2, b.height)
    }

    // ── totalLineCount after resize ───────────────────────────────────────────

    @Test
    fun `totalLineCount equals scrollbackSize plus new height after resize`() {
        val b = TerminalBuffer(5, 3, 10)
        b.write("a\nb\nc\nd\n")   // forces some scrollback
        val scrollBefore = b.scrollbackSize
        b.resize(5, 6)
        assertEquals(scrollBefore + 6, b.totalLineCount)
    }

    // ── no-op resize ─────────────────────────────────────────────────────────

    @Test
    fun `resize to same dimensions is a no-op`() {
        val b = TerminalBuffer(5, 3, 10)
        b.write("Hello")
        b.resize(5, 3)
        assertEquals("Hello", b.getLineAsString(b.scrollbackSize).trim())
        assertEquals(5, b.width)
        assertEquals(3, b.height)
    }

    // ── content preservation ─────────────────────────────────────────────────

    @Test
    fun `content is preserved after widen then narrow back to original size`() {
        val b = TerminalBuffer(5, 3, 0)
        b.write("Hi")
        b.resize(10, 3)
        b.resize(5, 3)
        assertEquals("Hi   ", b.getLineAsString(b.scrollbackSize))
    }

    @Test
    fun `getScreenContent has new height lines after tall resize`() {
        val b = TerminalBuffer(4, 2, 0)
        b.resize(4, 5)
        val lines = b.getScreenContent().split("\n")
        assertEquals(5, lines.size)
    }

    @Test
    fun `getScreenContent has new height lines after short resize`() {
        val b = TerminalBuffer(4, 5, 10)
        b.resize(4, 2)
        val lines = b.getScreenContent().split("\n")
        assertEquals(2, lines.size)
    }
}
