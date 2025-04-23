package io.github.avaxerrr.qsstoolkit.palette

import io.github.avaxerrr.qsstoolkit.palette.QssColor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

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
                    val newColor = JColorChooser.showDialog(
                        panel,
                        "Choose Color",
                        null
                    )

                    if (newColor != null) {
                        val name = Messages.showInputDialog(
                            project,
                            "Enter color name:",
                            "New Color",
                            null
                        )

                        if (!name.isNullOrBlank()) {
                            selectedPalette.addColor(QssColor(name, newColor))
                            paletteManager.updateState()
                            updateColorList()
                        }
                    }
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
                label.text = "${value.name} (${value.toHex()})"
                label.background = if (isSelected) list.selectionBackground else list.background

                panel.add(swatch, BorderLayout.WEST)
                panel.add(label, BorderLayout.CENTER)

                return panel
            }

            return label
        }
    }
}
