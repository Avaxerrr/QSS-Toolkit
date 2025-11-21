// QSS Toolkit version 1.1

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
        // 1. Validate Property Names
        // We look for IDENTIFIER tokens that are part of a Declaration Property
        // The structure is Declaration -> Property -> Identifier
        if (element.node.elementType == QssTokenTypes.IDENTIFIER) {
            val parent = element.parent
            // Check if this identifier is the property name in a declaration
            // Note: We need to be careful not to flag selectors or values

            // Logic: If parent is a PROPERTY, check its name
            // Since we don't have the full PSI tree structure handy in this script context,
            // we rely on the fact that property names are usually followed by a Colon (:).
            // However, using the parser structure is safer.

            // Assuming standard PSI structure: Property -> Identifier
            // We'll check if the text matches a known property

            // Heuristic: If we are inside a Block (Declaration), and this is the key.
            // This is simplified. A robust implementation checks: element.parent.node.elementType == QssTypes.PROPERTY
            // But let's check if the text *looks* like a property (lowercase, dashes) vs a Class (CamelCase)

            val text = element.text

            // Skip widget names (start with capital letter) or existing valid properties
            if (text.isNotEmpty() && !text[0].isUpperCase()) {
                // Check if it's in our known property list
                if (!QssData.PROPERTIES.contains(text)) {
                    // It might be a value (like "red" or "bold").
                    // We only want to flag it if it's in the "key" position.
                    // In a robust PSI, we'd check: element.parent is QssProperty

                    // For now, let's use a safe check: Is it followed by a COLON?
                    val nextSibling = element.nextSibling
                    // Skip whitespace
                    var next = nextSibling
                    while (next != null && next.node.elementType == QssTokenTypes.WHITE_SPACE) {
                        next = next.nextSibling
                    }

                    if (next != null && next.node.elementType == QssTokenTypes.COLON) {
                        holder.newAnnotation(HighlightSeverity.WARNING, "Unknown property '$text'")
                            .range(element)
                            .create()
                    }
                }
            }
        }
    }
}