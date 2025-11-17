// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.editing

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import io.github.avaxerrr.qsstoolkit.QssLanguage
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes

class QssTypedHandler : TypedHandlerDelegate() {

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file.language != QssLanguage) {
            return Result.CONTINUE
        }

        when (c) {
            '{' -> handleLeftBrace(editor, file)
            '(' -> handleLeftParen(editor)
            '[' -> handleLeftBracket(editor)
            ';' -> handleSemicolon(editor)
        }

        return Result.CONTINUE
    }

    private fun handleLeftBrace(editor: Editor, file: PsiFile) {
        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.text

        // Check if we should insert closing brace
        if (shouldInsertClosingBrace(text, offset)) {
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
    }

    private fun handleLeftParen(editor: Editor) {
        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.text

        // Check if we should insert closing paren
        if (shouldInsertClosingChar(text, offset, ')')) {
            document.insertString(offset, ")")
            // Keep cursor between the parentheses
        }
    }

    private fun handleLeftBracket(editor: Editor) {
        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.text

        // Check if we should insert closing bracket
        if (shouldInsertClosingChar(text, offset, ']')) {
            document.insertString(offset, "]")
            // Keep cursor between the brackets
        }
    }

    private fun handleSemicolon(editor: Editor) {
        val offset = editor.caretModel.offset
        val document = editor.document
        val text = document.text

        // Check if semicolon was typed inside a function call like rgb(255, 0, 0;)
        // and move it to the proper position
        if (offset > 0 && offset < text.length) {
            var searchOffset = offset
            var foundClosingParen = false
            var parenDepth = 0

            // Look ahead to find if we're inside parentheses
            while (searchOffset < text.length) {
                val char = text[searchOffset]
                when (char) {
                    ')' -> {
                        if (parenDepth == 0) {
                            foundClosingParen = true
                            break
                        } else {
                            parenDepth--
                        }
                    }
                    '(' -> parenDepth++
                    '\n', '}' -> break // Stop at line end or block end
                }
                searchOffset++
            }

            // If we found a closing paren and we're inside it, move semicolon after it
            if (foundClosingParen && searchOffset > offset) {
                // Remove the semicolon we just typed
                document.deleteString(offset - 1, offset)

                // Insert it after the closing paren
                document.insertString(searchOffset, ";")

                // Keep cursor after the moved semicolon
                editor.caretModel.moveToOffset(searchOffset + 1)
            }
        }
    }

    private fun shouldInsertClosingBrace(text: String, offset: Int): Boolean {
        // Don't insert if there's already a closing brace immediately after
        if (offset < text.length && text[offset] == '}') {
            return false
        }

        // Don't insert if we're in a string or comment
        // (More sophisticated check would inspect PSI tree)
        return true
    }

    private fun shouldInsertClosingChar(text: String, offset: Int, closingChar: Char): Boolean {
        // Don't insert if there's already the closing char immediately after
        if (offset < text.length && text[offset] == closingChar) {
            return false
        }

        // Don't insert if the next non-whitespace character is the closing char
        val nextChar = text.drop(offset).firstOrNull { !it.isWhitespace() }
        if (nextChar == closingChar) {
            return false
        }

        return true
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
