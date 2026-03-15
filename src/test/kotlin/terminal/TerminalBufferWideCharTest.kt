package terminal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import terminal.buffer.TerminalBuffer
import terminal.model.enum.WideCharRole

/**
 * Tests for wide character support.
 */
class TerminalBufferWideCharTest {

    @Test
    fun `write wide char marks left cell as wide`() {
        val b = TerminalBuffer(6, 2, 0)
        b.write("日")
        val cell = b.getAttributes(b.scrollbackSize, 0)!!
        assertTrue(cell.wideCharRole == WideCharRole.WideStart)
        assertEquals('日', cell.char)
    }

    @Test
    fun `write wide char places continuation cell to the right`() {
        val b = TerminalBuffer(6, 2, 0)
        b.write("日")
        val cont = b.getAttributes(b.scrollbackSize, 1)!!
        assertTrue(cont.wideCharRole == WideCharRole.WideContinuation)
        assertNull(cont.char)
    }

    @Test
    fun `write wide char advances cursor by 2`() {
        val b = TerminalBuffer(6, 2, 0)
        b.write("日")
        assertEquals(2, b.getCursorColumn())
    }

    @Test
    fun `write wide char that does not fit at last column wraps to next line`() {
        val b = TerminalBuffer(5, 3, 0)
        b.write("abcd")
        b.write("日")
        val wideCell = b.getAttributes(b.scrollbackSize + 1, 0)!!
        assertTrue(wideCell.wideCharRole == WideCharRole.WideStart, "wide char should be at start of row 1")
        assertTrue(b.getAttributes(b.scrollbackSize + 1, 1)!!.wideCharRole == WideCharRole.WideContinuation)
        assertEquals(2, b.getCursorColumn())
        assertEquals(1, b.getCursorRow())
    }

    @Test
    fun `write wide char exactly filling line places cursor at start of next line`() {
        val b = TerminalBuffer(4, 3, 0)
        b.write("日本")
        assertEquals(0, b.getCursorColumn())
        assertEquals(1, b.getCursorRow())
    }

    @Test
    fun `writing narrow char over wide char clears continuation cell`() {
        val b = TerminalBuffer(6, 2, 0)
        b.write("日")
        b.setCursor(0, 0)
        b.write("X")
        val col1 = b.getAttributes(b.scrollbackSize, 1)!!
        assertFalse(col1.wideCharRole == WideCharRole.WideContinuation, "continuation cell should have been cleared")
        assertTrue(col1.wideCharRole == WideCharRole.Normal)
    }

    @Test
    fun `writing narrow char over continuation cell clears the wide char`() {
        val b = TerminalBuffer(6, 2, 0)
        b.write("日")
        b.setCursor(1, 0)
        b.write("X")
        val col0 = b.getAttributes(b.scrollbackSize, 0)!!
        assertTrue(col0.wideCharRole == WideCharRole.Normal, "orphaned wide-char cell should have been cleared")
    }

    @Test
    fun `writing wide char over another wide char replaces both cells`() {
        val b = TerminalBuffer(6, 2, 0)
        b.write("日")
        b.setCursor(0, 0)
        b.write("本")
        val left = b.getAttributes(b.scrollbackSize, 0)!!
        val right = b.getAttributes(b.scrollbackSize, 1)!!
        assertEquals('本', left.char)
        assertTrue(left.wideCharRole == WideCharRole.WideStart)
        assertTrue(right.wideCharRole == WideCharRole.WideContinuation)
    }

    @Test
    fun `setCursor on continuation cell snaps to wide char left cell`() {
        val b = TerminalBuffer(6, 2, 0)
        b.write("日")
        b.setCursor(1, 0)
        assertEquals(0, b.getCursorColumn(), "cursor should snap left to the wide char cell")
    }

    @Test
    fun `insert wide char that does not fit wraps to next line`() {
        val b = TerminalBuffer(5, 3, 0)
        b.write("abcde")
        b.setCursor(4, 0)
        b.insert("日")
        val wideCell = b.getAttributes(b.scrollbackSize + 1, 0)!!
        assertTrue(wideCell.wideCharRole == WideCharRole.WideStart, "wide char should be at start of row 1")
        assertEquals(2, b.getCursorColumn())
        assertEquals(1, b.getCursorRow())
    }

    @Test
    fun `insert wide char with non-empty overflow pushes displaced cells to next row`() {
        val b = TerminalBuffer(6, 3, 10)
        b.write("abcdef")
        b.setCursor(0, 0)
        b.insert("日")
        val row0 = b.getLineAsString(b.scrollbackSize)
        val row1 = b.getLineAsString(b.scrollbackSize + 1)
        assertTrue(row0.startsWith("日abcd"), "row 0 should start with the wide char followed by shifted content, got: $row0")
        assertTrue(row1.startsWith("ef"), "displaced cells should appear at start of row 1, got: $row1")
        assertEquals(WideCharRole.WideStart, b.getAttributes(b.scrollbackSize, 0)!!.wideCharRole)
        assertEquals(WideCharRole.WideContinuation, b.getAttributes(b.scrollbackSize, 1)!!.wideCharRole)
    }

}
