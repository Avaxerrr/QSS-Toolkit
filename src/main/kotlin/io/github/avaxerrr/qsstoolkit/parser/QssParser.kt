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
            if (!builder.eof()) {
                builder.advanceLexer() // Skip token to avoid infinite loop
            }
            return
        }

        // Skip more whitespace
        while (builder.tokenType == QssTokenTypes.WHITE_SPACE ||
            builder.tokenType == QssTokenTypes.COMMENT) {
            builder.advanceLexer()
        }

        // Expect opening brace
        if (builder.tokenType != QssTokenTypes.LBRACE) {
            ruleMarker.drop()
            if (!builder.eof()) {
                builder.advanceLexer()
            }
            return
        }

        builder.advanceLexer() // Consume {

        // Parse declarations
        while (!builder.eof() && builder.tokenType != QssTokenTypes.RBRACE) {
            parseDeclaration(builder)
        }

        // Expect closing brace
        if (builder.tokenType == QssTokenTypes.RBRACE) {
            builder.advanceLexer()
        }

        ruleMarker.done(QssTypes.RULE)
    }

    private fun parseSelectors(builder: PsiBuilder): Boolean {
        val selectorMarker = builder.mark()
        var hasSelectorComponents = false

        while (!builder.eof() && builder.tokenType != QssTokenTypes.LBRACE) {
            when (builder.tokenType) {
                QssTokenTypes.IDENTIFIER, QssTokenTypes.HASH, QssTokenTypes.DOT,
                QssTokenTypes.ASTERISK, QssTokenTypes.GT, QssTokenTypes.EXCLAMATION,  // ADD GT and EXCLAMATION
                QssTokenTypes.PSEUDO_STATE, QssTokenTypes.PSEUDO_ELEMENT,
                QssTokenTypes.COMMA -> {
                    hasSelectorComponents = true
                    builder.advanceLexer()
                }
                // Handle attribute selectors
                QssTokenTypes.LBRACKET -> {
                    hasSelectorComponents = true
                    parseAttributeSelector(builder)
                }
                QssTokenTypes.WHITE_SPACE, QssTokenTypes.COMMENT -> {
                    builder.advanceLexer()
                }
                QssTokenTypes.COLON -> {
                    // Special handling for colon that might be part of a property declaration
                    if (!hasSelectorComponents) {
                        selectorMarker.drop()
                        return false
                    }
                    // Otherwise, treat it as part of the selector
                    hasSelectorComponents = true
                    builder.advanceLexer()
                }
                else -> {
                    // This is hit when we encounter an unexpected token
                    if (hasSelectorComponents) {
                        selectorMarker.done(QssTypes.SELECTOR_LIST)
                        return true
                    }
                    selectorMarker.drop()
                    return hasSelectorComponents
                }
            }
        }

        if (hasSelectorComponents) {
            selectorMarker.done(QssTypes.SELECTOR_LIST)
        } else {
            selectorMarker.drop()
        }

        return hasSelectorComponents
    }


    private fun parseAttributeSelector(builder: PsiBuilder) {
        // Consume the opening bracket [
        if (builder.tokenType == QssTokenTypes.LBRACKET) {
            builder.advanceLexer()
        }

        // Skip whitespace
        while (builder.tokenType == QssTokenTypes.WHITE_SPACE) {
            builder.advanceLexer()
        }

        // Expect attribute name (IDENTIFIER)
        if (builder.tokenType == QssTokenTypes.IDENTIFIER) {
            builder.advanceLexer()

            // Skip whitespace
            while (builder.tokenType == QssTokenTypes.WHITE_SPACE) {
                builder.advanceLexer()
            }

            // Optional: = and value
            if (builder.tokenType == QssTokenTypes.EQUALS) {
                builder.advanceLexer()

                // Skip whitespace
                while (builder.tokenType == QssTokenTypes.WHITE_SPACE) {
                    builder.advanceLexer()
                }

                // Expect value (STRING or IDENTIFIER)
                if (builder.tokenType == QssTokenTypes.STRING ||
                    builder.tokenType == QssTokenTypes.IDENTIFIER ||
                    builder.tokenType == QssTokenTypes.NUMBER) {
                    builder.advanceLexer()
                }
            }
        }

        // Skip whitespace
        while (builder.tokenType == QssTokenTypes.WHITE_SPACE) {
            builder.advanceLexer()
        }

        // Expect closing bracket ]
        if (builder.tokenType == QssTokenTypes.RBRACKET) {
            builder.advanceLexer()
        } else {
            builder.error("Expected ']'")
        }
    }

    private fun parseDeclaration(builder: PsiBuilder) {
        val declMarker = builder.mark()

        // Skip whitespace and comments
        while (builder.tokenType == QssTokenTypes.WHITE_SPACE ||
            builder.tokenType == QssTokenTypes.COMMENT) {
            builder.advanceLexer()
        }

        // Expect property name (identifier)
        if (builder.tokenType != QssTokenTypes.IDENTIFIER) {
            declMarker.drop()
            // Skip until semicolon or closing brace to recover
            while (!builder.eof() &&
                builder.tokenType != QssTokenTypes.SEMICOLON &&
                builder.tokenType != QssTokenTypes.RBRACE) {
                builder.advanceLexer()
            }
            if (builder.tokenType == QssTokenTypes.SEMICOLON) {
                builder.advanceLexer()
            }
            return
        }

        builder.advanceLexer() // Property name

        // Skip whitespace
        while (builder.tokenType == QssTokenTypes.WHITE_SPACE ||
            builder.tokenType == QssTokenTypes.COMMENT) {
            builder.advanceLexer()
        }

        // Expect colon
        if (builder.tokenType != QssTokenTypes.COLON) {
            declMarker.drop()
            // Skip until semicolon or closing brace
            while (!builder.eof() &&
                builder.tokenType != QssTokenTypes.SEMICOLON &&
                builder.tokenType != QssTokenTypes.RBRACE) {
                builder.advanceLexer()
            }
            if (builder.tokenType == QssTokenTypes.SEMICOLON) {
                builder.advanceLexer()
            }
            return
        }

        builder.advanceLexer() // Consume the colon

        // Skip whitespace
        while (builder.tokenType == QssTokenTypes.WHITE_SPACE ||
            builder.tokenType == QssTokenTypes.COMMENT) {
            builder.advanceLexer()
        }

        // Parse property values
        if (!parsePropertyValues(builder)) {
            declMarker.drop()
            // Skip to semicolon
            while (!builder.eof() &&
                builder.tokenType != QssTokenTypes.SEMICOLON &&
                builder.tokenType != QssTokenTypes.RBRACE) {
                builder.advanceLexer()
            }
            if (builder.tokenType == QssTokenTypes.SEMICOLON) {
                builder.advanceLexer()
            }
            return
        }

        // Skip whitespace
        while (builder.tokenType == QssTokenTypes.WHITE_SPACE ||
            builder.tokenType == QssTokenTypes.COMMENT) {
            builder.advanceLexer()
        }

        // Expect semicolon
        if (builder.tokenType == QssTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }

        declMarker.done(QssTypes.DECLARATION)
    }

    private fun parsePropertyValues(builder: PsiBuilder): Boolean {
        val valuesMarker = builder.mark()
        var hasValues = false

        while (!builder.eof() &&
            builder.tokenType != QssTokenTypes.SEMICOLON &&
            builder.tokenType != QssTokenTypes.RBRACE) {

            // Skip whitespace and comments
            if (builder.tokenType == QssTokenTypes.WHITE_SPACE ||
                builder.tokenType == QssTokenTypes.COMMENT) {
                builder.advanceLexer()
                continue
            }

            // Handle comma
            if (builder.tokenType == QssTokenTypes.COMMA) {
                builder.advanceLexer()
                continue
            }

            // Parse value tokens
            when (builder.tokenType) {
                QssTokenTypes.IDENTIFIER, QssTokenTypes.STRING, QssTokenTypes.NUMBER,
                QssTokenTypes.HEX_COLOR, QssTokenTypes.RGB_FUNCTION, QssTokenTypes.RGBA_FUNCTION,
                QssTokenTypes.LPAREN, QssTokenTypes.RPAREN,  // For url()
                QssTokenTypes.COLON, QssTokenTypes.SLASH -> {  // ADD SLASH for paths
                    hasValues = true
                    builder.advanceLexer()  // Just consume these tokens as part of values
                }
                else -> {
                    // Unknown token - stop parsing values
                    break
                }
            }
        }

        if (hasValues) {
            valuesMarker.done(QssTypes.PROPERTY_VALUES)
        } else {
            valuesMarker.drop()
        }

        return hasValues
    }

    private fun parsePropertyValue(builder: PsiBuilder) {
        val valueMarker = builder.mark()

        when (builder.tokenType) {
            QssTokenTypes.HEX_COLOR -> {
                val colorMarker = builder.mark()
                builder.advanceLexer()
                colorMarker.done(QssTypes.COLOR_VALUE)
            }
            QssTokenTypes.RGB_FUNCTION, QssTokenTypes.RGBA_FUNCTION -> {
                val colorMarker = builder.mark()
                builder.advanceLexer()
                colorMarker.done(QssTypes.COLOR_VALUE)
            }
            QssTokenTypes.NUMBER -> {
                val numberMarker = builder.mark()
                builder.advanceLexer()
                numberMarker.done(QssTypes.NUMBER_VALUE)
            }
            QssTokenTypes.STRING -> {
                val stringMarker = builder.mark()
                builder.advanceLexer()
                stringMarker.done(QssTypes.STRING_VALUE)
            }
            QssTokenTypes.IDENTIFIER -> {
                val identMarker = builder.mark()
                builder.advanceLexer()
                identMarker.done(QssTypes.IDENTIFIER_VALUE)
            }
            else -> {
                builder.advanceLexer()
            }
        }

        valueMarker.done(QssTypes.PROPERTY_VALUE)
    }
}
