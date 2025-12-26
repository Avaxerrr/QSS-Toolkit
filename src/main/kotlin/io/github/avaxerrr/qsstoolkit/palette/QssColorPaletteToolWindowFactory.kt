// QSS Toolkit version 1.1


package io.github.avaxerrr.qsstoolkit.palette

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.*
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JPopupMenu
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import java.awt.event.KeyEvent
import java.awt.event.KeyAdapter

class QssColorPaletteToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = QssColorPaletteToolWindowContent(project)
        val content = ContentFactory.getInstance().createContent(
            toolWindowContent.getContent(), "Color Palettes", false
        )
        toolWindow.contentManager.addContent(content)
    }
}

class QssColorPaletteToolWindowContent(private val project: Project) {
    private val paletteManager = QssColorPaletteManager.getInstance(project)
    private val panel = JPanel(BorderLayout())
    private val paletteListModel = DefaultListModel<QssColorPalette>()
    private val paletteList = JBList(paletteListModel)
    private val colorListModel = DefaultListModel<QssColor>()
    private val colorList = JBList(colorListModel)

    init {
        setupUI()
    }

    private fun setupUI() {
        // Setup palette list
        paletteList.cellRenderer = PaletteListCellRenderer()
        paletteList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        paletteList.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                updateColorList()
            }
        }

        // Setup color list
        colorList.cellRenderer = ColorListCellRenderer()
        colorList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        colorList.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                if (e.clickCount == 2) {
                    val index = colorList.locationToIndex(e.point)
                    if (index >= 0) {
                        colorList.selectedIndex = index
                        val selectedColor = colorList.selectedValue
                        if (selectedColor != null) {
                            // Copy in QSS-compatible format
                            copyToClipboard(selectedColor.toQssFormat())
                            JOptionPane.showMessageDialog(
                                panel,
                                "Copied ${selectedColor.toQssFormat()} to clipboard",
                                "Color Copied",
                                JOptionPane.INFORMATION_MESSAGE
                            )
                        }
                    }
                }
            }
        })

        colorList.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                // Check for Ctrl+C or Cmd+C on Mac
                if (e.isControlDown && e.keyCode == KeyEvent.VK_C) {
                    val selectedColor = colorList.selectedValue
                    if (selectedColor != null) {
                        // Copy in QSS-compatible format
                        copyToClipboard(selectedColor.toQssFormat())
                        e.consume()
                    }
                }
            }
        })


        // Set up the context menu
        setupContextMenu()

        // Create split pane
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
        splitPane.leftComponent = JBScrollPane(paletteList)
        splitPane.rightComponent = JBScrollPane(colorList)
        splitPane.dividerLocation = 150

        // Create button panel for palettes
        val paletteButtonPanel = JPanel()
        val addPaletteButton = JButton("Add Palette")
        val removePaletteButton = JButton("Remove Palette")

        addPaletteButton.addActionListener {
            val name = Messages.showInputDialog(
                project,
                "Enter palette name:",
                "New Color Palette",
                null
            )

            if (!name.isNullOrBlank()) {
                val palette = QssColorPalette(name)
                paletteManager.addPalette(palette)
                refreshPaletteList()
            }
        }

        removePaletteButton.addActionListener {
            val selectedPalette = paletteList.selectedValue
            if (selectedPalette != null) {
                paletteManager.removePalette(selectedPalette)
                refreshPaletteList()
            }
        }

        paletteButtonPanel.add(addPaletteButton)
        paletteButtonPanel.add(removePaletteButton)

        // Create button panel for colors
        val colorButtonPanel = JPanel()
        val addColorButton = JButton("Add Color")
        val removeColorButton = JButton("Remove Color")

        addColorButton.addActionListener {
            val selectedPalette = paletteList.selectedValue
            if (selectedPalette != null) {
                SwingUtilities.invokeLater {
                    // Create customized color chooser
                    val colorChooser = JColorChooser()

                    // Remove CMYK panel and configure tabs
                    configureColorChooserPanels(colorChooser)

                    // Show dialog with customized chooser
                    val dialog = JColorChooser.createDialog(
                        panel,
                        "Choose Color",
                        true,  // modal
                        colorChooser,
                        { actionEvent ->  // Changed from 'newColor' to 'actionEvent'
                            // OK action - get the selected color from the chooser
                            val selectedColor = colorChooser.color
                            if (selectedColor != null) {
                                // Save the selected panel for next time
                                saveSelectedPanel(colorChooser)

                                // Ask for color name
                                val name = Messages.showInputDialog(
                                    project,
                                    "Enter color name (optional):",
                                    "New Color",
                                    null
                                )

                                // Generate a default name if blank or null (canceled)
                                val colorName = if (name.isNullOrBlank()) {
                                    val hexColor = String.format("#%02X%02X%02X", selectedColor.red, selectedColor.green, selectedColor.blue)
                                    "Color $hexColor"
                                } else {
                                    name
                                }

                                selectedPalette.addColor(QssColor(colorName, selectedColor))
                                paletteManager.updateState()
                                updateColorList()
                            }
                        },
                        null  // Cancel action
                    )

                    dialog.isVisible = true
                }
            }
        }


        removeColorButton.addActionListener {
            val selectedPalette = paletteList.selectedValue
            val selectedColor = colorList.selectedValue

            if (selectedPalette != null && selectedColor != null) {
                selectedPalette.removeColor(selectedColor)
                paletteManager.updateState()
                updateColorList()
            }
        }

        colorButtonPanel.add(addColorButton)
        colorButtonPanel.add(removeColorButton)

        // Combine buttons
        val buttonPanel = JPanel(BorderLayout())
        buttonPanel.add(paletteButtonPanel, BorderLayout.WEST)
        buttonPanel.add(colorButtonPanel, BorderLayout.EAST)

        // Add components to main panel
        panel.add(splitPane, BorderLayout.CENTER)
        panel.add(buttonPanel, BorderLayout.SOUTH)

        // Initial refresh
        refreshPaletteList()
    }

    private fun configureColorChooserPanels(colorChooser: JColorChooser) {
        // Get all available panels
        val allPanels = colorChooser.chooserPanels

        // Filter out CMYK and Swatches panels - keep only HSV, HSL, RGB
        val filteredPanels = allPanels.filter { panel ->
            val displayName = panel.displayName
            // Keep only the useful panels
            displayName.contains("HSV", ignoreCase = true) ||
                    displayName.contains("HSL", ignoreCase = true) ||
                    displayName.contains("HSB", ignoreCase = true) ||
                    displayName.contains("RGB", ignoreCase = true)
        }

        // Set filtered panels back
        colorChooser.chooserPanels = filteredPanels.toTypedArray()

        // Restore last used panel or default to HSL/HSV
        restoreSelectedPanel(colorChooser)
    }

    private fun restoreSelectedPanel(colorChooser: JColorChooser) {
        val properties = PropertiesComponent.getInstance()
        val lastPanel = properties.getValue(LAST_PANEL_KEY, "HSV")

        val panels = colorChooser.chooserPanels
        val targetIndex = panels.indexOfFirst { it.displayName.equals(lastPanel, ignoreCase = true) }

        if (targetIndex >= 0) {
            // Reorder so the saved panel appears first and gets selected
            val reordered = panels.toMutableList()
            val selectedPanel = reordered.removeAt(targetIndex)
            reordered.add(0, selectedPanel)
            colorChooser.chooserPanels = reordered.toTypedArray()
        }
    }

    private fun saveSelectedPanel(colorChooser: JColorChooser) {
        // Find the JTabbedPane in the color chooser
        val tabbedPane = findTabbedPane(colorChooser)

        if (tabbedPane != null) {
            val selectedIndex = tabbedPane.selectedIndex

            // Map the selected tab index to the actual panel
            val panels = colorChooser.chooserPanels
            if (selectedIndex >= 0 && selectedIndex < panels.size) {
                val selectedPanelName = panels[selectedIndex].displayName

                // Save it to persistent storage
                val properties = PropertiesComponent.getInstance()
                properties.setValue(LAST_PANEL_KEY, selectedPanelName)
            }
        }
    }

    private fun findTabbedPane(component: java.awt.Component): javax.swing.JTabbedPane? {
        if (component is javax.swing.JTabbedPane) {
            return component
        }
        if (component is java.awt.Container) {
            for (child in component.components) {
                val result = findTabbedPane(child)
                if (result != null) return result
            }
        }
        return null
    }

    companion object {
        private const val LAST_PANEL_KEY = "qss.color.picker.last.panel"
    }


    private fun refreshPaletteList() {
        paletteListModel.clear()
        paletteManager.getAllPalettes().forEach { paletteListModel.addElement(it) }
    }

    private fun updateColorList() {
        colorListModel.clear()
        val selectedPalette = paletteList.selectedValue
        selectedPalette?.getAllColors()?.forEach { colorListModel.addElement(it) }
    }

    fun getContent(): JComponent = panel

    private class PaletteListCellRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            if (value is QssColorPalette) {
                text = value.name
            }
            return component
        }
    }

    private class ColorListCellRenderer : ListCellRenderer<QssColor> {
        private val renderer = DefaultListCellRenderer()

        override fun getListCellRendererComponent(
            list: JList<out QssColor>,
            value: QssColor?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            val label = renderer.getListCellRendererComponent(
                list, value?.name, index, isSelected, cellHasFocus
            ) as JLabel

            if (value != null) {
                // Create a panel to hold the color swatch and label
                val panel = JPanel(BorderLayout(5, 0))
                panel.background = if (isSelected) list.selectionBackground else list.background

                // Create color swatch
                val swatch = JPanel()
                swatch.background = value.value
                swatch.preferredSize = Dimension(16, 16)
                swatch.border = BorderFactory.createLineBorder(Color.BLACK)

                // Set up label
                label.text = "${value.name} (${value.toHex()}) - Right-click to copy"
                label.background = if (isSelected) list.selectionBackground else list.background

                panel.add(swatch, BorderLayout.WEST)
                panel.add(label, BorderLayout.CENTER)

                return panel
            }

            return label
        }
    }


    private fun setupContextMenu() {
        val menu = JPopupMenu()

        val copyHexItem = JMenuItem("Copy Hex Value")
        copyHexItem.addActionListener {
            val selectedColor = colorList.selectedValue
            if (selectedColor != null) {
                copyToClipboard(selectedColor.toHex())
            }
        }
        menu.add(copyHexItem)

        // NEW: Copy in QSS-compatible format (rgba if transparent, hex if opaque)
        val copyQssFormatItem = JMenuItem("Copy QSS Format")
        copyQssFormatItem.addActionListener {
            val selectedColor = colorList.selectedValue
            if (selectedColor != null) {
                copyToClipboard(selectedColor.toQssFormat())
            }
        }
        menu.add(copyQssFormatItem)

        val copyQssPropertyItem = JMenuItem("Copy as QSS Color Property")
        copyQssPropertyItem.addActionListener {
            val selectedColor = colorList.selectedValue
            if (selectedColor != null) {
                copyToClipboard("color: ${selectedColor.toQssFormat()};")
            }
        }
        menu.add(copyQssPropertyItem)

        val copyQssBackgroundItem = JMenuItem("Copy as QSS Background Property")
        copyQssBackgroundItem.addActionListener {
            val selectedColor = colorList.selectedValue
            if (selectedColor != null) {
                copyToClipboard("background-color: ${selectedColor.toQssFormat()};")
            }
        }
        menu.add(copyQssBackgroundItem)

        // Add the context menu to the color list
        colorList.componentPopupMenu = menu
    }

    // Utility method for copying text
    private fun copyToClipboard(text: String) {
        val selection = StringSelection(text)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(selection, null)
    }
}
