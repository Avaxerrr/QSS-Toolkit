package io.github.avaxerrr.qsstoolkit.validation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.github.avaxerrr.qsstoolkit.completion.QssData
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes
import io.github.avaxerrr.qsstoolkit.psi.QssFile
import io.github.avaxerrr.qsstoolkit.psi.QssTypes
import java.util.Locale
import com.intellij.openapi.util.TextRange
import java.util.regex.Pattern

class QssValidationAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is QssFile) {
            validateDuplicates(element, holder)
        }

        if (element.node.elementType == QssTypes.RULE) {
            validateRuleSelectors(element, holder)
        }

        if (element.node.elementType == QssTypes.DECLARATION) {
            validateDeclaration(element, holder)
        }
    }

    private fun validateDuplicates(file: QssFile, holder: AnnotationHolder) {
        val allRules = PsiTreeUtil.findChildrenOfType(file, PsiElement::class.java)
            .filter { it.node.elementType == QssTypes.RULE }

        val seen = mutableMapOf<String, PsiElement>()

        for (rule in allRules) {
            val selectorText = getSelectorText(rule)
            if (selectorText.isEmpty()) continue

            if (seen.containsKey(selectorText)) {
                val selectorEnd = getSelectorEndOffset(rule)
                val range = TextRange(rule.textRange.startOffset, selectorEnd)

                holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Duplicate selector definition. Rules will be merged.")
                    .range(range)
                    .create()
            } else {
                seen[selectorText] = rule
            }
        }
    }

    private fun getSelectorText(rule: PsiElement): String {
        val sb = StringBuilder()
        var child = rule.firstChild
        while (child != null) {
            if (child.node.elementType == QssTokenTypes.LBRACE) break
            sb.append(child.text)
            child = child.nextSibling
        }
        return sb.toString().trim()
    }

    private fun getSelectorEndOffset(rule: PsiElement): Int {
        var child = rule.firstChild
        var lastSelectorChild = child
        while (child != null) {
            if (child.node.elementType == QssTokenTypes.LBRACE) break
            lastSelectorChild = child
            child = child.nextSibling
        }
        return lastSelectorChild?.textRange?.endOffset ?: rule.textRange.startOffset
    }

    private fun validateRuleSelectors(rule: PsiElement, holder: AnnotationHolder) {
        val selectorText = getSelectorText(rule)
        if (selectorText.isEmpty()) return

        var currentWidget: String? = null

        for (widget in QssData.WIDGET_SELECTORS) {
            if (selectorText.contains(widget, ignoreCase = true)) {
                currentWidget = widget
                break
            }
        }

        if (currentWidget != null) {
            val matcher = Pattern.compile("::([a-zA-Z0-9-]+)").matcher(selectorText)
            while (matcher.find()) {
                val subControlName = matcher.group(1)
                val fullSubControl = "::" + subControlName

                val validList = QssData.WIDGET_SUBCONTROLS[currentWidget]
                if (validList != null && !validList.contains(fullSubControl)) {
                    val startOffset = rule.textRange.startOffset + matcher.start()
                    val endOffset = rule.textRange.startOffset + matcher.end()

                    holder.newAnnotation(HighlightSeverity.ERROR, "'$fullSubControl' is not a valid sub-control for $currentWidget")
                        .range(TextRange(startOffset, endOffset))
                        .create()
                }
            }
        }

        var child = rule.firstChild
        while (child != null) {
            if (child.node.elementType == QssTokenTypes.LBRACE) break
            if (child.node.elementType == QssTokenTypes.IDENTIFIER) {
                val text = child.text
                if (!text.startsWith("#") && !text.startsWith(".")) {
                    val knownWidget = QssData.WIDGET_SELECTORS.firstOrNull { it.equals(text, ignoreCase = true) }
                    if (knownWidget != null && knownWidget != text) {
                        holder.newAnnotation(HighlightSeverity.ERROR, "Invalid casing. Did you mean '$knownWidget'?")
                            .range(child)
                            .create()
                    }
                }
            }
            child = child.nextSibling
        }
    }

    private fun validateDeclaration(declaration: PsiElement, holder: AnnotationHolder) {
        var propertyName = ""
        var foundColon = false

        var child = declaration.firstChild
        while (child != null) {
            val type = child.node.elementType

            if (!foundColon) {
                if (type == QssTokenTypes.IDENTIFIER) {
                    propertyName = child.text
                } else if (type == QssTokenTypes.COLON) {
                    foundColon = true
                }
            } else {
                if (type != QssTokenTypes.WHITE_SPACE &&
                    type != QssTokenTypes.SEMICOLON &&
                    type != QssTokenTypes.COMMENT &&
                    type != QssTokenTypes.RBRACE &&
                    type != QssTokenTypes.LPAREN &&
                    type != QssTokenTypes.RPAREN &&
                    type != QssTokenTypes.COMMA) {

                    if (propertyName.isNotEmpty()) {
                        validateValue(propertyName, child, holder)
                    }
                }
            }
            child = child.nextSibling
        }
    }

    private fun validateValue(propertyName: String, valueElement: PsiElement, holder: AnnotationHolder) {
        val expectedType = QssData.PROPERTY_TYPES[propertyName.lowercase(Locale.getDefault())] ?: return
        val valueText = valueElement.text.trim()

        val isValid = when (expectedType) {
            QssData.PropertyType.COLOR -> isColor(valueText)
            QssData.PropertyType.MEASUREMENT -> isMeasurement(valueText)
            QssData.PropertyType.NUMBER -> isNumber(valueText)
            QssData.PropertyType.URL -> valueText.startsWith("url(")
            else -> true
        }

        if (!isValid) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Invalid value for property '$propertyName'. Expected $expectedType")
                .range(valueElement)
                .create()
        }
    }

    private fun isColor(text: String): Boolean {
        if (text.startsWith("#")) return true
        val lower = text.lowercase()
        if (lower.startsWith("rgb") || lower.startsWith("hsv") || lower.startsWith("q") || lower.contains("gradient")) return true
        if (lower == "palette") return true
        // FIX: Allow hyphens in color names (e.g., highlighted-text)
        if (text.matches(Regex("^[a-zA-Z-]+$"))) return true
        return false
    }
    private fun isMeasurement(text: String): Boolean = text == "0" || text.matches(Regex("^-?\\d+(\\.\\d+)?(px|pt|em|ex)?$"))
    private fun isNumber(text: String): Boolean = text.matches(Regex("^-?\\d+(\\.\\d+)?$"))
}