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
            if (selectorText.startsWith(widget)) {
                currentWidget = widget
                break
            }
        }

        if (currentWidget == null) {
            for (widget in QssData.WIDGET_SELECTORS) {
                if (selectorText.contains(widget, ignoreCase = false)) {
                    currentWidget = widget
                    break
                }
            }
        }

        if (currentWidget != null) {
            val matcher = Pattern.compile("::([a-zA-Z0-9-]+)").matcher(selectorText)
            while (matcher.find()) {
                val subControlName = matcher.group(1)
                val fullSubControl = "::" + subControlName

                val widgetSubControls = QssData.WIDGET_SUBCONTROLS[currentWidget]

                if (widgetSubControls != null) {
                    if (!widgetSubControls.contains(fullSubControl)) {
                        val startOffset = rule.textRange.startOffset + matcher.start()
                        val endOffset = rule.textRange.startOffset + matcher.end()

                        holder.newAnnotation(
                            HighlightSeverity.ERROR,
                            "'$fullSubControl' is not a valid sub-control for $currentWidget. Valid sub-controls: ${widgetSubControls.joinToString(", ")}"
                        )
                            .range(TextRange(startOffset, endOffset))
                            .create()
                    }
                } else {
                    if (!QssData.COMMON_SUBCONTROLS.contains(fullSubControl)) {
                        val startOffset = rule.textRange.startOffset + matcher.start()
                        val endOffset = rule.textRange.startOffset + matcher.end()

                        holder.newAnnotation(
                            HighlightSeverity.WARNING,
                            "'$fullSubControl' may not be supported by $currentWidget"
                        )
                            .range(TextRange(startOffset, endOffset))
                            .create()
                    }
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
        var propertyNameStartElement: PsiElement? = null
        var propertyNameEndElement: PsiElement? = null
        var foundColon = false

        println("\n========== NEW DECLARATION ==========")

        var child = declaration.firstChild
        var tokenCounter = 0
        while (child != null) {
            val type = child.node.elementType

            println("TOKEN #\${tokenCounter++}: type=\${type}, text='\${child.text}'")

            if (!foundColon) {
                // Accept IDENTIFIER, WIDGET_CLASS, KEYWORD
                if (type == QssTokenTypes.IDENTIFIER ||
                    type == QssTokenTypes.WIDGET_CLASS ||
                    type == QssTokenTypes.KEYWORD) {

                    println("   → CAPTURED as property part")

                    if (propertyName.isEmpty()) {
                        propertyNameStartElement = child
                        println("   → Set as START element (offset: \${child.textRange.startOffset})")
                    }
                    propertyName += child.text
                    propertyNameEndElement = child
                    println("   → propertyName is now: '\$propertyName'")
                    println("   → Set as END element (offset: \${child.textRange.endOffset})")

                } else if (type == QssTokenTypes.COLON) {
                    println("   → COLON FOUND - Time to validate!")
                    foundColon = true

                    if (propertyName.isNotEmpty() && propertyNameStartElement != null && propertyNameEndElement != null) {
                        val normalizedProperty = propertyName.lowercase(Locale.getDefault())

                        println("   → propertyName: '\$propertyName'")
                        println("   → normalized: '\$normalizedProperty'")

                        val fullRange = TextRange(
                            propertyNameStartElement.textRange.startOffset,
                            propertyNameEndElement.textRange.endOffset
                        )

                        println("   → fullRange: \${fullRange.startOffset} to \${fullRange.endOffset}")

                        if (!QssData.PROPERTY_TYPES.containsKey(normalizedProperty)) {
                            println("   → ❌ Unknown property")
                            holder.newAnnotation(
                                HighlightSeverity.ERROR,
                                "Unknown property '\$propertyName'. Check spelling or refer to Qt documentation."
                            )
                                .range(fullRange)
                                .create()
                        } else {
                            println("   → ✓ Property exists in map")

                            val document = propertyNameStartElement.containingFile.viewProvider.document
                            if (document != null) {
                                val originalText = document.getText(fullRange)
                                println("   → Document text at range: '\$originalText'")
                                println("   → Has uppercase? \${originalText.any { it.isUpperCase() }}")

                                if (originalText.any { it.isUpperCase() }) {
                                    println("   → ⚠️ CREATING WARNING ANNOTATION")
                                    holder.newAnnotation(
                                        HighlightSeverity.WEAK_WARNING,
                                        "Property names should be lowercase. Use '\$normalizedProperty' instead of '\$originalText'"
                                    )
                                        .range(fullRange)
                                        .create()
                                    println("   → ✅ Warning annotation created")
                                } else {
                                    println("   → ℹ️ No uppercase detected, no warning")
                                }
                            } else {
                                println("   → ❌ Document is null!")
                            }
                        }
                    } else {
                        println("   → ⚠️ propertyName is empty or elements are null")
                    }
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

        println("========== END DECLARATION ==========\n")
    }

    private fun validateValue(propertyName: String, valueElement: PsiElement, holder: AnnotationHolder) {
        val expectedType = QssData.PROPERTY_TYPES[propertyName.lowercase(Locale.getDefault())] ?: return
        val valueText = valueElement.text.trim()

        if (isTemplateVariable(valueText)) {
            return
        }

        val validationResult = when (expectedType) {
            QssData.PropertyType.COLOR -> validateColor(valueText)
            QssData.PropertyType.MEASUREMENT -> validateMeasurement(valueText)
            QssData.PropertyType.NUMBER -> validateNumber(valueText, propertyName)
            QssData.PropertyType.URL -> validateUrl(valueText)
            QssData.PropertyType.STRING -> ValidationResult.Valid
            QssData.PropertyType.BORDER -> validateBorder(valueText)
            else -> ValidationResult.Valid
        }

        if (validationResult is ValidationResult.Invalid) {
            holder.newAnnotation(HighlightSeverity.ERROR, validationResult.message)
                .range(valueElement)
                .create()
        } else if (validationResult is ValidationResult.Warning) {
            holder.newAnnotation(HighlightSeverity.WEAK_WARNING, validationResult.message)
                .range(valueElement)
                .create()
        }
    }

    private sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
        data class Warning(val message: String) : ValidationResult()
    }

    private fun isTemplateVariable(text: String): Boolean {
        return text.matches(Regex("^\\{\\{[^}]+\\}\\}$"))
    }

    private fun validateMeasurement(text: String): ValidationResult {
        if (text == "0" || text == "0.0") {
            return ValidationResult.Valid
        }

        val keywords = setOf("auto", "none")
        if (keywords.contains(text.lowercase())) {
            return ValidationResult.Valid
        }

        val measurementPattern = Regex("^-?\\d+(\\.\\d+)?(px|pt|em|ex)$")
        if (text.matches(measurementPattern)) {
            return ValidationResult.Valid
        }

        val numberWithoutUnit = Regex("^-?\\d+(\\.\\d+)?$")
        if (text.matches(numberWithoutUnit)) {
            return ValidationResult.Invalid(
                "Measurement requires a unit (px, pt, em, ex). Did you mean '\${text}px'?"
            )
        }

        return ValidationResult.Invalid(
            "Invalid measurement value '\$text'. Expected format: '10px', '1.5em', or '0'"
        )
    }

    private fun validateNumber(text: String, propertyName: String): ValidationResult {
        if (text.matches(Regex("^-?\\d+(\\.\\d+)?$"))) {
            val numValue = text.toDoubleOrNull()

            if (propertyName.lowercase() == "opacity" && numValue != null) {
                if (numValue < 0.0 || numValue > 1.0) {
                    return ValidationResult.Warning(
                        "Opacity value should be between 0.0 and 1.0. Value '\$text' will be clamped by Qt."
                    )
                }
            }

            return ValidationResult.Valid
        }

        if (text == "0" || text == "1" || text == "true" || text == "false") {
            return ValidationResult.Valid
        }

        return ValidationResult.Invalid(
            "Invalid number value '\$text'. Expected a numeric value (e.g., '0.5', '1', '100')"
        )
    }

    private fun validateUrl(text: String): ValidationResult {
        val lower = text.lowercase()

        if (!lower.startsWith("url(")) {
            return ValidationResult.Invalid(
                "URL must use url() function. Example: url(path/to/file.png)"
            )
        }

        if (text.length <= 5) {
            return ValidationResult.Invalid(
                "Empty url() function. Provide a file path: url(icon.png)"
            )
        }

        val content = text.substring(4, text.length - 1).trim()
        if (content.isEmpty() || content == "\"\"" || content == "''") {
            return ValidationResult.Invalid(
                "Empty url() function. Provide a file path: url(icon.png)"
            )
        }

        return ValidationResult.Valid
    }

    private fun validateBorder(text: String): ValidationResult {
        val lower = text.lowercase()

        if (lower.contains("url(")) {
            if (lower.matches(Regex(".*url\\(\\s*\\).*"))) {
                return ValidationResult.Invalid(
                    "Empty url() function. Provide a file path: url(icon.png)"
                )
            }
        }

        val borderKeywords = setOf("thick", "thin", "medium")
        if (borderKeywords.contains(lower) ||
            (lower.split("\\s+".toRegex()).size == 1 &&
                    setOf("solid", "dashed", "dotted", "double", "groove", "ridge", "inset", "outset").contains(lower))) {
            return ValidationResult.Warning(
                "Incomplete border declaration. Consider specifying width, style, and color (e.g., '1px solid red')"
            )
        }

        return ValidationResult.Valid
    }

    private fun validateColor(text: String): ValidationResult {
        if (text.matches(Regex("^#[0-9a-fA-F]{3}$")) ||
            text.matches(Regex("^#[0-9a-fA-F]{6}$"))) {
            return ValidationResult.Valid
        }

        val lower = text.lowercase()

        if (lower.startsWith("rgb(") || lower.startsWith("rgba(") ||
            lower.startsWith("hsv(") || lower.startsWith("hsva(") ||
            lower.startsWith("hsl(") || lower.startsWith("hsla(") ||
            lower.startsWith("qlineargradient(") ||
            lower.startsWith("qradialgradient(") ||
            lower.startsWith("qconicalgradient(")) {
            return ValidationResult.Valid
        }

        if (lower == "palette" || lower == "transparent" || lower == "none") {
            return ValidationResult.Valid
        }

        if (isValidNamedColor(lower)) {
            return ValidationResult.Valid
        }

        return ValidationResult.Invalid(
            "Invalid color value '\$text'. Use hex (#RRGGBB), rgb(r,g,b), rgba(r,g,b,a), or a valid color name"
        )
    }

    private fun isValidNamedColor(colorName: String): Boolean {
        val validColors = setOf(
            "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque",
            "black", "blanchedalmond", "blue", "blueviolet", "brown", "burlywood", "cadetblue",
            "chartreuse", "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson", "cyan",
            "darkblue", "darkcyan", "darkgoldenrod", "darkgray", "darkgreen", "darkgrey",
            "darkkhaki", "darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred",
            "darksalmon", "darkseagreen", "darkslateblue", "darkslategray", "darkslategrey",
            "darkturquoise", "darkviolet", "deeppink", "deepskyblue", "dimgray", "dimgrey",
            "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro",
            "ghostwhite", "gold", "goldenrod", "gray", "green", "greenyellow", "grey", "honeydew",
            "hotpink", "indianred", "indigo", "ivory", "khaki", "lavender", "lavenderblush",
            "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan",
            "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink",
            "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey",
            "lightsteelblue", "lightyellow", "lime", "limegreen", "linen", "magenta", "maroon",
            "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen",
            "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred",
            "midnightblue", "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace",
            "olive", "olivedrab", "orange", "orangered", "orchid", "palegoldenrod", "palegreen",
            "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum",
            "powderblue", "purple", "red", "rosybrown", "royalblue", "saddlebrown", "salmon",
            "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue", "slateblue",
            "slategray", "slategrey", "snow", "springgreen", "steelblue", "tan", "teal", "thistle",
            "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke", "yellow", "yellowgreen"
        )
        return validColors.contains(colorName)
    }
}