package io.github.avaxerrr.qsstoolkit.palette

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import io.github.avaxerrr.qsstoolkit.QssIcons
import io.github.avaxerrr.qsstoolkit.ui.QssColorSwatchIcon
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractAction
import javax.swing.DropMode
import javax.swing.JButton
import javax.swing.JColorChooser
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.KeyStroke
import javax.swing.JTabbedPane
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

class QssColorPaletteToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = QssColorPaletteToolWindowContent(project)
        val content = ContentFactory.getInstance().createContent(
            toolWindowContent.getContent(), "Color Folders", false
        )
        content.setDisposer(toolWindowContent)
        toolWindow.contentManager.addContent(content)
    }
}

class QssColorPaletteToolWindowContent(private val project: Project) : Disposable {
    private val paletteManager = QssColorPaletteManager.getInstance(project)
    private val panel = JPanel(BorderLayout())
    private val rootNode = DefaultMutableTreeNode(ROOT_LABEL)
    private val treeModel = PaletteTreeModel(rootNode, paletteManager)
    private val tree = object : Tree(treeModel) {
        override fun isWideSelection(): Boolean = false
    }
    private val addColorButton = JButton("Add Color")
    private val removeButton = JButton("Remove")
    private val renameButton = JButton("Rename")
    private val paletteChangeSubscription = paletteManager.addChangeListener {
        SwingUtilities.invokeLater {
            refreshTree(getSelectionAnchorItem())
        }
    }

    init {
        setupUI()
    }

    private fun setupUI() {
        setupTree()

        val buttonPanel = JPanel()
        val addPaletteButton = JButton("Add Folder")

        addPaletteButton.addActionListener {
            val palette = paletteManager.createPalette()
            refreshTree(palette)
        }

        addColorButton.addActionListener {
            getSelectedPalette()?.let { palette ->
                showColorChooser(palette)
            }
        }

        renameButton.addActionListener {
            startRenamingSelectedNode()
        }

        removeButton.addActionListener {
            removeSelectedNode()
        }

        buttonPanel.add(addPaletteButton)
        buttonPanel.add(addColorButton)
        buttonPanel.add(renameButton)
        buttonPanel.add(removeButton)

        panel.add(JBScrollPane(tree), BorderLayout.CENTER)
        panel.add(buttonPanel, BorderLayout.SOUTH)

        refreshTree()
    }

