package io.github.avaxerrr.qsstoolkit.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.StatusBar
import io.github.avaxerrr.qsstoolkit.livereload.QssLiveReloadFramework
import io.github.avaxerrr.qsstoolkit.livereload.QssLivePreviewHelpers
import io.github.avaxerrr.qsstoolkit.livereload.QssLiveReloadSnippets
import java.awt.datatransfer.StringSelection
import java.nio.file.Path

private const val QSS_TOOLKIT_NOTIFICATION_GROUP = "QSS Toolkit"
private const val QSS_PATH_PLACEHOLDER = "path/to/your/style.qss"

class QssLivePreviewActionGroup(
    private val currentQssPath: String?
) : ActionGroup("Enable Live Preview", true), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        if (currentQssPath == null) {
            return arrayOf(DisabledLivePreviewAction("Open a .qss file to enable live preview"))
        }

        return QssLiveReloadFramework.entries
            .map { framework -> CreateLivePreviewHelperAction(framework, currentQssPath) }
            .toTypedArray()
    }
}

private class CreateLivePreviewHelperAction(
    private val framework: QssLiveReloadFramework,
    private val qssPath: String
) : AnAction("${framework.label} Helper"), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val targetDir = resolveTargetDirectory(project, e)
        if (targetDir == null) {
            notifyError(project, "Could not find a project folder for the live preview helper.")
            return
        }

        val helperFileName = QssLivePreviewHelpers.helperFileName(framework)
        val existing = targetDir.findChild(helperFileName)
        if (existing?.isDirectory == true) {
            notifyError(project, "$helperFileName already exists as a folder in ${targetDir.path}.")
            return
        }

        val helperText = QssLivePreviewHelpers.generateHelper(framework, qssPath)
        var helperFile: VirtualFile? = existing
        var created = false

        try {
            if (helperFile == null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    helperFile = targetDir.createChildData(this, helperFileName)
                    VfsUtil.saveText(helperFile!!, helperText)
                    created = true
                }
            }
        } catch (error: Exception) {
            notifyError(project, "Could not create $helperFileName: ${error.message ?: error.javaClass.simpleName}")
            return
        }

        helperFile?.let { file ->
            FileEditorManager.getInstance(project).openFile(file, true)
            StatusBar.Info.set(
                if (created) "Created ${file.name} for QSS live preview" else "Opened existing ${file.name}",
                project
            )
            notifyHelperReady(project, file, created)
        }
    }

    private fun resolveTargetDirectory(project: Project, e: AnActionEvent): VirtualFile? {
        val projectRoot = project.basePath
            ?.let { basePath -> LocalFileSystem.getInstance().refreshAndFindFileByNioFile(Path.of(basePath)) }
            ?.takeIf { it.isDirectory }
        if (projectRoot != null) {
            return projectRoot
        }

        val currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        return if (currentFile?.isDirectory == true) currentFile else currentFile?.parent
    }

    private fun notifyHelperReady(project: Project, helperFile: VirtualFile, created: Boolean) {
        val setupCall = QssLivePreviewHelpers.setupCall(framework)
            .escapeHtml()
            .replace("\n", "<br>")
        val state = if (created) "created" else "already exists"

        NotificationGroupManager.getInstance()
            .getNotificationGroup(QSS_TOOLKIT_NOTIFICATION_GROUP)
            .createNotification(
                "Live preview helper $state",
                "${helperFile.name} was $state in ${helperFile.parent.path}. Use this setup in your app:<br><code>$setupCall</code>",
                NotificationType.INFORMATION
            )
            .notify(project)
    }

    private fun notifyError(project: Project, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(QSS_TOOLKIT_NOTIFICATION_GROUP)
            .createNotification("Live preview helper not created", message, NotificationType.ERROR)
            .notify(project)
    }
}

class QssLiveReloadSnippetActionGroup(
    private val currentQssPath: String?,
    private val insertIntoEditor: Boolean
) : ActionGroup(if (insertIntoEditor) "Insert Live Reload Watcher" else "Copy Live Reload Watcher", true), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return QssLiveReloadFramework.entries
            .map { framework ->
                LiveReloadSnippetAction(
                    framework = framework,
                    currentQssPath = currentQssPath,
                    insertIntoEditor = insertIntoEditor
                )
            }
            .toTypedArray()
    }
}

private class DisabledLivePreviewAction(text: String) : AnAction(text), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = false
    }

    override fun actionPerformed(e: AnActionEvent) = Unit
}

private fun String.escapeHtml(): String {
    return replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}

private class LiveReloadSnippetAction(
    private val framework: QssLiveReloadFramework,
    private val currentQssPath: String?,
    private val insertIntoEditor: Boolean
) : AnAction("${if (insertIntoEditor) "Insert" else "Copy"} ${framework.label} Watcher Code"), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val qssPath = currentQssPath ?: QSS_PATH_PLACEHOLDER
        val snippet = QssLiveReloadSnippets.generate(framework, qssPath)

        if (insertIntoEditor) {
            val project = e.project ?: return
            val editor = e.getData(CommonDataKeys.EDITOR) ?: return
            insertSnippet(project, editor, snippet)
            StatusBar.Info.set("Inserted ${framework.label} live reload snippet", project)
            notifyInserted(project)
        } else {
            CopyPasteManager.getInstance().setContents(StringSelection(snippet))
            StatusBar.Info.set("Copied ${framework.label} live reload snippet", e.project)
            notifyCopied(e.project)
        }
    }

    private fun notifyInserted(project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(QSS_TOOLKIT_NOTIFICATION_GROUP)
            .createNotification(
                "Live reload watcher inserted",
                "${framework.label} watcher code was inserted at the caret. Update QSS_PATH, then keep the code after QApplication is created.",
                NotificationType.INFORMATION
            )
            .notify(project)
    }

    private fun notifyCopied(project: Project?) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(QSS_TOOLKIT_NOTIFICATION_GROUP)
            .createNotification(
                "Live reload watcher copied",
                "${framework.label} watcher code was copied to the clipboard. Paste it into your application code after QApplication is created; the .qss file only contains styles.",
                NotificationType.INFORMATION
            )
            .notify(project)
    }

    private fun insertSnippet(project: Project, editor: Editor, snippet: String) {
        val document = editor.document
        val selectionModel = editor.selectionModel
        val startOffset = if (selectionModel.hasSelection()) {
            selectionModel.selectionStart
        } else {
            editor.caretModel.offset
        }
        val endOffset = if (selectionModel.hasSelection()) {
            selectionModel.selectionEnd
        } else {
            editor.caretModel.offset
        }
        val text = snippet + "\n"

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(startOffset, endOffset, text)
            selectionModel.removeSelection()
            editor.caretModel.moveToOffset(startOffset + text.length)
        }
    }
}
