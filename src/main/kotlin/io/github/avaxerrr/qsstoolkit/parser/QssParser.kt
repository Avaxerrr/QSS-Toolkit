// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes
import io.github.avaxerrr.qsstoolkit.psi.QssTypes

class QssParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()

        while (!builder.eof()) {
            parseRule(builder)
        }

        rootMarker.done(root)
        return builder.treeBuilt
    }

    private fun parseRule(builder: PsiBuilder) {
        val ruleMarker = builder.mark()

        // Skip comments and whitespace
        while (builder.tokenType == QssTokenTypes.WHITE_SPACE ||
            builder.tokenType == QssTokenTypes.COMMENT) {
            builder.advanceLexer()
        }

        // Parse selectors
        if (!parseSelectors(builder)) {
            ruleMarker.drop()
            builder.advanceLexer() // Move forward if no selector found to avoid infinite loop
            return
        }

        // Expect opening brace
        if (builder.tokenType != QssTokenTypes.LBRACE) {
            builder.error("Expected '{' after selector")
            ruleMarker.done(QssTypes.RULE)
            return
        }
        builder.advanceLexer() // Consume {

        // Parse declarations
        while (!builder.eof() && builder.tokenType != QssTokenTypes.RBRACE) {

            // CHECK: Break if we see the start of a new rule (Recovery)
            // This prevents the parser from consuming the next rule if '}' is missing
            if (isStartOfNewRule(builder)) {
                builder.error("Missing '}'")
                break
            }

            parseDeclaration(builder)

            // Skip separators/whitespace
            while (builder.tokenType == QssTokenTypes.SEMICOLON ||
                builder.tokenType == QssTokenTypes.WHITE_SPACE) {
                builder.advanceLexer()
            }
        }

        // Expect closing brace
        if (builder.tokenType == QssTokenTypes.RBRACE) {
            builder.advanceLexer() // Consume }
        } else if (!isStartOfNewRule(builder)) {
            // Only error if we didn't already error in the loop
            // If we broke out of the loop due to new rule, we already flagged it
            // But if we hit EOF, we need to flag it here.
            builder.error("Expected '}'")
        }

        ruleMarker.done(QssTypes.RULE)
    }

    // Helper to detect if the current token looks like a new selector
    private fun isStartOfNewRule(builder: PsiBuilder): Boolean {
        val type = builder.tokenType
        // If we see a class selector (.), ID selector (#), or a Widget Name (Identifier that isn't a property)
        // This is heuristic but works for 95% of cases.
        // Note: We have to be careful not to confuse property names with widget names.
        // But inside a declaration block, a raw identifier is usually a property.
        // However, if we see "." or "#", it's definitely a new selector.

        if (type == QssTokenTypes.DOT || type == QssTokenTypes.HASH) {
            return true
        }

        // If we see a common Widget name (starts with Q), it's likely a new rule
        // This is a simple heuristic.
        if (type == QssTokenTypes.IDENTIFIER) {
            val text = builder.tokenText
            if (text != null && text.startsWith("Q") && text.length > 1 && text[1].isUpperCase()) {
                return true
            }
        }

        return false
    }

    private fun parseSelectors(builder: PsiBuilder): Boolean {
        var selectorFound = false

        while (!builder.eof() && builder.tokenType != QssTokenTypes.LBRACE) {
            if (builder.tokenType != QssTokenTypes.WHITE_SPACE &&
                builder.tokenType != QssTokenTypes.COMMENT &&
                builder.tokenType != QssTokenTypes.COMMA) {
                selectorFound = true
            }

            if (builder.tokenType == QssTokenTypes.LBRACE) break

            builder.advanceLexer()
        }

        return selectorFound
    }

    private fun parseDeclaration(builder: PsiBuilder) {
        // Skip whitespace
        if (builder.tokenType == QssTokenTypes.WHITE_SPACE) {
            builder.advanceLexer()
        }

        // If we hit a brace or EOF, stop
        if (builder.tokenType == QssTokenTypes.RBRACE || builder.eof()) return

        val declMarker = builder.mark()

        // Property name
        if (builder.tokenType == QssTokenTypes.IDENTIFIER ||
            builder.tokenType == QssTokenTypes.WIDGET_CLASS ||
            builder.tokenType == QssTokenTypes.KEYWORD) {
            builder.advanceLexer()
        } else {
            declMarker.drop()
            builder.advanceLexer() // consume unexpected token
            return
        }

        // Colon
        if (builder.tokenType == QssTokenTypes.COLON) {
            builder.advanceLexer()
        } else {
            builder.error("Expected ':' after property name")
            declMarker.done(QssTypes.DECLARATION)
            return
        }

        // Value (simplistic for now - consume until semicolon or brace)
        while (!builder.eof() &&
            builder.tokenType != QssTokenTypes.SEMICOLON &&
            builder.tokenType != QssTokenTypes.RBRACE &&
            !isStartOfNewRule(builder)) { // Add protection here too
            builder.advanceLexer()
        }

        declMarker.done(QssTypes.DECLARATION)
    }
}
