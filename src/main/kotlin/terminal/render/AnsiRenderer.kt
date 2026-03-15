package terminal.render

import terminal.buffer.TerminalBuffer
import terminal.model.Cell
import terminal.model.enum.CursorShape
import terminal.model.enum.Style
import terminal.model.enum.TerminalColor
import terminal.model.enum.WideCharRole

/**
 * Renders the terminal buffer to a string with ANSI escape codes
 */
object AnsiRenderer {
    private const val RESET = "\u001b[0m"

    private fun fgCode(color: TerminalColor): String? = when (color) {
        TerminalColor.Default -> null
        TerminalColor.Black -> "30"
        TerminalColor.Red -> "31"
        TerminalColor.Green -> "32"
        TerminalColor.Yellow -> "33"
        TerminalColor.Blue -> "34"
        TerminalColor.Magenta -> "35"
        TerminalColor.Cyan -> "36"
        TerminalColor.White -> "37"
        TerminalColor.BrightBlack -> "90"
        TerminalColor.BrightRed -> "91"
        TerminalColor.BrightGreen -> "92"
        TerminalColor.BrightYellow -> "93"
        TerminalColor.BrightBlue -> "94"
        TerminalColor.BrightMagenta -> "95"
        TerminalColor.BrightCyan -> "96"
        TerminalColor.BrightWhite -> "97"
    }

    private fun bgCode(color: TerminalColor): String? = when (color) {
        TerminalColor.Default -> null
        TerminalColor.Black -> "40"
        TerminalColor.Red -> "41"
        TerminalColor.Green -> "42"
        TerminalColor.Yellow -> "43"
        TerminalColor.Blue -> "44"
        TerminalColor.Magenta -> "45"
        TerminalColor.Cyan -> "46"
        TerminalColor.White -> "47"
        TerminalColor.BrightBlack -> "100"
        TerminalColor.BrightRed -> "101"
        TerminalColor.BrightGreen -> "102"
        TerminalColor.BrightYellow -> "103"
        TerminalColor.BrightBlue -> "104"
        TerminalColor.BrightMagenta -> "105"
        TerminalColor.BrightCyan -> "106"
        TerminalColor.BrightWhite -> "107"
    }

    private fun styleCodes(styles: Set<Style>): List<String> = styles.mapNotNull {
        when (it) {
            Style.Bold -> "1"
            Style.Italic -> "3"
            Style.Underline -> "4"
        }
    }

    private fun renderLineInto(
        buffer: TerminalBuffer,
        lineIndex: Int,
        screenRow: Int,
        sb: StringBuilder,
        cursorRow: Int,
        cursorCol: Int,
        cursorChar: Char,
    ) {
        var lastFg: TerminalColor? = null
        var lastBg: TerminalColor? = null
        var lastStyles: Set<Style>? = null

        for (col in 0 until buffer.width) {
            val cell = buffer.getAttributes(lineIndex, col) ?: Cell.EMPTY
            val charToShow = if (cell.wideCharRole == WideCharRole.WideContinuation) ' ' else cell.charForDisplay()

            if (cell.wideCharRole != WideCharRole.WideContinuation && screenRow == cursorRow && col == cursorCol) {
                sb.append(RESET).append("\u001b[7m").append(cursorChar).append(RESET)
                lastFg = null; lastBg = null; lastStyles = null
                continue
            }

            val fg = cell.foreground
            val bg = cell.background
            val styles = cell.styles
            val isDefaultCell = fg == TerminalColor.Default && bg == TerminalColor.Default && styles.isEmpty()
            val hadFormatting = (lastFg != null && lastFg != TerminalColor.Default) ||
                (lastBg != null && lastBg != TerminalColor.Default) ||
                (lastStyles != null && lastStyles.isNotEmpty())

            if (isDefaultCell && hadFormatting) {
                sb.append(RESET)
                lastFg = null; lastBg = null; lastStyles = null
            } else {
                val codes = mutableListOf<String>()
                if (fg != lastFg) fgCode(fg)?.let { codes.add(it) }
                if (bg != lastBg) bgCode(bg)?.let { codes.add(it) }
                if (styles != lastStyles) styleCodes(styles).forEach { codes.add(it) }
                lastFg = fg; lastBg = bg; lastStyles = styles
                if (codes.isNotEmpty()) sb.append("\u001b[").append(codes.joinToString(";")).append("m")
            }
            sb.append(charToShow)
        }
    }

    fun renderScreen(buffer: TerminalBuffer): String =
        renderScreenInternal(buffer, cursorRow = -1, cursorCol = -1)

    fun renderLine(
        buffer: TerminalBuffer,
        row: Int,
        cursorRow: Int = -1,
        cursorCol: Int = -1,
        cursorChar: Char = '▌',
    ): String {
        val sb = StringBuilder()
        val lineIndex = buffer.scrollbackSize + row.coerceIn(0, buffer.height - 1)
        renderLineInto(buffer, lineIndex, row, sb, cursorRow, cursorCol, cursorChar)
        return sb.append(RESET).toString()
    }

    fun renderScreenWithCursor(
        buffer: TerminalBuffer,
        cursorChar: Char? = null,
        showCursorNow: Boolean? = null,
    ): String {
        val style = buffer.getCursorStyle()
        val actuallyShow = showCursorNow ?: style.visible
        if (!actuallyShow) return renderScreenInternal(buffer, -1, -1, ' ')
        val char = cursorChar ?: when (style.shape) {
            CursorShape.Block -> '▌'
            CursorShape.Underline -> '▁'
            CursorShape.Bar -> '|'
        }
        return renderScreenInternal(buffer, buffer.getCursorRow(), buffer.getCursorColumn(), char)
    }

    private fun renderScreenInternal(
        buffer: TerminalBuffer,
        cursorRow: Int,
        cursorCol: Int,
        cursorChar: Char = '▌',
    ): String {
        val sb = StringBuilder()
        val scrollbackSize = buffer.scrollbackSize

        for (row in 0 until buffer.height) {
            val lineIndex = scrollbackSize + row
            renderLineInto(buffer, lineIndex, row, sb, cursorRow, cursorCol, cursorChar)
            sb.append(RESET).append("\n")
        }
        return sb.append(RESET).toString()
    }

    fun renderLines(buffer: TerminalBuffer, fromLine: Int, maxLines: Int): String {
        val sb = StringBuilder()
        val total = buffer.totalLineCount

        for (lineIndex in fromLine until minOf(fromLine + maxLines, total)) {
            val screenRow = lineIndex - buffer.scrollbackSize
            renderLineInto(buffer, lineIndex, screenRow, sb, -1, -1, ' ')
            sb.append(RESET).append("\n")
        }
        return sb.append(RESET).toString()
    }
}
