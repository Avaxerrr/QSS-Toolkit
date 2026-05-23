package io.github.avaxerrr.qsstoolkit.actions

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.StatusBar
import io.github.avaxerrr.qsstoolkit.livereload.QssLiveReloadFramework
import io.github.avaxerrr.qsstoolkit.livereload.QssLiveReloadSnippets
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class QssLiveReloadSnippetActionGroup(
    private val qssPath: String
) : ActionGroup("Copy Live Reload Snippet", true), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return QssLiveReloadFramework.entries
            .map { framework -> CopyLiveReloadSnippetAction(framework, qssPath) }
            .toTypedArray()
    }
}

private class CopyLiveReloadSnippetAction(
    private val framework: QssLiveReloadFramework,
    private val qssPath: String
) : AnAction(framework.label), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val snippet = QssLiveReloadSnippets.generate(framework, qssPath)
        Toolkit.getDefaultToolkit()
            .systemClipboard
            .setContents(StringSelection(snippet), null)
        StatusBar.Info.set("Copied ${framework.label} live reload snippet", e.project)
    }
}
