// QSS Toolkit version 1.1

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
import java.awt.event.ActionEvent
import javax.swing.JColorChooser
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
                true,  // modal
                colorChooser,
                { actionEvent ->  // FIXED: Changed from 'newColor' to 'actionEvent'
                    // OK action - get the selected color from the chooser
                    val selectedColor = colorChooser.color
                    if (selectedColor != null) {
                        // Save the selected panel for next time
                        saveSelectedPanel(colorChooser)

                        ApplicationManager.getApplication().invokeLater({
                            updateColorInFile(project, editor, element, selectedColor)
                        }, ModalityState.defaultModalityState())
                    }
                },
                null  // Cancel action
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

        // Try to select the last used panel
        val panels = colorChooser.chooserPanels
        val targetPanel = panels.firstOrNull {
            it.displayName.equals(lastPanel, ignoreCase = true)
        } ?: panels.firstOrNull {
            it.displayName.contains("HSV", ignoreCase = true) ||
                    it.displayName.contains("HSL", ignoreCase = true) ||
                    it.displayName.contains("HSB", ignoreCase = true)
        } ?: panels.firstOrNull()

        if (targetPanel != null) {
            colorChooser.setChooserPanels(
                panels.sortedBy { if (it == targetPanel) 0 else 1 }.toTypedArray()
            )
        }
    }

    private fun saveSelectedPanel(colorChooser: JColorChooser) {
        val selectedPanel = colorChooser.chooserPanels.firstOrNull()
        if (selectedPanel != null) {
            val properties = PropertiesComponent.getInstance()
            properties.setValue(LAST_PANEL_KEY, selectedPanel.displayName)
        }
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
        val hexColor = String.format("#%02X%02X%02X", color.red, color.green, color.blue)
        val startOffset = element.textRange.startOffset
        val endOffset = element.textRange.endOffset

        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.replaceString(startOffset, endOffset, hexColor)
            editor.caretModel.moveToOffset(startOffset + hexColor.length)
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }

    companion object {
        private const val LAST_PANEL_KEY = "qss.color.picker.last.panel"
    }
}
