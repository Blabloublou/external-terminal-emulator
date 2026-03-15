package terminal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import terminal.buffer.TerminalBuffer

/**
 * Tests for the resize operation.
 */
class TerminalBufferResizeTest {

    @Test
    fun `resize updates width and height properties`() {
        val b = TerminalBuffer(10, 5, 0)
        b.resize(20, 10)
        assertEquals(20, b.width)
        assertEquals(10, b.height)
    }

    @Test
    fun `widen screen pads lines with empty cells on the right`() {
        val b = TerminalBuffer(5, 3, 0)
        b.write("Hello")
        b.resize(10, 3)
        val line = b.getLineAsString(b.scrollbackSize)
        assertEquals("Hello     ", line)
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
        b.write("ab\ncd\nef\n")
        val scrollbackIdx = 0
        b.resize(5, 2)
        assertEquals(5, b.getLineAsString(scrollbackIdx).length)
    }

    @Test
    fun `increase height appends empty lines at bottom`() {
        val b = TerminalBuffer(5, 2, 0)
        b.write("Hello")
        b.resize(5, 5)
        assertEquals(5, b.height)
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

    @Test
    fun `decrease height pushes top lines to scrollback`() {
        val b = TerminalBuffer(5, 4, 20)
        b.write("line0\nline1\nline2\nline3")
        b.setCursor(0, 0)
        b.resize(5, 2)
        assertEquals(2, b.height)
        assertTrue(b.scrollbackSize >= 2)
    }

    @Test
    fun `totalLineCount equals scrollbackSize plus new height after resize`() {
        val b = TerminalBuffer(5, 3, 10)
        b.write("a\nb\nc\nd\n") 
        val scrollBefore = b.scrollbackSize
        b.resize(5, 6)
        assertEquals(scrollBefore + 6, b.totalLineCount)
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
