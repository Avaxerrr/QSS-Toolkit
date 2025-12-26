package io.github.avaxerrr.qsstoolkit.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.ide.util.PropertiesComponent
import java.awt.Color
import java.awt.Component
import java.awt.Container
import javax.swing.JColorChooser
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState

class ChooseColorAction(
    private val initialColor: Color,
    private val colorText: String,
    private val element: PsiElement
) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = getEditorForElement(project, element) ?: return

        // Show the dialog
        SwingUtilities.invokeLater {
            // Create customized color chooser
            val colorChooser = JColorChooser(initialColor)

            // Remove CMYK panel and configure tabs
            configureColorChooserPanels(colorChooser)

            // Show dialog with customized chooser
            val dialog = JColorChooser.createDialog(
                editor.component,
                "Choose Color",
                true, // modal
                colorChooser,
                { _ ->
                    // OK action - FIRST, save the selected panel while dialog is still open
                    saveSelectedPanel(colorChooser)

                    // THEN get the selected color and update
                    val selectedColor = colorChooser.color
                    if (selectedColor != null) {
                        ApplicationManager.getApplication().invokeLater(
                            { updateColorInFile(project, editor, element, selectedColor) },
                            ModalityState.defaultModalityState()
                        )
                    }
                },
                null // Cancel action
            )

            dialog.isVisible = true
        }
    }

    private fun configureColorChooserPanels(colorChooser: JColorChooser) {
        // Get all available panels
        val allPanels = colorChooser.chooserPanels

        // Filter out CMYK and Swatches panels - keep only HSV, HSL, RGB
        val filteredPanels = allPanels.filter { panel ->
            val displayName = panel.displayName
            displayName.contains("HSV", ignoreCase = true) ||
                    displayName.contains("HSL", ignoreCase = true) ||
                    displayName.contains("HSB", ignoreCase = true) ||
                    displayName.contains("RGB", ignoreCase = true)
        }

        // Set filtered panels back
        colorChooser.chooserPanels = filteredPanels.toTypedArray()

        // Restore last used panel
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

                println("✅ Saved color picker panel: $selectedPanelName")
            }
        } else {
            println("⚠️ Could not find JTabbedPane in color chooser")
        }
    }

    private fun findTabbedPane(component: Component): JTabbedPane? {
        if (component is JTabbedPane) {
            return component
        }
        if (component is Container) {
            for (child in component.components) {
                val result = findTabbedPane(child)
                if (result != null) return result
            }
        }
        return null
    }

    private fun getEditorForElement(project: Project, element: PsiElement): Editor? {
        val containingFile = element.containingFile ?: return null
        val fileEditors = FileEditorManager.getInstance(project)
            .getEditors(containingFile.virtualFile)

        if (fileEditors.isEmpty()) return null

        val textEditor = fileEditors[0] as? TextEditor ?: return null
        return textEditor.editor
    }

    private fun updateColorInFile(project: Project, editor: Editor, element: PsiElement, color: Color) {
        val originalText = element.text

        val newColorText = when {
            // RGBA format: rgba(r, g, b, a) - preserve it
            originalText.startsWith("rgba", ignoreCase = true) -> {
                val alpha = color.alpha / 255.0f
                String.format("rgba(%d, %d, %d, %.2f)", color.red, color.green, color.blue, alpha)
            }
            // RGB format: rgb(r, g, b) - if user added transparency, convert to rgba
            originalText.startsWith("rgb", ignoreCase = true) -> {
                if (color.alpha < 255) {
                    val alpha = color.alpha / 255.0f
                    String.format("rgba(%d, %d, %d, %.2f)", color.red, color.green, color.blue, alpha)
                } else {
                    String.format("rgb(%d, %d, %d)", color.red, color.green, color.blue)
                }
            }
            // Hex format: #RRGGBB - if user added transparency, convert to rgba
            else -> {
                if (color.alpha < 255) {
                    // Convert to rgba() instead of 8-digit hex for Qt compatibility
                    val alpha = color.alpha / 255.0f
                    String.format("rgba(%d, %d, %d, %.2f)", color.red, color.green, color.blue, alpha)
                } else {
                    // Standard 6-digit hex
                    String.format("#%02X%02X%02X", color.red, color.green, color.blue)
                }
            }
        }

        val startOffset = element.textRange.startOffset
        val endOffset = element.textRange.endOffset

        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.replaceString(startOffset, endOffset, newColorText)
            editor.caretModel.moveToOffset(startOffset + newColorText.length)
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }

    companion object {
        private const val LAST_PANEL_KEY = "qss.color.picker.last.panel"
    }
}
