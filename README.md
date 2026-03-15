# Terminal Text Buffer

This project implements a grid of cells (character, colors, styles) with a fixed screen, scrollback, cursor, and edit operations.

---

## Run

```bash
docker build -t terminal-buffer .
docker run --rm -it terminal-buffer
```

## Build and test

```bash
docker build -t terminal-buffer .
docker run --rm terminal-buffer gradle test
```

---

## Demo commands

Type a line and press Enter. Lines not starting with `/` are echoed into the buffer.

| Command | Description |
|---------|-------------|
| `/help`, `/h` | Show help and command list |
| `/quit`, `/q` | Exit |
| `/clear` | Clear screen and show banner |
| `/color <name>` | Foreground color |
| `/background <name>` | Background color |
| `/style <name>` | `bold`, `italic`, `underline`; `off` to reset |
| `/cursor <name>` | Shape: `block`, `underline`, `bar`; visibility: `hide`, `show`; blink: `blink`, `noblink` |
| `/scroll [on\|off]` | `on`: ↑/↓ scroll buffer; `off` (default): ↑/↓ = command history when line is empty |
| `/scrollback <N>` | Max scrollback size in lines |
| `/resize W H` | Resize terminal (e.g. `/resize 80 24`) |
| `/save [name]` | Save current config (default name: `default`) |
| `/select <name>` | Apply a saved config |

### Colors (for `/color` and `/background`)

`default`, `black`, `red`, `green`, `blue`, `yellow`, `cyan`, `magenta`, `purple`, `white`, `bright_black`, `bright_red`, `bright_green`, `bright_yellow`, `bright_blue`, `bright_magenta`, `bright_purple`, `bright_cyan`, `bright_white`.

