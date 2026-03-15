package terminal.model.enum

/**
 * Role of a cell regarding wide characters that occupy 2 columns.
 */
enum class WideCharRole {
    /** Normal cell. */
    Normal,
    /** Left cell of a wide character. */
    WideStart,
    /** Right cell of a wide character. */
    WideContinuation,
}
