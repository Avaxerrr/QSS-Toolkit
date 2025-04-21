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

class ChooseColorAction(
    private val initialColor: Color,
    private val colorText: String,
    private val element: PsiElement
) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // Find the editor containing the element
        val editor = getEditorForElement(project, element) ?: return

        // Show the color chooser dialog using JColorChooser directly
        val newColor = JColorChooser.showDialog(
            editor.component,
            "Choose Color",
            initialColor
        )

        // If user selected a color, update it in the file
        if (newColor != null) {
            updateColorInFile(project, editor, element, newColor)
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

        // Get the range of text to replace
        val startOffset = element.textRange.startOffset
        val endOffset = element.textRange.endOffset

        // Update the text
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.replaceString(startOffset, endOffset, hexColor)
            editor.caretModel.moveToOffset(startOffset + hexColor.length)
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }
}
