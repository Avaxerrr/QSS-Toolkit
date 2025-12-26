package io.github.avaxerrr.qsstoolkit.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import io.github.avaxerrr.qsstoolkit.QssIcons

class CreateQssFileAction : CreateFileFromTemplateAction("QSS File", "Creates a new QSS style sheet file", QssIcons.FILE) {

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New QSS File")
            .addKind("QSS File", QssIcons.FILE, "QssFile")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create QSS File"
    }
}
