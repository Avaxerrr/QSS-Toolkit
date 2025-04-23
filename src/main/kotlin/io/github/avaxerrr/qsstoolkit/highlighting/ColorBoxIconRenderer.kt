// QSS Toolkit version 1.0

package io.github.avaxerrr.qsstoolkit.highlighting

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.util.ui.ColorIcon
import io.github.avaxerrr.qsstoolkit.actions.ChooseColorAction
import java.awt.Color
import javax.swing.Icon

class ColorBoxIconRenderer(private val color: Color, private val colorText: String, private val element: PsiElement) : GutterIconRenderer() {
    companion object {
        private const val ICON_SIZE = 12
    }

    override fun getIcon(): Icon = ColorIcon(ICON_SIZE, color)

    override fun getClickAction(): AnAction = ChooseColorAction(color, colorText, element)

    override fun isNavigateAction(): Boolean = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ColorBoxIconRenderer

        if (color != other.color) return false
        if (colorText != other.colorText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = color.hashCode()
        result = 31 * result + colorText.hashCode()
        return result
    }
}
