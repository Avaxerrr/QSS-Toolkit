package io.github.avaxerrr.qsstoolkit.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes
import io.github.avaxerrr.qsstoolkit.palette.QssColorFormats
import java.awt.Color

/**
 * Annotator that adds background color highlighting to color values with contrast-aware text.
 * Shows the actual color as background behind the text with smart text color selection.
 *
 * Examples:
 *   #FF0000     → white text on SOLID red background
 *   #FFFFFF     → black text on SOLID white background
 *   rgba(0,0,255,0.5) → white text on 50% transparent blue background
 */
class QssColorBackgroundAnnotator : Annotator {

    companion object {
        // Softer white for better readability (instead of harsh 255,255,255)
        private val SOFT_WHITE = Color(240, 240, 240)
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.node.elementType) {
            QssTokenTypes.HEX_COLOR -> {
                // Handle hex colors: #FF0000, #0e639c, etc.
                val colorText = element.text
                val color = QssColorFormats.parseConcreteColor(colorText)
                if (color != null) {
                    applyBackgroundColor(element, holder, color)
                }
            }

            QssTokenTypes.RGB_FUNCTION -> {
                // Handle rgb(255, 0, 0)
                val colorText = element.text
                val color = QssColorFormats.parseConcreteColor(colorText)
                if (color != null) {
                    applyBackgroundColor(element, holder, color)
                }
            }

            QssTokenTypes.RGBA_FUNCTION -> {
                // Handle rgba(0, 0, 255, 0.5) - preserves alpha!
                val colorText = element.text
                val color = QssColorFormats.parseConcreteColor(colorText)
                if (color != null) {
                    applyBackgroundColor(element, holder, color)
                }
            }

            QssTokenTypes.COLOR_FUNCTION -> {
                val colorText = element.text
                val color = QssColorFormats.parseConcreteColor(colorText)
                if (color != null) {
                    applyBackgroundColor(element, holder, color)
                }
            }
        }
    }

    /**
     * Applies the actual background color with contrast-aware text color.
     * NO forced transparency - uses the exact color provided.
     */
    private fun applyBackgroundColor(
        element: PsiElement,
        holder: AnnotationHolder,
        color: Color
    ) {
        // Use the ACTUAL color (no forced transparency)
        val backgroundColor = color

        // Calculate contrast-aware text color based on the OPAQUE version
        // (We check contrast against the opaque color for consistency)
        val opaqueColor = Color(color.red, color.green, color.blue)
        val textColor = getContrastTextColor(opaqueColor)

        // Create custom text attributes with background and foreground colors
        val textAttributes = TextAttributes()
        textAttributes.backgroundColor = backgroundColor
        textAttributes.foregroundColor = textColor

        // Apply the colors
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element)
            .enforcedTextAttributes(textAttributes)
            .create()
    }

    /**
     * Calculates the appropriate text color (black or soft white) based on background luminance.
     * Uses the WCAG relative luminance formula for accessibility.
     *
     * @param backgroundColor The background color to calculate contrast for
     * @return Color.BLACK for bright backgrounds, SOFT_WHITE for dark backgrounds
     */
    private fun getContrastTextColor(backgroundColor: Color): Color {
        // Calculate relative luminance using WCAG formula
        // https://www.w3.org/TR/WCAG20/#relativeluminancedef
        val r = backgroundColor.red / 255.0
        val g = backgroundColor.green / 255.0
        val b = backgroundColor.blue / 255.0

        // Apply gamma correction
        val rLinear = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
        val gLinear = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
        val bLinear = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)

        // Calculate luminance
        val luminance = 0.2126 * rLinear + 0.7152 * gLinear + 0.0722 * bLinear

        // Return soft white text for dark backgrounds, black text for bright backgrounds
        return if (luminance > 0.5) Color.BLACK else SOFT_WHITE
    }
}
