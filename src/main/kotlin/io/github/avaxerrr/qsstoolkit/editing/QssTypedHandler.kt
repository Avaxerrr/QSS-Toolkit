package io.github.avaxerrr.qsstoolkit.editing

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import io.github.avaxerrr.qsstoolkit.QssLanguage
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes

class QssTypedHandler : TypedHandlerDelegate() {

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file.language != QssLanguage) {
            return Result.CONTINUE
        }

        when (c) {
            '{' -> handleLeftBrace(editor, file)
            '(' -> handleLeftParen(editor, file)
            '[' -> handleLeftBracket(editor, file)
        }

        return Result.CONTINUE
    }

    override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
        // Use the same check pattern as charTyped method
        if (file.language != QssLanguage) {
            return Result.CONTINUE
        }

        // Auto-trigger completion after typing ':'
        if (charTyped == ':') {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
        }

        return Result.CONTINUE
    }

    private fun handleLeftBrace(editor: Editor, file: PsiFile) {
        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.text

        // Check if we're in a valid context (not in string/comment)
        if (!isInValidContext(file, offset)) {
            return
        }

        // Don't insert if closing brace is immediately next
        if (offset < text.length && text[offset] == '}') {
            return
        }

        // Insert closing brace with newlines and indentation
        val lineNumber = document.getLineNumber(offset)
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val currentLineText = text.substring(lineStartOffset, offset)
        val indent = currentLineText.takeWhile { it.isWhitespace() }

        // Calculate indent for content (one level deeper)
        val indentString = getIndentString(editor)
        val contentIndent = indent + indentString

        // Insert closing brace with proper formatting
        document.insertString(offset, "\n$contentIndent\n$indent}")

        // Move cursor to the content line with proper indentation
        editor.caretModel.moveToOffset(offset + 1 + contentIndent.length)
    }

    private fun handleLeftParen(editor: Editor, file: PsiFile) {
        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.text

        // Check if we're in a valid context (not in string/comment)
        if (!isInValidContext(file, offset)) {
            return
        }

        // Don't insert if closing paren is immediately next
        if (offset < text.length && text[offset] == ')') {
            return
        }

        document.insertString(offset, ")")
    }

    private fun handleLeftBracket(editor: Editor, file: PsiFile) {
        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.text

        // Check if we're in a valid context (not in string/comment)
        if (!isInValidContext(file, offset)) {
            return
        }

        // Don't insert if closing bracket is immediately next
        if (offset < text.length && text[offset] == ']') {
            return
        }

        document.insertString(offset, "]")
    }

    private fun isInValidContext(file: PsiFile, offset: Int): Boolean {
        // Get the PSI element at the cursor position
        if (offset <= 0) return true

        val element = file.findElementAt(offset - 1) ?: return true
        val elementType = element.node?.elementType

        // Don't auto-complete inside strings or comments
        return elementType != QssTokenTypes.STRING &&
                elementType != QssTokenTypes.COMMENT
    }

    private fun getIndentString(editor: Editor): String {
        val settings = editor.settings
        val indentSize = if (editor is EditorEx) {
            editor.settings.getTabSize(editor.project)
        } else {
            4 // Default fallback
        }

        return if (settings.isUseTabCharacter(editor.project)) {
            "\t"
        } else {
            " ".repeat(indentSize)
        }
    }
}
