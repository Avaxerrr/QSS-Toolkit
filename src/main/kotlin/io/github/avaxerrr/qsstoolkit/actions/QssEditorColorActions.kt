package io.github.avaxerrr.qsstoolkit.actions

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import io.github.avaxerrr.qsstoolkit.QssIcons
import io.github.avaxerrr.qsstoolkit.QssFileType
import io.github.avaxerrr.qsstoolkit.palette.QssColor
import io.github.avaxerrr.qsstoolkit.palette.QssColorFormat
import io.github.avaxerrr.qsstoolkit.palette.QssColorFormats
import io.github.avaxerrr.qsstoolkit.palette.QssColorPalette
import io.github.avaxerrr.qsstoolkit.palette.QssColorPaletteManager
import io.github.avaxerrr.qsstoolkit.ui.QssColorSwatchIcon
import java.awt.Color

class QssEditorColorsActionGroup : ActionGroup("QSS Toolkit", true), DumbAware {
    init {
        templatePresentation.icon = QssIcons.TOOLKIT
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible =
            e.project != null && e.getData(CommonDataKeys.EDITOR) != null
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val project = e?.project ?: return emptyArray()
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return emptyArray()
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val paletteManager = QssColorPaletteManager.getInstance(project)
        val palettes = paletteManager.getAllPalettes()

        return buildList {
            add(InsertColorGroup(palettes))

            val selectedColor = editor.selectedConcreteColor()
            if (selectedColor != null) {
                add(Separator.getInstance())
                add(AddSelectedColorGroup(paletteManager, palettes, selectedColor))
            }

            if (virtualFile?.extension?.equals(QssFileType.DEFAULT_EXTENSION, ignoreCase = true) == true) {
                add(Separator.getInstance())
                add(QssLiveReloadSnippetActionGroup(virtualFile.path))
            }
        }.toTypedArray()
    }
}

private class InsertColorGroup(
    private val palettes: List<QssColorPalette>
) : ActionGroup("Insert Color", true), DumbAware {
    init {
        templatePresentation.icon = QssIcons.INSERT_COLOR
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        if (palettes.isEmpty()) {
            return arrayOf(DisabledAction("No color folders"))
        }

        return palettes.map { palette -> InsertFolderGroup(palette) }.toTypedArray()
    }
}

private class InsertFolderGroup(
    private val palette: QssColorPalette
) : ActionGroup(palette.name, true), DumbAware {
    init {
        templatePresentation.icon = QssIcons.FOLDER
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val colors = palette.getAllColors()
        if (colors.isEmpty()) {
            return arrayOf(DisabledAction("No colors"))
        }

        return colors.map { color -> InsertSavedColorGroup(color) }.toTypedArray()
    }
}

private class InsertSavedColorGroup(
    private val color: QssColor
) : ActionGroup(color.name, true), DumbAware {
    init {
        templatePresentation.icon = QssColorSwatchIcon(color.value)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return QssColorFormats.explicitFormatsFor(color.value)
            .map { format -> InsertColorFormatAction(color.value, format) }
            .toTypedArray()
    }
}

private class InsertColorFormatAction(
    private val color: Color,
    private val format: QssColorFormat
) : AnAction(format.label), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val text = QssColorFormats.format(color, format)

        replaceSelectionOrInsert(project, editor, text)
        StatusBar.Info.set("Inserted $text", project)
    }
}

private class AddSelectedColorGroup(
    private val paletteManager: QssColorPaletteManager,
    private val palettes: List<QssColorPalette>,
    private val selectedColor: Color
) : ActionGroup("Save Color", true), DumbAware {
    init {
        templatePresentation.icon = QssIcons.ADD_COLOR
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        if (palettes.isEmpty()) {
            return arrayOf(DisabledAction("No color folders"))
        }

        return palettes.map { palette ->
            AddSelectedColorToFolderAction(paletteManager, palette, selectedColor)
        }.toTypedArray()
    }
}

private class AddSelectedColorToFolderAction(
    private val paletteManager: QssColorPaletteManager,
    private val palette: QssColorPalette,
    private val selectedColor: Color
) : AnAction(palette.name), DumbAware {
    init {
        templatePresentation.icon = QssIcons.FOLDER
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        paletteManager.addColor(palette, selectedColor)
        StatusBar.Info.set(
            "Added ${QssColorFormats.format(selectedColor, QssColorFormat.AUTO)} to ${palette.name}",
            e.project
        )
    }
}

private class DisabledAction(text: String) : AnAction(text), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = false
    }

    override fun actionPerformed(e: AnActionEvent) = Unit
}

private fun Editor.selectedConcreteColor(): Color? {
    val selectedText = selectionModel.selectedText?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: return null
    return QssColorFormats.findConcreteColor(selectedText)
}

private fun replaceSelectionOrInsert(project: Project, editor: Editor, text: String) {
    val selectionModel = editor.selectionModel
    val document = editor.document
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

    WriteCommandAction.runWriteCommandAction(project) {
        document.replaceString(startOffset, endOffset, text)
        selectionModel.removeSelection()
        editor.caretModel.moveToOffset(startOffset + text.length)
    }
}
