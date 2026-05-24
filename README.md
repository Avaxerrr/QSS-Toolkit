# QSS Toolkit

<div align="center">

**Professional Qt Style Sheet (QSS) editing for JetBrains IDEs**

[![Version](https://img.shields.io/badge/version-2.1-blue.svg)](https://github.com/avaxerrr/qss-toolkit/releases)
[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange.svg)](https://plugins.jetbrains.com/)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](LICENSE.md)

*Works in CLion, PyCharm, IntelliJ IDEA, WebStorm, Rider, and other IntelliJ Platform IDEs.*

</div>

---

## Why QSS Toolkit?

QSS Toolkit brings dedicated Qt Style Sheet support directly into JetBrains IDEs. It helps you write, validate, navigate, organize colors, and live-preview `.qss` changes without switching to external tools.

<img width="2559" height="1439" alt="QSS Toolkit screenshot" src="https://github.com/user-attachments/assets/7f580ce9-3a24-46cc-b368-74d240507285"/>

---

## What's New in Version 2.1

- **Live Preview Tools** - Generate PySide, PyQt, or C++ Qt helper files that reload a selected `.qss` file when it is saved.
- **Revamped QSS Colors panel** - Organize saved colors with folders, search, drag/drop, multi-select actions, import/export, and editable swatches.
- **Editor context menu** - Use `Insert Color`, `Save Color`, and `Enable Live Preview` directly from QSS files.
- **More color formats** - Added HSL/HSLA and HSV/HSVA support alongside hex, RGB, RGBA, palette colors, transparent values, and Qt gradients.
- **Validation fixes** - Improved handling for Qt-specific properties, `qproperty-*` declarations, border values, and Qt color functions.
- **IDE compatibility** - Supports JetBrains IDE builds from 2024.2 through 2026.2.

---

## Key Features

### QSS Language Support

- Syntax highlighting, parsing, code folding, structure view, comments, bracket matching, and smart editing for `.qss` files.
- Completion for Qt widgets, properties, values, sub-controls, pseudo-states, and chained selectors such as `QScrollBar::handle:vertical:hover`.
- Real-time validation for unknown properties, invalid values, incorrect units, malformed declarations, and common QSS mistakes.
- Qt-aware syntax support for `qproperty-*`, palette roles, gradients, icon properties, and template placeholders such as `{{BUTTON_COLOR}}`.

### Color Tools

- Gutter color previews for supported color values.
- Visual color picker with HSV, HSL, and RGB editing modes.
- Format-aware editing for hex, RGB/RGBA, HSL/HSLA, HSV/HSVA, palette colors, transparent values, and Qt gradients.
- QSS Colors tool window for saving, searching, renaming, organizing, importing, exporting, copying, and inserting reusable colors.

### Live Preview Tools

- Generate helper files for PySide6, PyQt6, PySide2/PyQt5, or C++ Qt.
- Helpers watch the selected `.qss` file and reload it in the running Qt app when saved.
- No debugger hooks, process injection, or external watcher setup required.
- Works after adding one setup call to your app after `QApplication` is created.

### Universal IDE Support

QSS Toolkit works in JetBrains IDEs with no language-specific dependency.

| IDE | Use Case |
|-----|----------|
| CLion | C++ Qt development |
| PyCharm | Python Qt with PySide or PyQt |
| IntelliJ IDEA | Java Qt or mixed projects |
| WebStorm | Qt for WebAssembly or frontend-adjacent projects |
| Rider | .NET Qt development |
| Other IntelliJ IDEs | Any project that uses `.qss` files |

---

## Installation

### From JetBrains Marketplace

1. Open your JetBrains IDE.
2. Go to **Settings/Preferences -> Plugins**.
3. Open the **Marketplace** tab.
4. Search for **QSS Toolkit**.
5. Click **Install**.
6. Restart the IDE if prompted.

### Manual Installation

1. Download the latest `.zip` from the [Releases page](https://github.com/avaxerrr/qss-toolkit/releases).
2. Open **Settings/Preferences -> Plugins**.
3. Click the gear icon.
4. Choose **Install Plugin from Disk...**.
5. Select the downloaded `.zip`.

---

## Usage

### Getting Started

- Create or open any `.qss` file.
- Use **Ctrl+Space** for completion.
- Use the gutter color swatches to edit colors.
- Use the Structure tool window to navigate selectors and properties.

### QSS Colors

1. Open **View -> Tool Windows -> QSS Colors**.
2. Create color folders and add reusable colors.
3. Search, rename, drag/drop, import, export, copy, or insert saved colors.
4. Use the editor context menu to insert saved colors or save selected colors without leaving the file.

### Live Preview

1. Open a `.qss` file.
2. Right-click in the editor.
3. Choose **QSS Toolkit -> Enable Live Preview**.
4. Select PySide6, PyQt6, PySide2/PyQt5, or C++ Qt.
5. Add the generated setup call to your app after `QApplication` is created.
6. Run your app, edit the `.qss` file, and save to reload the stylesheet.

---

## Supported QSS Features

- **Properties:** layout, spacing, colors, borders, fonts, outlines, icons, `qproperty-*`, and Qt-specific properties.
- **Widgets:** common Qt widgets including buttons, labels, inputs, combo boxes, scroll bars, sliders, menus, views, tabs, toolbars, docks, and more.
- **Sub-controls:** `::handle`, `::groove`, `::indicator`, `::drop-down`, `::item`, `::branch`, `::separator`, `::title`, and more.
- **Pseudo-states:** `:hover`, `:pressed`, `:checked`, `:disabled`, `:focus`, `:selected`, `:horizontal`, `:vertical`, and more.
- **Colors:** hex, RGB/RGBA, HSL/HSLA, HSV/HSVA, Qt palette colors, transparent values, and Qt gradients.

---

## Contributing

Issues and pull requests are welcome.

When reporting a bug, include:

- IDE name and version
- Plugin version
- Steps to reproduce
- Sample QSS code, if applicable

---

## License

This project is licensed under the [Apache License 2.0](LICENSE.md).

---

## Links

- GitHub: [https://github.com/avaxerrr](https://github.com/avaxerrr)
- Issues: [GitHub Issues](https://github.com/avaxerrr/qss-toolkit/issues)
- Email: [zonemaxq@gmail.com](mailto:zonemaxq@gmail.com)

---

<div align="center">

*QSS Toolkit is not affiliated with JetBrains or Qt.*

</div>
