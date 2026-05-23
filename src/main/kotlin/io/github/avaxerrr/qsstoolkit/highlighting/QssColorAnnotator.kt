package io.github.avaxerrr.qsstoolkit.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes
import io.github.avaxerrr.qsstoolkit.palette.QssColorFormats
import io.github.avaxerrr.qsstoolkit.psi.QssColorValue

class QssColorAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Handle hex color values
        if (element.node.elementType == QssTokenTypes.HEX_COLOR) {
            val colorText = element.text
            val color = QssColorFormats.parseConcreteColor(colorText)

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
            val color = QssColorFormats.parseConcreteColor(colorText)

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
            val color = QssColorFormats.parseConcreteColor(colorText)

            if (color != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .textAttributes(QssSyntaxHighlighter.QSS_FUNCTION)
                    .gutterIconRenderer(ColorBoxIconRenderer(color, colorText, element))
                    .create()
            }
        }

        if (element.node.elementType == QssTokenTypes.COLOR_FUNCTION) {
            val colorText = element.text
            val color = QssColorFormats.parseConcreteColor(colorText)

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
            val color = QssColorFormats.parseConcreteColor(colorText)

            if (color != null) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .gutterIconRenderer(ColorBoxIconRenderer(color, colorText, element))
                    .create()
            }
        }
    }
}
