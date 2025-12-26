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
        // Handle hex color values
        if (element.node.elementType == QssTokenTypes.HEX_COLOR) {
            val colorText = element.text
            val hexColor = colorText.removePrefix("#")
            val color = try {
                convertHexToColor(hexColor)
            } catch (e: Exception) {
                null
            }

            if (color != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .textAttributes(QssSyntaxHighlighter.QSS_HEX_COLOR)
                    .gutterIconRenderer(ColorBoxIconRenderer(color, colorText, element))
                    .create()
            }
        }

        // Handle RGB function: rgb(255, 0, 0)
        if (element.node.elementType == QssTokenTypes.RGB_FUNCTION) {
            val colorText = element.text
            val color = parseRgbFunction(colorText)

            if (color != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .textAttributes(QssSyntaxHighlighter.QSS_FUNCTION)
                    .gutterIconRenderer(ColorBoxIconRenderer(color, colorText, element))
                    .create()
            }
        }

        // Handle RGBA function: rgba(0, 0, 255, 0.5)
        if (element.node.elementType == QssTokenTypes.RGBA_FUNCTION) {
            val colorText = element.text
            val color = parseRgbaFunction(colorText)

            if (color != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .textAttributes(QssSyntaxHighlighter.QSS_FUNCTION)
                    .gutterIconRenderer(ColorBoxIconRenderer(color, colorText, element))
                    .create()
            }
        }

        // If using the PSI tree for color values
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
        return when (hex.length) {
            3 -> {
                val r = hex.substring(0, 1).repeat(2)
                val g = hex.substring(1, 2).repeat(2)
                val b = hex.substring(2, 3).repeat(2)
                Color(r.toInt(16), g.toInt(16), b.toInt(16))
            }
            6 -> {
                Color(hex.toInt(16))
            }
            8 -> {
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                val a = hex.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            else -> null
        }
    }

    private fun parseRgbFunction(text: String): Color? {
        try {
            val pattern = "rgb" + "\\(" + "(\\d+)" + ",\\s*" + "(\\d+)" + ",\\s*" + "(\\d+)" + "\\)"
            val match = Regex(pattern).find(text)
            if (match != null) {
                val (r, g, b) = match.destructured
                val red = r.toInt().coerceIn(0, 255)
                val green = g.toInt().coerceIn(0, 255)
                val blue = b.toInt().coerceIn(0, 255)
                return Color(red, green, blue)
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    private fun parseRgbaFunction(text: String): Color? {
        try {
            val pattern = "rgba" + "\\(" + "(\\d+)" + ",\\s*" + "(\\d+)" + ",\\s*" + "(\\d+)" + ",\\s*" + "([0-9.]+)" + "\\)"
            val match = Regex(pattern).find(text)
            if (match != null) {
                val (r, g, b, a) = match.destructured
                val red = r.toInt().coerceIn(0, 255)
                val green = g.toInt().coerceIn(0, 255)
                val blue = b.toInt().coerceIn(0, 255)
                val alpha = (a.toFloat() * 255).toInt().coerceIn(0, 255)
                return Color(red, green, blue, alpha)
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }
}