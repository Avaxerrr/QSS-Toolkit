# QSS Toolkit for PyCharm

Style Sheet (QSS) support to PyCharm and other IntelliJ-based IDEs. Never switch between your IDE and external editors again when working with QSS files.

![Screenshot 2025-04-24 004434](https://github.com/user-attachments/assets/765b8f77-733c-4fb2-9cf6-dffd556bb36f)


https://github.com/user-attachments/assets/d2e1ae50-fffa-4f25-b675-f4a767112219


## Features

### Syntax Highlighting
- **Rich Highlighting** - Specialized coloring for Qt selectors, properties, values, and comments
- **Selector Recognition** - Full support for complex selectors including pseudo-elements (`::item`) and pseudo-states (`:hover`)
- **Color Preview** - Inline visualization of color values in your code

### Integrated Color Tools
- **In-Editor Color Picker** - Click on color swatches in the gutter to select colors visually
- **Real-Time Updates** - Automatically updates hex values when you select new colors
- **Instant Visual Feedback** - See your colors directly in the editor gutter

### Smart Code Completion
- **Widget Suggestions** - Quick access to Qt widget selectors (QPushButton, QMainWindow, etc.)
- **Property Completion** - Auto-completion for all standard QSS properties with descriptions
- **Context-Aware Values** - Suggests appropriate values based on the property context
- **Pseudo-States** - Complete list of available pseudo-states and pseudo-elements

### Color Palette Management
- **Project-Wide Palettes** - Create and manage color schemes for consistent styling
- **Easy Color Access** - Copy color values directly to your clipboard
- **Visual Organization** - View all your project colors in one dedicated tool window
- **Persistent Storage** - Palettes are saved with your project

### Navigation & Structure
- **Hierarchical View** - See your entire QSS file structure organized by selectors
- **Quick Navigation** - Jump directly to any selector or property with one click
- **Rule Visualization** - Understand the structure of complex stylesheets at a glance

### Code Folding
- **Selective Focus** - Collapse complex rule blocks to focus on relevant sections
- **Comment Folding** - Hide large comment blocks when not needed
- **Improved Readability** - Reduce visual clutter in large stylesheets

## Installation

### From JetBrains Marketplace
1. Open PyCharm
2. Go to Settings/Preferences → Plugins
3. Select "Marketplace" tab
4. Search for "QSS Toolkit"
5. Click "Install"
6. Restart PyCharm when prompted

### Manual Installation
1. Download the latest plugin release (.zip file) from the [Releases page](https://github.com/avaxerrr/qss-toolkit/releases)
2. Open PyCharm
3. Go to Settings/Preferences → Plugins
4. Click the gear icon and select "Install Plugin from Disk..."
5. Navigate to the downloaded .zip file and select it
6. Restart PyCharm when prompted

## Usage

### Working with QSS Files
- Create or open any file with a `.qss` extension
- Syntax highlighting and code completion will be automatically available
- The plugin requires no configuration - it works immediately upon installation

### Color Picker
1. Look for color swatches in the gutter next to hex color values (e.g., `#FF5500`)
2. Click on a swatch to open the color picker dialog
3. Select your desired color using the visual picker or by entering values
4. Click OK to automatically update the hex value in your code

### Color Palette Management
1. Open the QSS Colors tool window (View → Tool Windows → QSS Colors)
2. Click "Add Palette" to create a new color collection
3. With a palette selected, click "Add Color" to choose colors
4. Name your colors (optional) for better organization
5. Use right-click or keyboard shortcuts to copy color values to clipboard
6. Double-click on a color to quickly copy its hex value

### Structure Navigation
- Open the Structure tool window (View → Tool Windows → Structure, or Alt+7)
- Browse the hierarchical view of selectors and properties
- Click on any item to navigate directly to that part of your QSS file

### Code Folding
- Use the "-" icons in the gutter to collapse rule blocks or comments
- Press Ctrl+Shift+NumPad- to collapse all blocks in the file
- Press Ctrl+NumPad+ to expand a collapsed block
- 

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE.md) file for details.

## Contact

- GitHub: [https://github.com/avaxerrr](https://github.com/avaxerrr)
- Email: zonemaxq@gmail.com

---

*QSS Toolkit is not affiliated with JetBrains or Qt. PyCharm is a trademark of JetBrains s.r.o. Qt is a trademark of The Qt Company Ltd.*
