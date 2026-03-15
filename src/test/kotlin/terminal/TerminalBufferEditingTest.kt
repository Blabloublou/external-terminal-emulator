package terminal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import terminal.buffer.TerminalBuffer
import terminal.model.enum.Style
import terminal.model.enum.TerminalColor

/**
 * Editing operations: write (overwrite at cursor, newline, wrap, scroll), writeOnLine,
 * insert (shift/ripple), fillLine, insertLineAtBottom, clearScreen, clearAll.
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

        @Test fun `write newline at last row scrolls`() {
            val b = TerminalBuffer(3, 2, 100)
            b.write("11\n22\n")
            assertEquals(0, b.getCursorColumn())
            assertEquals(1, b.getCursorRow())
            assertEquals(1, b.scrollbackSize)
            assertEquals("11 ", b.getLineAsString(0))
            assertEquals("22 ", b.getLineAsString(1))
        }

        @Test fun `write past last column scrolls when filling row`() {
            val b = TerminalBuffer(2, 2, 100)
            b.write("ABCD")
            assertEquals(0, b.getCursorColumn())
            assertEquals(1, b.getCursorRow())
            assertEquals(1, b.scrollbackSize)
            assertEquals("AB", b.getLineAsString(0))
            assertEquals("CD", b.getLineAsString(1))
        }

        @Test fun `cursor stays within screen bounds after write`() {
            val b = TerminalBuffer(5, 3, 100)
            b.write("hello world\nfoo\nbar\nbaz\nqux\n")
            assertTrue(b.getCursorColumn() in 0 until b.width)
            assertTrue(b.getCursorRow() in 0 until b.height)
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

        @Test fun `writeOnLine clears remainder of line beyond text`() {
            val b = TerminalBuffer(6, 2, 10)
            b.write("abcdef")
            b.writeOnLine(0, "hi")
            assertEquals("hi    ", b.getLineAsString(b.scrollbackSize))
        }

        @Test fun `writeOnLine with row out of bounds clamps to last row`() {
            val b = TerminalBuffer(4, 3, 10)
            b.writeOnLine(99, "zz")
            assertEquals("zz  ", b.getLineAsString(b.scrollbackSize + 2))
            assertEquals(2, b.getCursorRow())
        }

        @Test fun `writeOnLine with negative row clamps to first row`() {
            val b = TerminalBuffer(4, 3, 10)
            b.writeOnLine(-1, "hi")
            assertEquals("hi  ", b.getLineAsString(b.scrollbackSize))
            assertEquals(0, b.getCursorRow())
        }

        @Test fun `writeOnLine with empty string clears the entire row`() {
            val b = TerminalBuffer(4, 2, 10)
            b.write("abcd")
            b.writeOnLine(0, "")
            assertEquals("    ", b.getLineAsString(b.scrollbackSize))
            assertEquals(0, b.getCursorColumn())
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

        @Test fun `insert at start of full line ripples overflow to next line`() {
            val b = TerminalBuffer(4, 2, 10)
            b.write("abcd")
            b.setCursor(0, 0)
            b.insert("X")
            assertEquals("Xabc", b.getLineAsString(b.scrollbackSize))
            assertTrue(b.getLineAsString(b.scrollbackSize + 1).startsWith("d"))
        }

        @Test fun `insert ripple propagates across multiple lines`() {
            val b = TerminalBuffer(3, 3, 10)
            b.write("abcdefghi")
            b.setCursor(0, 0)
            b.insert("Z")
            assertEquals("Zde", b.getLineAsString(b.scrollbackSize))
            assertEquals("fgh", b.getLineAsString(b.scrollbackSize + 1))
            assertTrue(b.getLineAsString(b.scrollbackSize + 2).startsWith("i"))
        }

        @Test fun `insert on full screen with overflow scrolls top line to scrollback`() {
            val b = TerminalBuffer(2, 2, 10)
            b.write("abcd")
            val scrollBefore = b.scrollbackSize
            b.setCursor(0, 0)
            b.insert("X")
            assertTrue(b.scrollbackSize >= scrollBefore)
            assertTrue(b.getCursorColumn() in 0 until b.width)
            assertTrue(b.getCursorRow() in 0 until b.height)
        }

        @Test fun `insert at end of line shifts nothing and advances cursor`() {
            val b = TerminalBuffer(5, 2, 10)
            b.write("abc")
            b.insert("Z")
            assertEquals("abcZ ", b.getLineAsString(b.scrollbackSize))
            assertEquals(4, b.getCursorColumn())
            assertEquals(0, b.getCursorRow())
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

        @Test fun `fillLine with null uses empty cell`() {
            val b = TerminalBuffer(3, 2, 10)
            b.write("abc")
            b.fillLine(0, null)
            assertEquals("   ", b.getLineAsString(b.scrollbackSize))
        }

        @Test fun `fillLine clamps row to screen bounds`() {
            val b = TerminalBuffer(3, 2, 10)
            b.fillLine(10, 'z')
            assertEquals("zzz", b.getLineAsString(b.scrollbackSize + 1))
        }
    }

    @Nested
    inner class ScreenEditing {
        @Test fun `insertLineAtBottom pushes top line to scrollback`() {
            val b = TerminalBuffer(5, 2, 100)
            b.write("top ")
            b.insertLineAtBottom()
            assertEquals(1, b.scrollbackSize)
            assertEquals("top  ", b.getLineAsString(0))
            assertEquals("     ", b.getLineAsString(1))
        }

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
