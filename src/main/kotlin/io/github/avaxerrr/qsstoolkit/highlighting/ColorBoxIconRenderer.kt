// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.highlighting

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import io.github.avaxerrr.qsstoolkit.actions.ChooseColorAction
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon

class ColorBoxIconRenderer(
    private val color: Color,
    private val colorText: String,
    private val element: PsiElement
) : GutterIconRenderer() {

    companion object {
        private const val ICON_SIZE = 12
    }

    override fun getIcon(): Icon = BorderedColorIcon(ICON_SIZE, color)

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

    /**
     * Custom icon with a border to make transparent colors visible
     */
    private class BorderedColorIcon(private val size: Int, private val color: Color) : Icon {
        override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
            // Fill with color
            g.color = color
            g.fillRect(x, y, size, size)

            // Draw border (dark gray for visibility)
            g.color = Color(128, 128, 128)
            g.drawRect(x, y, size - 1, size - 1)
        }

        override fun getIconWidth(): Int = size

        override fun getIconHeight(): Int = size
    }
}
