package io.github.avaxerrr.qsstoolkit.ui

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import javax.swing.Icon

class QssColorSwatchIcon(
    private val color: Color,
    private val size: Int = JBUI.scale(DEFAULT_SIZE)
) : Icon {
    override fun getIconWidth(): Int = size

    override fun getIconHeight(): Int = size

    override fun paintIcon(component: Component?, graphics: Graphics, x: Int, y: Int) {
        val graphics2D = graphics.create() as Graphics2D
        try {
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            val shape = RoundRectangle2D.Float(
                x.toFloat(),
                y.toFloat(),
                size.toFloat() - 1f,
                size.toFloat() - 1f,
                CORNER_RADIUS.toFloat(),
                CORNER_RADIUS.toFloat()
            )
            val oldClip = graphics2D.clip
            graphics2D.clip = shape

            if (color.alpha < 255) {
                paintTransparencyGrid(graphics2D, x, y)
            }

            graphics2D.color = color
            graphics2D.fill(shape)
            graphics2D.clip = oldClip
            graphics2D.color = JBColor.border()
            graphics2D.draw(shape)
        } finally {
            graphics2D.dispose()
        }
    }

    private fun paintTransparencyGrid(graphics: Graphics2D, x: Int, y: Int) {
        val tileSize = maxOf(2, size / 3)
        for (tileX in x until x + size step tileSize) {
            for (tileY in y until y + size step tileSize) {
                val dark = ((tileX - x) / tileSize + (tileY - y) / tileSize) % 2 == 0
                graphics.color = if (dark) CHECKER_DARK else CHECKER_LIGHT
                graphics.fillRect(tileX, tileY, tileSize, tileSize)
            }
        }
    }

    private companion object {
        private const val DEFAULT_SIZE = 14
        private const val CORNER_RADIUS = 4
        private val CHECKER_DARK = Color(160, 160, 160)
        private val CHECKER_LIGHT = Color(230, 230, 230)
    }
}
