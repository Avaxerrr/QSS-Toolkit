package io.github.avaxerrr.qsstoolkit.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes
import io.github.avaxerrr.qsstoolkit.psi.QssColorValue
import java.awt.Color

class QssColorAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Handle color values
        if (element.node.elementType == QssTokenTypes.HEX_COLOR) {
            val colorText = element.text
            val hexColor = colorText.removePrefix("#")

            // Validate and convert the hex color
            val color = try {
                convertHexToColor(hexColor)
            } catch (e: Exception) {
                null
            }

            if (color != null) {
                // Create an annotation with color preview
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .textAttributes(QssSyntaxHighlighter.QSS_COLOR)
                    .gutterIconRenderer(ColorBoxIconRenderer(color, colorText, element))
                    .create()
            }
        }

        // If using the PSI tree, we can also check for QssColorValue
        if (element is QssColorValue) {
            val colorText = element.colorText ?: return
            val hexColor = colorText.removePrefix("#")

            val color = try {
                convertHexToColor(hexColor)
            } catch (e: Exception) {
                null
            }

            if (color != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .gutterIconRenderer(ColorBoxIconRenderer(color, colorText, element))
                    .create()
            }
        }
    }

    private fun convertHexToColor(hex: String): Color? {
        // Convert hex color to Color object
        return when (hex.length) {
            3 -> {
                // Convert 3-digit hex to 6-digit
                val r = hex.substring(0, 1).repeat(2)
                val g = hex.substring(1, 2).repeat(2)
                val b = hex.substring(2, 3).repeat(2)
                Color(r.toInt(16), g.toInt(16), b.toInt(16))
            }
            6 -> {
                // Standard 6-digit hex
                Color(hex.toInt(16))
            }
            8 -> {
                // 8-digit hex with alpha
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                val a = hex.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            else -> null
        }
    }
}
