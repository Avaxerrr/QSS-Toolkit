package io.github.avaxerrr.qsstoolkit.validation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import io.github.avaxerrr.qsstoolkit.completion.QssData
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes
import io.github.avaxerrr.qsstoolkit.psi.QssTypes

class QssValidationAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.node.elementType == QssTypes.DECLARATION) {
            validateDeclaration(element, holder)
        }
    }

    private fun validateDeclaration(declaration: PsiElement, holder: AnnotationHolder) {
        var propertyNameElement: PsiElement? = null
        var colonElement: PsiElement? = null

        var child = declaration.firstChild
        while (child != null) {
            if (child.node.elementType == QssTokenTypes.IDENTIFIER) {
                propertyNameElement = child
            } else if (child.node.elementType == QssTokenTypes.COLON) {
                colonElement = child
                break
            }
            child = child.nextSibling
        }

        if (propertyNameElement == null || colonElement == null) return

        val propertyName = propertyNameElement.text
        if (propertyName.isNotEmpty() && propertyName[0].isUpperCase()) return

        if (!QssData.PROPERTIES.contains(propertyName)) {
            holder.newAnnotation(HighlightSeverity.WARNING, "Unknown property '$propertyName'")
                .range(propertyNameElement)
                .create()
        } else {
            validateValue(declaration, propertyName, colonElement, holder)
        }
    }

    private fun validateValue(declaration: PsiElement, propertyName: String, colonElement: PsiElement, holder: AnnotationHolder) {
        val expectedType = QssData.PROPERTY_TYPES[propertyName] ?: return

        var current = colonElement.nextSibling
        var valueText = ""
        var valueStartElement: PsiElement? = null
        var valueEndElement: PsiElement? = null
        var hasTemplateTag = false

        while (current != null) {
            val type = current.node.elementType
            if (type == QssTokenTypes.SEMICOLON || type == QssTokenTypes.RBRACE) break

            if (type == QssTokenTypes.TEMPLATE_TAG) {
                hasTemplateTag = true
            }

            if (type != QssTokenTypes.WHITE_SPACE && type != QssTokenTypes.COMMENT) {
                if (valueStartElement == null) valueStartElement = current
                valueEndElement = current
                valueText += current.text
            } else if (valueStartElement != null && type == QssTokenTypes.WHITE_SPACE) {
                valueText += " "
            }
            current = current.nextSibling
        }

        // SKIP validation if we found a template tag {{...}}
        if (hasTemplateTag) return

        if (valueStartElement == null || valueText.isEmpty()) return

        val trimmedValue = valueText.trim()

        val range = com.intellij.openapi.util.TextRange(
            valueStartElement.textRange.startOffset,
            valueEndElement!!.textRange.endOffset
        )

        var errorMsg: String? = null

        when (expectedType) {
            QssData.PropertyType.MEASUREMENT -> {
                if (!isValidMeasurement(trimmedValue)) {
                    errorMsg = "Invalid measurement: '$trimmedValue'. Expected number with unit (e.g., 10px) or 0"
                }
            }
            QssData.PropertyType.COLOR -> {
                if (!isValidColor(trimmedValue)) {
                    errorMsg = "Invalid color: '$trimmedValue'. Expected #hex, rgb(), named color, or gradient"
                }
            }
            QssData.PropertyType.NUMBER -> {
                if (trimmedValue.toDoubleOrNull() == null) {
                    errorMsg = "Invalid number: '$trimmedValue'"
                }
            }
            QssData.PropertyType.URL -> {
                if (trimmedValue != "none" && (!trimmedValue.startsWith("url(") || !trimmedValue.endsWith(")"))) {
                    errorMsg = "Invalid URL: '$trimmedValue'. Expected url(...) or 'none'"
                }
            }
            else -> {}
        }

        if (errorMsg != null) {
            holder.newAnnotation(HighlightSeverity.ERROR, errorMsg)
                .range(range)
                .create()
        }
    }

    private fun isValidMeasurement(value: String): Boolean {
        // Handle multi-value shorthand (e.g., "5px 10px", "1px 2px 3px 4px")
        val parts = value.split("\\s+".toRegex())
        if (parts.size > 4) return false

        val regex = Regex("^-?\\d+(\\.\\d+)?(px|pt|em|ex|%)$")

        return parts.all { part ->
            part == "0" || part == "auto" || regex.matches(part)
        }
    }

    private fun isValidColor(value: String): Boolean {
        if (value.startsWith("#")) {
            return value.length == 4 || value.length == 7 || value.length == 9
        }
        if (value.startsWith("rgb(") || value.startsWith("rgba(") ||
            value.startsWith("hsv(") || value.startsWith("hsva(")) {
            return value.endsWith(")")
        }
        if (value.startsWith("qlineargradient(") ||
            value.startsWith("qradialgradient(") ||
            value.startsWith("qconicalgradient(")) {
            return value.endsWith(")")
        }

        val commonColors = listOf("red", "green", "blue", "white", "black", "transparent", "yellow", "cyan", "magenta", "gray", "grey", "darkgray", "lightgray")
        return commonColors.contains(value.lowercase()) || value == "none"
    }
}