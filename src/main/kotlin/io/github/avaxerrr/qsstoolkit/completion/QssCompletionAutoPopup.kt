package io.github.avaxerrr.qsstoolkit.completion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import io.github.avaxerrr.qsstoolkit.psi.QssFile

class QssCompletionAutoPopup : TypedHandlerDelegate() {

    override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
        // Only trigger for QSS files
        if (file !is QssFile) {
            return Result.CONTINUE
        }

        // Auto-trigger completion after typing ':'
        if (charTyped == ':') {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            return Result.STOP
        }

        return Result.CONTINUE
    }
}