    private fun setupTree() {
        tree.isRootVisible = false
        tree.showsRootHandles = true
        tree.isEditable = true
        tree.toggleClickCount = 0
        tree.cellRenderer = PaletteTreeCellRenderer()
        tree.selectionModel.selectionMode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
        tree.dragEnabled = true
        tree.dropMode = DropMode.ON_OR_INSERT
        tree.transferHandler = PaletteTreeTransferHandler(paletteManager) { selectedItem ->
            refreshTree(selectedItem)
        }
        tree.toolTipText = ""
        tree.inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx),
            COPY_SELECTED_COLOR_ACTION
        )
        tree.actionMap.put(COPY_SELECTED_COLOR_ACTION, object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                getSingleSelectedColor()?.let { color ->
                    copyToClipboard(color.toQssFormat())
                }
            }
        })

        tree.addTreeSelectionListener {
            updateButtonState()
        }

        tree.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                handlePopup(e)
            }

            override fun mouseReleased(e: MouseEvent) {
                handlePopup(e)
            }

            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(e) && e.clickCount == 1) {
                    handleColorSwatchClick(e)
                } else if (SwingUtilities.isLeftMouseButton(e) && e.clickCount == 2) {
                    handleDoubleClick(e)
                }
            }
        })
    }

    private fun showColorChooser(palette: QssColorPalette) {
        showColorChooser(palette, null)
    }

    private fun showColorChooser(palette: QssColorPalette, color: QssColor?) {
        SwingUtilities.invokeLater {
            val colorChooser = JColorChooser()
            if (color != null) {
                colorChooser.color = color.value
            }
            configureColorChooserPanels(colorChooser)

            val dialog = JColorChooser.createDialog(
                panel,
                if (color == null) "Choose Color" else "Edit Color",
                true,
                colorChooser,
                {
                    val selectedColor = colorChooser.color
                    if (selectedColor != null) {
                        saveSelectedPanel(colorChooser)
                        val selectedItem = if (color == null) {
                            paletteManager.addColor(palette, selectedColor)
                        } else if (paletteManager.updateColor(palette, color, selectedColor)) {
                            color
                        } else {
                            null
                        }
                        if (selectedItem != null) {
                            refreshTree(selectedItem)
                        }
                    }
                },
                null
            )

            dialog.isVisible = true
        }
    }

    private fun configureColorChooserPanels(colorChooser: JColorChooser) {
        val filteredPanels = colorChooser.chooserPanels.filter { panel ->
            val displayName = panel.displayName
            displayName.contains("HSV", ignoreCase = true) ||
                displayName.contains("HSL", ignoreCase = true) ||
                displayName.contains("HSB", ignoreCase = true) ||
                displayName.contains("RGB", ignoreCase = true)
        }

        colorChooser.chooserPanels = filteredPanels.toTypedArray()
        restoreSelectedPanel(colorChooser)
    }

    private fun restoreSelectedPanel(colorChooser: JColorChooser) {
        val properties = PropertiesComponent.getInstance()
        val lastPanel = properties.getValue(LAST_PANEL_KEY, "HSV")

        val panels = colorChooser.chooserPanels
        val targetIndex = panels.indexOfFirst { it.displayName.equals(lastPanel, ignoreCase = true) }

        if (targetIndex >= 0) {
            val reordered = panels.toMutableList()
            val selectedPanel = reordered.removeAt(targetIndex)
            reordered.add(0, selectedPanel)
            colorChooser.chooserPanels = reordered.toTypedArray()
        }
    }

    private fun saveSelectedPanel(colorChooser: JColorChooser) {
        val tabbedPane = findTabbedPane(colorChooser) ?: return
        val selectedIndex = tabbedPane.selectedIndex
        val panels = colorChooser.chooserPanels

        if (selectedIndex >= 0 && selectedIndex < panels.size) {
            PropertiesComponent.getInstance().setValue(LAST_PANEL_KEY, panels[selectedIndex].displayName)
        }
    }

    private fun findTabbedPane(component: Component): JTabbedPane? {
        if (component is JTabbedPane) {
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

    private fun refreshTree(selectedItem: Any? = null) {
        rootNode.removeAllChildren()

        for (palette in paletteManager.getAllPalettes()) {
            val paletteNode = DefaultMutableTreeNode(palette)
            for (color in palette.getAllColors()) {
                paletteNode.add(DefaultMutableTreeNode(color))
            }
            rootNode.add(paletteNode)
        }

        treeModel.reload()
        expandAllNodes()
        selectItem(selectedItem)
        updateButtonState()
    }

    private fun expandAllNodes() {
        for (row in 0 until tree.rowCount) {
            tree.expandRow(row)
        }
    }

    private fun selectItem(item: Any?) {
        if (item == null) return

        val node = findNode(item) ?: return
        tree.selectionPath = TreePath(node.path)
        tree.scrollPathToVisible(tree.selectionPath)
    }

    private fun findNode(item: Any?): DefaultMutableTreeNode? {
        val nodes = rootNode.breadthFirstEnumeration()
        while (nodes.hasMoreElements()) {
            val node = nodes.nextElement() as? DefaultMutableTreeNode ?: continue
            if (node.userObject === item) {
                return node
            }
        }
        return null
    }

    private fun updateButtonState() {
        val selectedNodes = getSelectedNodes()
        addColorButton.isEnabled = getSelectedPalette() != null
        renameButton.isEnabled = selectedNodes.size == 1
        removeButton.isEnabled = canRemoveSelection()
    }

    private fun handlePopup(e: MouseEvent) {
        if (!e.isPopupTrigger) return

        val path = tree.getPathForLocation(e.x, e.y)
        if (path != null) {
            if (!tree.isPathSelected(path)) {
                tree.selectionPath = path
            }
        } else {
            tree.clearSelection()
        }

        createContextMenu().show(tree, e.x, e.y)
    }

    private fun handleDoubleClick(e: MouseEvent) {
        val path = tree.getPathForLocation(e.x, e.y) ?: return
        tree.selectionPath = path

        when (val value = path.nodeUserObject()) {
            is QssColorPalette -> tree.startEditingAtPath(path)
            is QssColor -> copyToClipboard(value.toQssFormat())
        }
    }

    private fun handleColorSwatchClick(e: MouseEvent) {
        val path = tree.getPathForLocation(e.x, e.y) ?: return
        val rowBounds = tree.getPathBounds(path) ?: return
        if (e.x < rowBounds.x || e.x > rowBounds.x + COLOR_SWATCH_HIT_WIDTH) return

        val color = path.nodeUserObject() as? QssColor ?: return
        val palette = (path.lastPathComponent as? DefaultMutableTreeNode)?.parentPalette() ?: return
        tree.selectionPath = path
        showColorChooser(palette, color)
    }

    private fun createContextMenu(): JPopupMenu {
        val menu = JPopupMenu()
        val selectedNodes = getSelectedNodes()
        val selectedValue = selectedNodes.singleOrNull()?.userObject

        if (selectedValue is QssColorPalette) {
            menu.add(JMenuItem("Add Color").apply {
                addActionListener {
                    showColorChooser(selectedValue)
                }
            })
        }

        if (selectedNodes.size == 1 && selectedValue is QssColor) {
            menu.add(JMenuItem("Edit Color").apply {
                addActionListener {
                    val palette = selectedNodes.single().parentPalette() ?: return@addActionListener
                    showColorChooser(palette, selectedValue)
                }
            })
        }

        if (selectedNodes.size == 1 && (selectedValue is QssColorPalette || selectedValue is QssColor)) {
            menu.addSeparator()
            menu.add(JMenuItem("Rename").apply {
                addActionListener {
                    startRenamingSelectedNode()
                }
            })
            menu.add(JMenuItem("Delete").apply {
                addActionListener {
                    removeSelectedNode()
                }
            })
        } else if (selectedNodes.isNotEmpty() && getSelectedPalettes().isEmpty() && getSelectedColorSelections().isNotEmpty()) {
            menu.add(JMenuItem("Delete Selected Colors").apply {
                addActionListener {
                    removeSelectedNode()
                }
            })
        }

        if (selectedValue is QssColor) {
            menu.addSeparator()
            addCopyItem(menu, "Copy Value") { selectedValue.toQssFormat() }

            val formatMenu = JMenu("Copy Format")
            for (format in copyFormatsFor(selectedValue)) {
                addCopyItem(formatMenu, "Copy ${format.label}") {
                    QssColorFormats.format(selectedValue.value, format)
                }
            }
            menu.add(formatMenu)
        }

        return menu
    }

    private fun copyFormatsFor(color: QssColor): List<QssColorFormat> {
        return QssColorFormats.explicitFormatsFor(color.value)
    }

    private fun addCopyItem(menu: JComponent, label: String, valueProvider: () -> String) {
        menu.add(JMenuItem(label).apply {
            addActionListener {
                copyToClipboard(valueProvider())
            }
        })
    }

    private fun startRenamingSelectedNode() {
        tree.selectionPaths?.singleOrNull()?.let { path ->
            tree.startEditingAtPath(path)
        }
    }

    private fun removeSelectedNode() {
        val selectedNodes = getSelectedNodes()
        if (selectedNodes.isEmpty()) return

        val selectedPalettes = getSelectedPalettes()
        val selectedColors = getSelectedColorSelections()

        if (selectedPalettes.size == 1 && selectedColors.isEmpty()) {
            if (!confirmRemoval("Delete folder '${selectedPalettes.single().name}' and all colors in it?")) {
                return
            }

            paletteManager.removePalette(selectedPalettes.single())
            refreshTree()
            return
        }

        if (selectedPalettes.isEmpty() && selectedColors.isNotEmpty()) {
            if (selectedColors.size > 1 && !confirmRemoval("Delete ${selectedColors.size} selected colors?")) {
                return
            }

            val fallbackSelection = selectedColors.firstOrNull()?.palette
            paletteManager.removeColors(selectedColors)
            refreshTree(fallbackSelection)
        }
    }

    private fun getSelectedNodes(): List<DefaultMutableTreeNode> {
        return tree.selectionPaths
            ?.mapNotNull { it.lastPathComponent as? DefaultMutableTreeNode }
            ?: emptyList()
    }

    private fun getSelectionAnchorItem(): Any? {
        return getSelectedNodes().firstOrNull()?.userObject
    }

    private fun getSelectedPalettes(): List<QssColorPalette> {
        return getSelectedNodes().mapNotNull { it.userObject as? QssColorPalette }
    }

    private fun getSelectedColorSelections(): List<QssColorPaletteManager.ColorSelection> {
        return getSelectedNodes().mapNotNull { node ->
            val color = node.userObject as? QssColor ?: return@mapNotNull null
            val palette = node.parentPalette() ?: return@mapNotNull null
            QssColorPaletteManager.ColorSelection(palette, color)
        }
    }

    private fun getSingleSelectedColor(): QssColor? {
        return getSelectedNodes().singleOrNull()?.userObject as? QssColor
    }

    private fun getSelectedPalette(): QssColorPalette? {
        val selectedNodes = getSelectedNodes()
        if (selectedNodes.isEmpty()) return null

        val selectedPalette = selectedNodes.singleOrNull()?.userObject as? QssColorPalette
        if (selectedPalette != null) return selectedPalette

        val selectedColorParents = selectedNodes
            .filter { it.userObject is QssColor }
            .mapNotNull { it.parentPalette() }
            .distinctBy { System.identityHashCode(it) }

        return selectedColorParents.singleOrNull()
    }

    private fun canRemoveSelection(): Boolean {
        val selectedPalettes = getSelectedPalettes()
        val selectedColors = getSelectedColorSelections()
        return (selectedPalettes.size == 1 && selectedColors.isEmpty()) ||
            (selectedPalettes.isEmpty() && selectedColors.isNotEmpty())
    }

    private fun confirmRemoval(message: String): Boolean {
        return Messages.showYesNoDialog(project, message, "Delete Colors", null) == Messages.YES
    }

    fun getContent(): JComponent = panel

    override fun dispose() {
        paletteChangeSubscription.close()
    }

    private fun copyToClipboard(text: String) {
        val selection = StringSelection(text)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
        StatusBar.Info.set("Copied $text", project)
    }

    private class PaletteTreeModel(
        root: DefaultMutableTreeNode,
        private val paletteManager: QssColorPaletteManager
    ) : DefaultTreeModel(root) {
        override fun valueForPathChanged(path: TreePath, newValue: Any?) {
            val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
            val requestedName = newValue?.toString() ?: return

            val renamed = when (val item = node.userObject) {
                is QssColorPalette -> paletteManager.renamePalette(item, requestedName)
                is QssColor -> {
                    val palette = node.parentPalette() ?: return
                    paletteManager.renameColor(palette, item, requestedName)
                }
                else -> false
            }

            if (renamed) {
                nodeChanged(node)
            }
        }
    }

    private class PaletteTreeCellRenderer : DefaultTreeCellRenderer() {
        init {
            backgroundSelectionColor = null
            backgroundNonSelectionColor = null
            borderSelectionColor = null
        }

        override fun getTreeCellRendererComponent(
            tree: JTree,
            value: Any?,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ): Component {
            val component = super.getTreeCellRendererComponent(
                tree,
                value,
                selected,
                expanded,
                leaf,
                row,
                hasFocus
            ) as JLabel

            val node = value as? DefaultMutableTreeNode
            component.isOpaque = false
            component.background = null
            component.foreground = tree.foreground

            when (val item = node?.userObject) {
                is QssColorPalette -> {
                    text = item.name
                    icon = QssIcons.FOLDER
                    toolTipText = "Double-click to rename"
                }

                is QssColor -> {
                    text = item.name
                    icon = QssColorSwatchIcon(item.value)
                    toolTipText = "Click swatch to edit; double-click name to copy ${item.toQssFormat()}"
                }
            }

            return component
        }
    }

    private class PaletteTreeTransferHandler(
        private val paletteManager: QssColorPaletteManager,
        private val onMoved: (Any?) -> Unit
    ) : TransferHandler() {
        private val itemFlavor = DataFlavor(DraggedItem::class.java, "QSS palette tree item")

        override fun getSourceActions(component: JComponent): Int = MOVE

        override fun createTransferable(component: JComponent): Transferable? {
            val tree = component as? JTree ?: return null
            val selectedNodes = tree.selectionPaths
                ?.mapNotNull { it.lastPathComponent as? DefaultMutableTreeNode }
                ?: return null
            val node = selectedNodes.singleOrNull() ?: selectedNodes.firstOrNull() ?: return null

            val draggedItem = when (val item = node.userObject) {
                is QssColorPalette -> {
                    if (selectedNodes.size != 1) return null
                    DraggedItem.Palette(item)
                }
                is QssColor -> {
                    val colorSelections = selectedNodes.mapNotNull { selectedNode ->
                        val color = selectedNode.userObject as? QssColor ?: return@mapNotNull null
                        val palette = selectedNode.parentPalette() ?: return@mapNotNull null
                        QssColorPaletteManager.ColorSelection(palette, color)
                    }
                    if (colorSelections.size != selectedNodes.size) return null

                    val sourcePalette = colorSelections.firstOrNull()?.palette ?: return null
                    if (colorSelections.any { it.palette !== sourcePalette }) return null

                    DraggedItem.Colors(sourcePalette, colorSelections.map { it.color })
                }
                else -> return null
            }

            return PaletteTransferable(itemFlavor, draggedItem)
        }

        override fun canImport(support: TransferSupport): Boolean {
            if (!support.isDrop || !support.isDataFlavorSupported(itemFlavor)) return false

            val draggedItem = support.draggedItem() ?: return false
            val dropLocation = support.dropLocation as? JTree.DropLocation ?: return false
            return findDropTarget(draggedItem, dropLocation) != null
        }

        override fun importData(support: TransferSupport): Boolean {
            if (!canImport(support)) return false

            val draggedItem = support.draggedItem() ?: return false
            val dropLocation = support.dropLocation as? JTree.DropLocation ?: return false
            val target = findDropTarget(draggedItem, dropLocation) ?: return false

            val moved = when {
                draggedItem is DraggedItem.Palette && target is DropTarget.PaletteIndex ->
                    paletteManager.movePalette(draggedItem.palette, target.index)

                draggedItem is DraggedItem.Colors && target is DropTarget.ColorIndex ->
                    paletteManager.moveColors(
                        sourcePalette = draggedItem.sourcePalette,
                        colors = draggedItem.colors,
                        targetPalette = target.palette,
                        targetIndex = target.index
                    )

                else -> false
            }

            if (moved) {
                onMoved(
                    when (draggedItem) {
                        is DraggedItem.Palette -> draggedItem.palette
                        is DraggedItem.Colors -> draggedItem.colors.firstOrNull()
                    }
                )
            }

            return moved
        }

        private fun findDropTarget(
            draggedItem: DraggedItem,
            dropLocation: JTree.DropLocation
        ): DropTarget? {
            val path = dropLocation.path ?: return null
            val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return null
            val childIndex = dropLocation.childIndex

            return when (draggedItem) {
                is DraggedItem.Palette -> findPaletteDropTarget(node, childIndex)
                is DraggedItem.Colors -> findColorDropTarget(node, childIndex)
            }
        }

        private fun findPaletteDropTarget(node: DefaultMutableTreeNode, childIndex: Int): DropTarget? {
            if (node.isRoot && childIndex >= 0) {
                return DropTarget.PaletteIndex(childIndex)
            }

            if (childIndex >= 0) return null

            node.userObject as? QssColorPalette ?: return null
            val parent = node.parent as? DefaultMutableTreeNode ?: return null
            val targetIndex = parent.getIndex(node)
            return DropTarget.PaletteIndex(targetIndex)
        }

        private fun findColorDropTarget(node: DefaultMutableTreeNode, childIndex: Int): DropTarget? {
            return when (val item = node.userObject) {
                is QssColorPalette -> {
                    val index = if (childIndex >= 0) childIndex else item.getAllColors().size
                    DropTarget.ColorIndex(item, index)
                }

                is QssColor -> {
                    val palette = node.parentPalette() ?: return null
                    val parent = node.parent as? DefaultMutableTreeNode ?: return null
                    DropTarget.ColorIndex(palette, parent.getIndex(node))
                }

                else -> null
            }
        }

        private fun TransferSupport.draggedItem(): DraggedItem? {
            return try {
                transferable.getTransferData(itemFlavor) as? DraggedItem
            } catch (_: UnsupportedFlavorException) {
                null
            } catch (_: java.io.IOException) {
                null
            }
        }
    }

    private class PaletteTransferable(
        private val itemFlavor: DataFlavor,
        private val item: DraggedItem
    ) : Transferable {
        override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(itemFlavor)

        override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = flavor == itemFlavor

        override fun getTransferData(flavor: DataFlavor): Any {
            if (!isDataFlavorSupported(flavor)) throw UnsupportedFlavorException(flavor)
            return item
        }
    }

    private sealed class DraggedItem {
        data class Palette(val palette: QssColorPalette) : DraggedItem()
        data class Colors(val sourcePalette: QssColorPalette, val colors: List<QssColor>) : DraggedItem()
    }

    private sealed class DropTarget {
        data class PaletteIndex(val index: Int) : DropTarget()
        data class ColorIndex(val palette: QssColorPalette, val index: Int) : DropTarget()
    }

    companion object {
        private const val ROOT_LABEL = "Folders"
        private const val LAST_PANEL_KEY = "qss.color.picker.last.panel"
        private const val COPY_SELECTED_COLOR_ACTION = "copySelectedQssColor"
        private const val COLOR_SWATCH_HIT_WIDTH = 22
    }
}

private fun DefaultMutableTreeNode.parentPalette(): QssColorPalette? {
    val parentNode = parent as? DefaultMutableTreeNode ?: return null
    return parentNode.userObject as? QssColorPalette
}

private fun TreePath.nodeUserObject(): Any? {
    return (lastPathComponent as? DefaultMutableTreeNode)?.userObject
}
