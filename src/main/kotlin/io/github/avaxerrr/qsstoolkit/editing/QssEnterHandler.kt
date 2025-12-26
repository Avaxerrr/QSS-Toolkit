package io.github.avaxerrr.qsstoolkit.editing

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile
import io.github.avaxerrr.qsstoolkit.QssLanguage

class QssEnterHandler : EnterHandlerDelegate {

    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffset: Ref<Int>,
        caretAdvance: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?
    ): EnterHandlerDelegate.Result {
        if (file.language != QssLanguage) {
            return EnterHandlerDelegate.Result.Continue
        }

        val offset = caretOffset.get()
        val document = editor.document
        val text = document.text

        // Check if we're between braces { | }
        if (offset > 0 && offset < text.length &&
            text[offset - 1] == '{' && text[offset] == '}') {

            // Calculate indentation
            val lineNumber = document.getLineNumber(offset)
            val lineStartOffset = document.getLineStartOffset(lineNumber)
            val currentLineText = text.substring(lineStartOffset, offset)
            val indent = currentLineText.takeWhile { it.isWhitespace() }

            val indentString = getIndentString(editor)
            val contentIndent = indent + indentString

            // Insert newline with proper indentation for both lines
            document.insertString(offset, "\n$contentIndent\n$indent")

            // Move cursor to content line
            editor.caretModel.moveToOffset(offset + 1 + contentIndent.length)

            return EnterHandlerDelegate.Result.Stop
        }

        return EnterHandlerDelegate.Result.Continue
    }

    override fun postProcessEnter(
        file: PsiFile,
        editor: Editor,
        dataContext: DataContext
    ): EnterHandlerDelegate.Result {
        // No post-processing needed for QSS
        return EnterHandlerDelegate.Result.Continue
    }

    private fun getIndentString(editor: Editor): String {
        val settings = editor.settings
        val indentSize = settings.getTabSize(editor.project)

        return if (settings.isUseTabCharacter(editor.project)) {
            "\t"
        } else {
            " ".repeat(indentSize)
        }
    }
}
