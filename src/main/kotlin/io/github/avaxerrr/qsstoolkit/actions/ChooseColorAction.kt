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
import java.awt.Color
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
            val newColor = JColorChooser.showDialog(
                editor.component,
                "Choose Color",
                initialColor
            )

            if (newColor != null) {
                // Wrap the document modification in ApplicationManager.getApplication().invokeLater
                // with the correct modality state to ensure it runs in a write-safe context
                ApplicationManager.getApplication().invokeLater(
                    {
                        updateColorInFile(project, editor, element, newColor)
                    },
                    ModalityState.defaultModalityState() // Use the default modality state
                )
            }
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
}
