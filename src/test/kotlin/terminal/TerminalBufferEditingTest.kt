package terminal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import terminal.buffer.TerminalBuffer
import terminal.model.enum.Style
import terminal.model.enum.TerminalColor

/**
 * Editing operations.
 */
class TerminalBufferEditingTest {

    @Nested
    inner class Write {
        @Test fun `write advances cursor`() {
            val b = TerminalBuffer(10, 5, 10)
            b.write("ab")
            assertEquals(2, b.getCursorColumn())
            assertEquals(0, b.getCursorRow())
        }

        @Test fun `write at last column wraps to next line`() {
            val b = TerminalBuffer(3, 3, 10)
            b.write("abc")
            assertEquals(0, b.getCursorColumn())
            assertEquals(1, b.getCursorRow())
        }

        @Test fun `write newline moves to start of next line`() {
            val b = TerminalBuffer(10, 5, 10)
            b.write("hi\n")
            assertEquals(0, b.getCursorColumn())
            assertEquals(1, b.getCursorRow())
        }
    }

    @Nested
    inner class WriteOnLine {
        @Test fun `writeOnLine replaces existing content on the row`() {
            val b = TerminalBuffer(5, 3, 10)
            b.write("hello")
            b.writeOnLine(0, "xyz")
            assertEquals("xyz  ", b.getLineAsString(b.scrollbackSize))
        }

        @Test fun `writeOnLine moves cursor to end of written text`() {
            val b = TerminalBuffer(8, 3, 10)
            b.writeOnLine(0, "abc")
            assertEquals(3, b.getCursorColumn())
            assertEquals(0, b.getCursorRow())
        }

        @Test fun `writeOnLine applies current foreground background and styles`() {
            val b = TerminalBuffer(5, 3, 10)
            b.setForeground(TerminalColor.Cyan)
            b.setBackground(TerminalColor.Yellow)
            b.setStyles(setOf(Style.Bold, Style.Italic))
            b.writeOnLine(1, "x")
            val cell = b.getAttributes(b.scrollbackSize + 1, 0)!!
            assertEquals(TerminalColor.Cyan, cell.foreground)
            assertEquals(TerminalColor.Yellow, cell.background)
            assertTrue(cell.styles.contains(Style.Bold))
            assertTrue(cell.styles.contains(Style.Italic))
        }
    }

    @Nested
    inner class Insert {
        @Test fun `insert shifts existing content and advances cursor`() {
            val b = TerminalBuffer(10, 3, 10)
            b.write("abc")
            b.setCursor(1, 0)
            b.insert("X")
            assertTrue(b.getLineAsString(b.scrollbackSize).startsWith("aXbc"))
            assertEquals(2, b.getCursorColumn())
            assertEquals(0, b.getCursorRow())
        }

        @Test fun `insert newline moves cursor to next line`() {
            val b = TerminalBuffer(10, 3, 10)
            b.setCursor(2, 0)
            b.insert("\n")
            assertEquals(0, b.getCursorColumn())
            assertEquals(1, b.getCursorRow())
        }

        @Test fun `insert multiple chars shifts all content correctly`() {
            val b = TerminalBuffer(8, 2, 10)
            b.write("abcde")
            b.setCursor(2, 0)
            b.insert("XY")
            assertTrue(b.getLineAsString(b.scrollbackSize).startsWith("abXYcd"))
        }
    }

    @Nested
    inner class FillLine {
        @Test fun `fillLine fills given screen row with current attributes`() {
            val b = TerminalBuffer(5, 3, 10)
            b.setForeground(TerminalColor.Green)
            b.fillLine(1, 'x')
            assertEquals("xxxxx", b.getLineAsString(b.scrollbackSize + 1))
            assertEquals(TerminalColor.Green, b.getAttributes(b.scrollbackSize + 1, 0)!!.foreground)
        }

        @Test fun `fillLine does not move cursor`() {
            val b = TerminalBuffer(5, 3, 10)
            b.setCursor(2, 1)
            b.fillLine(0, 'a')
            assertEquals(2, b.getCursorColumn())
            assertEquals(1, b.getCursorRow())
        }
    }

    @Nested
    inner class ScreenEditing {

        @Test fun `clearScreen clears content and resets cursor`() {
            val b = TerminalBuffer(5, 3, 10)
            b.write("hello")
            b.clearScreen()
            assertEquals("     \n     \n     ", b.getScreenContent())
            assertEquals(0, b.getCursorColumn())
            assertEquals(0, b.getCursorRow())
        }

        @Test fun `clearAll clears scrollback too`() {
            val b = TerminalBuffer(2, 2, 100)
            b.write("ABCD")
            assertEquals(1, b.scrollbackSize)
            b.clearAll()
            assertEquals(0, b.scrollbackSize)
            assertEquals("  \n  ", b.getScreenContent())
        }
    }
}
