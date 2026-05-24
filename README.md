# QSS Toolkit

<div align="center">

**Professional Qt Style Sheet (QSS) editing for all JetBrains IDEs**

[![Version](https://img.shields.io/badge/version-2.1.1-blue.svg)](https://github.com/avaxerrr/qss-toolkit/releases)
[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange.svg)](https://plugins.jetbrains.com/)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](LICENSE.md)

*Works in CLion, PyCharm, IntelliJ IDEA, WebStorm, Rider, and all other IntelliJ Platform IDEs*

</div>

---

## Why QSS Toolkit?

Stop switching between your IDE and external editors when styling Qt applications. QSS Toolkit brings **professional Qt Style Sheet support** directly into your JetBrains IDE with intelligent completion, real-time validation, and visual color tools.

<img width="2559" height="1439" alt="Screenshot 2025-12-26 211437" src="https://github.com/user-attachments/assets/7f580ce9-3a24-46cc-b368-74d240507285"/>

---

## What's New in Version 2.1.1

- **Marketplace compatibility cleanup** - Removed deprecated IntelliJ Platform API usage reported by Plugin Verifier while keeping support for 2024.2 through 2026.2 IDE builds

## What's New in Version 2.1

- **Live Preview Tools** - Generate PySide, PyQt, or C++ Qt helper files that reload a selected `.qss` file when it is saved
- **Revamped QSS Colors panel** - Organize colors with folders, search, drag/drop, multi-select actions, import/export, and editable opacity-aware swatches
- **Editor context menu** - Use `Insert Color`, `Save Color`, and `Enable Live Preview` directly from QSS files
- **More color formats** - Added HSL/HSLA and HSV/HSVA support alongside hex, RGB, and RGBA
- **Validation fixes** - Improved handling for Qt-specific properties, `qproperty-*` declarations, border values, and Qt color functions
- **2026 IDE support** - Added compatibility for newer JetBrains IDE builds

---

## What's New in Version 2.0

### Robust Error Detection
- **Real-time validation** catches typos like `pading` or invalid values like `width: red` before you run your app
- **Smart parser recovery** isolates syntax errors (missing braces) so the rest of your file stays error-free
- **Type safety** validates measurements (px, pt), colors, and URLs to prevent silent failures
- **Widget-aware validation** warns if you use `::handle` on a `QPushButton` (invalid) vs. `QScrollBar` (valid)

### Visual Color Enhancements
- **Background color highlighting** - See actual colors displayed as backgrounds (#FF0000 shows solid red)
- **Contrast-aware text** - Text color automatically adjusts (black/white) using WCAG luminance standards for readability
- **Alpha transparency support** - Preserves original transparency from `rgba()` and `#RRGGBBAA` formats
- **Multiple color formats** - Full support for hex, RGB, and RGBA with format preservation when editing

### Template & Dynamic Values
- **First-class template support** for Jinja2/QString templates (e.g., `{{BUTTON_COLOR}}`)
- Parser correctly handles variable tags without throwing syntax errors
- Perfect for Python/C++ template engines

### Enhanced Syntax Highlighting
- **Semantic coloring** - Widgets (Teal), Properties (Blue), Keywords (Orange), Templates are visually distinct
- **Theme compatibility** - Fixed invisible colors in dark themes using reliable color key mapping
- **URL & gradient support** - Proper highlighting for `url(...)` paths and Qt gradient functions
- **Improved tokenization** - Numbers with units and CSS keywords are now visually distinct

---

## Key Features

### Complete Qt 6.10 Coverage
- All **96 QSS properties** with Qt 5/6 backward compatibility
- **50 styleable widgets** (QPushButton, QScrollBar, QComboBox, etc.)
- **82 sub-controls** (`::handle`, `::indicator`, `::drop-down`, etc.)
- **44 pseudo-states** (`:hover`, `:pressed`, `:checked`, `:disabled`, etc.)
- **Intelligent chained syntax** support (`QScrollBar::handle:vertical:hover`)

### Smart Context-Aware Completion
- Widget-specific suggestions (scrollbars show `:horizontal`/`:vertical`, checkboxes show `:checked`/`:unchecked`)
- Auto-popup triggers after `::` and `:` for effortless workflow
- Property value suggestions (CSS units, colors, alignments, border styles)
- Auto-inserts colons and spaces for faster typing

### Advanced Color Tools
- **Visual color picker** - Click gutter icons to edit colors with HSV/HSL/RGB panels
- **Gutter previews** - Color swatches with borders for transparent colors
- **Format support** - Works with hex, RGB/RGBA, HSL/HSLA, and HSV/HSVA colors
- **Gradient recognition** - Support for `qlineargradient()`, `qradialgradient()`, `qconicalgradient()`
- **QSS Colors panel** - Save, edit, organize, and insert reusable colors from the side panel or editor context menu

### Live Preview Tools
- Generate small helper files for PySide6, PyQt6, PySide2/PyQt5, or C++ Qt
- Helpers watch the selected `.qss` file and reload it in the running Qt app when saved
- No debugger hooks, process injection, or external watcher setup required
- Works after adding one setup call to your app

### Smart Editing Features
- **Auto-close brackets & quotes** - Automatic insertion of closing characters
- **Smart indentation** - Enter key creates properly formatted blocks
- **Comment toggle** - Ctrl+/ (Cmd+/) support
- **Bracket matching** - Visual highlighting of matching pairs
- **Code folding** - Collapse complex rule blocks for better focus
- **Structure navigation** - Hierarchical view of selectors and properties

### Universal IDE Support
Works in **ALL** JetBrains IDEs with **NO** language-specific dependencies:

| IDE | Use Case |
|-----|----------|
| **CLion** | C++ Qt development (no Python dependency!) |
| **PyCharm** | Python Qt (PySide6, PyQt6, PySide2, PyQt5) |
| **IntelliJ IDEA** | Java Qt (QtJambi) |
| **WebStorm** | Qt for WebAssembly projects |
| **Rider** | .NET Qt development |
| **All Others** | Any IntelliJ Platform IDE (2024.1+) |

---

## Installation

### From JetBrains Marketplace (Recommended)
1. Open your JetBrains IDE (PyCharm, CLion, IntelliJ, etc.)
2. Go to **Settings/Preferences → Plugins**
3. Select the **Marketplace** tab
4. Search for **"QSS Toolkit"**
5. Click **Install**
6. Restart your IDE when prompted

### Manual Installation
1. Download the latest `.zip` file from the [Releases page](https://github.com/avaxerrr/qss-toolkit/releases)
2. Open your IDE → **Settings/Preferences → Plugins**
3. Click the gear icon → **Install Plugin from Disk...**
4. Select the downloaded `.zip` file
5. Restart your IDE

---

## Usage Guide

### Getting Started
- Create or open any file with a `.qss` extension
- **Zero configuration required** - All features work immediately!
- Press **Ctrl+Space** for intelligent completion suggestions

### Color Picker
1. Look for color swatches in the gutter next to color values (e.g., `#FF5500`, `rgb(255, 85, 0)`)
2. Click on a swatch to open the color picker dialog
3. Select your desired color using visual picker or by entering values
4. Click **OK** to automatically update the value in your code
5. Format is preserved (hex stays hex, rgba stays rgba)

### Color Palette Management
1. Open **View → Tool Windows → QSS Colors**
2. Click **"Add Palette"** to create a new color collection
3. Click **"Add Color"** to choose colors from the picker
4. **Right-click** on colors for copy options:
   - Copy Hex Value
   - Copy QSS Format (auto-converts to rgba if transparent)
   - Copy as `color: ...;`
   - Copy as `background-color: ...;`
5. Double-click to quickly copy hex value to clipboard

### Live Preview
1. Open a `.qss` file
2. Right-click in the editor
3. Choose **QSS Toolkit → Enable Live Preview**
4. Select your framework: PySide6, PyQt6, PySide2/PyQt5, or C++ Qt
5. Add the shown setup call to your app after `QApplication` is created
6. Run your app, edit the `.qss` file, and save to reload the stylesheet

### Structure Navigation
- Open **View → Tool Windows → Structure** (or **Alt+7**)
- Browse hierarchical view of selectors and properties
- Click any item to jump directly to that code

### Smart Completion Examples
```
QScrollBar::      ← Auto-suggests: handle, add-line, sub-line, etc.
QPushButton:      ← Auto-suggests: hover, pressed, disabled, etc.
background-color: ← Auto-suggests: color names, rgb(), rgba(), hex
padding: 10       ← Auto-suggests: px, pt, em, ex
```

### Real-Time Validation
The plugin catches errors as you type:
- `pading: 10px;` → "Unknown property 'pading'. Did you mean 'padding'?"
- `width: red;` → "Invalid value type. Expected measurement with unit (px, pt)"
- `QPushButton::handle` → "'::handle' is not valid for QPushButton. Valid for: QScrollBar, QSlider"

### Code Folding
- Click "-" icons in gutter to collapse rule blocks
- **Ctrl+Shift+NumPad-** to collapse all blocks
- **Ctrl+NumPad+** to expand collapsed block

---

## Supported QSS Features

### Properties (96 total)
All standard QSS properties including:
- **Layout**: `margin`, `padding`, `spacing`, `width`, `height`
- **Colors**: `color`, `background-color`, `border-color`, `selection-color`
- **Borders**: `border`, `border-radius`, `border-style`, `border-width`
- **Fonts**: `font-family`, `font-size`, `font-weight`, `letter-spacing`, `word-spacing`
- **Advanced**: `opacity`, `outline`, `subcontrol-position`, `qproperty-*`

### Widgets (50 total)
Including: `QPushButton`, `QLabel`, `QLineEdit`, `QComboBox`, `QScrollBar`, `QCheckBox`, `QRadioButton`, `QSpinBox`, `QSlider`, `QProgressBar`, `QTabWidget`, `QTreeView`, `QTableView`, `QMenu`, `QMenuBar`, `QToolBar`, `QDockWidget`, and many more.

### Sub-Controls (82 total)
`::handle`, `::groove`, `::indicator`, `::drop-down`, `::down-arrow`, `::up-button`, `::item`, `::branch`, `::separator`, `::title`, `::close-button`, and more.

### Pseudo-States (44 total)
`:hover`, `:pressed`, `:checked`, `:unchecked`, `:disabled`, `:enabled`, `:focus`, `:selected`, `:on`, `:off`, `:open`, `:closed`, `:horizontal`, `:vertical`, and more.

---

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

### Found a Bug?
Please open an issue with:
- Your IDE version (e.g., PyCharm 2024.3, CLion 2025.1)
- Plugin version
- Steps to reproduce
- Sample QSS code (if applicable)

---

## License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](LICENSE.md) file for details.

---

## Contact

- **GitHub**: [https://github.com/avaxerrr](https://github.com/avaxerrr)
- **Email**: [zonemaxq@gmail.com](mailto:zonemaxq@gmail.com)
- **Issues**: [GitHub Issues](https://github.com/avaxerrr/qss-toolkit/issues)

---

## Acknowledgments

- Built with the [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/)
- Inspired by the Qt community's need for better IDE tooling
- Color contrast algorithm based on [WCAG 2.0 standards](https://www.w3.org/TR/WCAG20/)

---

<div align="center">

**If you find QSS Toolkit helpful, please consider starring the repository!**

*QSS Toolkit is not affiliated with JetBrains or Qt.*  
*PyCharm, CLion, IntelliJ IDEA are trademarks of JetBrains s.r.o.*  
*Qt is a trademark of The Qt Company Ltd.*

</div>
