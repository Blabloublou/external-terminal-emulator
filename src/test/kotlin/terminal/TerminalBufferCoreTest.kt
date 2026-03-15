package terminal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import terminal.buffer.TerminalBuffer
import terminal.model.enum.Style
import terminal.model.enum.TerminalColor

/**
 * Core buffer behavior.
 */
class TerminalBufferCoreTest {

    @Nested
    inner class Setup {
        @Test fun `constructor creates buffer with given dimensions`() {
            val b = TerminalBuffer(80, 24, 1000)
            assertEquals(80, b.width)
            assertEquals(24, b.height)
        }

        @Test fun `initial cursor is at 0 0`() {
            val b = TerminalBuffer(10, 5, 100)
            assertEquals(0, b.getCursorColumn())
            assertEquals(0, b.getCursorRow())
        }

        @Test fun `initial screen is empty`() {
            val b = TerminalBuffer(5, 3, 10)
            assertEquals("     \n     \n     ", b.getScreenContent())
        }
    }

    @Nested
    inner class Cursor {
        @Test fun `moveCursor at column 0 left does not move`() {
            val b = TerminalBuffer(5, 3, 10)
            b.moveCursor(-1, 0)
            assertEquals(0, b.getCursorColumn())
        }

        @Test fun `moveCursor at row 0 up does not move`() {
            val b = TerminalBuffer(5, 3, 10)
            b.moveCursor(0, -1)
            assertEquals(0, b.getCursorRow())
        }

        @Test fun `getDisplayColumn is 1-based and accounts for wide char display width`() {
            val b = TerminalBuffer(5, 2, 10)
            b.write("ab")
            assertEquals(1, b.getDisplayColumn(0, 0))
            assertEquals(2, b.getDisplayColumn(0, 1))
            b.setCursor(0, 1)
            b.write("\u65e5")
            assertEquals(1, b.getDisplayColumn(1, 0))
            assertEquals(3, b.getDisplayColumn(1, 1))
        }
    }

    @Nested
    inner class Attributes {
        @Test fun `write uses current foreground and background`() {
            val b = TerminalBuffer(5, 3, 10)
            b.setForeground(TerminalColor.Red)
            b.setBackground(TerminalColor.Blue)
            b.write("x")
            val cell = b.getAttributes(0, 0)!!
            assertEquals('x', cell.char)
            assertEquals(TerminalColor.Red, cell.foreground)
            assertEquals(TerminalColor.Blue, cell.background)
        }

        @Test fun `write uses current styles`() {
            val b = TerminalBuffer(5, 3, 10)
            b.setStyles(setOf(Style.Bold, Style.Underline))
            b.write("a")
            val cell = b.getAttributes(0, 0)!!
            assertTrue(cell.styles.contains(Style.Bold))
            assertTrue(cell.styles.contains(Style.Underline))
        }
    }

    @Nested
    inner class ContentAccess {
        @Test fun `getChar returns character at position`() {
            val b = TerminalBuffer(5, 3, 10)
            b.write("x")
            assertEquals('x', b.getChar(0, 0))
        }

        @Test fun `getChar from scrollback returns character from history`() {
            val b = TerminalBuffer(3, 2, 100)
            b.write("AB\nCD\n")
            assertEquals(1, b.scrollbackSize)
            assertEquals('A', b.getChar(0, 0))
            assertEquals('B', b.getChar(0, 1))
        }

        @Test fun `getChar from screen returns character from visible screen`() {
            val b = TerminalBuffer(3, 2, 100)
            b.write("AB\nCD\n")   
            b.write("XY")    
            val screenRowWithXY = b.scrollbackSize + 1
            assertEquals('X', b.getChar(screenRowWithXY, 0))
            assertEquals('Y', b.getChar(screenRowWithXY, 1))
        }

        @Test fun `getScreenContent returns all screen lines`() {
            val b = TerminalBuffer(4, 2, 10)
            b.write("AB\nCD")
            assertEquals("AB  \nCD  ", b.getScreenContent())
        }

        @Test fun `getFullContent returns scrollback then screen`() {
            val b = TerminalBuffer(3, 2, 100)
            b.write("AB\nCD\n")
            val lines = b.getFullContent().split("\n")
            assertTrue(lines[0].contains("AB"))
            assertTrue(lines.any { it.contains("CD") })
            assertTrue(lines.indexOfFirst { it.contains("AB") } < lines.indexOfFirst { it.contains("CD") })
        }
    }

    @Nested
    inner class Scrollback {
        @Test fun `scrollback limited to maxScrollbackSize`() {
            val b = TerminalBuffer(2, 2, 2)
            b.write("1\n2\n3\n4\n5\n")
            assertEquals(2, b.scrollbackSize)
        }

        @Test fun `oldest line dropped when limit exceeded`() {
            val b = TerminalBuffer(10, 1, 2)
            b.write("aaa\nbbb\nccc\n")
            assertEquals(2, b.scrollbackSize)
            assertEquals("bbb", b.getLineAsString(0).trim())
            assertEquals("ccc", b.getLineAsString(1).trim())
        }

        @Test fun `zero maxScrollbackSize never keeps history`() {
            val b = TerminalBuffer(3, 1, 0)
            b.write("aaa\nbbb\nccc\n")
            assertEquals(0, b.scrollbackSize)
        }

        @Test fun `scrollback accumulates until limit`() {
            val b = TerminalBuffer(10, 1, 10)
            b.write("aaa\nbbb\nccc\n")
            assertEquals(3, b.scrollbackSize)
            assertEquals("aaa", b.getLineAsString(0).trim())
        }
    }
}
