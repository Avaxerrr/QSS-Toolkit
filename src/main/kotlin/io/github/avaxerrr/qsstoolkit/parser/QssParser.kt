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
        if (!expectToken(builder, QssTokenTypes.LBRACE)) {
            builder.error("Expected '{'")
            ruleMarker.drop()
            return
        }

        // Parse declarations
        parseDeclarations(builder)

        // Expect closing brace
        expectToken(builder, QssTokenTypes.RBRACE)

        ruleMarker.done(QssTypes.RULE)
    }


    private fun parseSelectors(builder: PsiBuilder): Boolean {
        val selectorMarker = builder.mark()
        var hasSelectorComponents = false

        while (!builder.eof() && builder.tokenType != QssTokenTypes.LBRACE) {
            when (builder.tokenType) {
                QssTokenTypes.IDENTIFIER, QssTokenTypes.HASH, QssTokenTypes.DOT,
                QssTokenTypes.PSEUDO_STATE, QssTokenTypes.PSEUDO_ELEMENT,
                QssTokenTypes.COMMA -> {
                    hasSelectorComponents = true
                    builder.advanceLexer()
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


    private fun parseDeclarations(builder: PsiBuilder) {
        while (!builder.eof() && builder.tokenType != QssTokenTypes.RBRACE) {
            when (builder.tokenType) {
                QssTokenTypes.IDENTIFIER -> parseDeclaration(builder)
                else -> builder.advanceLexer()
            }
        }
    }

    private fun parseDeclaration(builder: PsiBuilder) {
        val declarationMarker = builder.mark()

        // Property name
        builder.advanceLexer() // Consume identifier

        // Expect colon
        if (!expectToken(builder, QssTokenTypes.COLON)) {
            declarationMarker.drop()
            return
        }

        // Parse property value(s)
        if (!parsePropertyValues(builder)) {
            declarationMarker.drop()
            return
        }

        // Expect semicolon (optional)
        if (builder.tokenType == QssTokenTypes.SEMICOLON) {
            builder.advanceLexer()
        }

        declarationMarker.done(QssTypes.DECLARATION)
    }

    private fun parsePropertyValues(builder: PsiBuilder): Boolean {
        val valuesMarker = builder.mark()
        var hasValues = false

        while (!builder.eof() &&
            builder.tokenType != QssTokenTypes.SEMICOLON &&
            builder.tokenType != QssTokenTypes.RBRACE) {
            when (builder.tokenType) {
                QssTokenTypes.IDENTIFIER, QssTokenTypes.STRING, QssTokenTypes.NUMBER,
                QssTokenTypes.HEX_COLOR -> {
                    hasValues = true
                    parsePropertyValue(builder)
                }
                QssTokenTypes.WHITE_SPACE, QssTokenTypes.COMMENT, QssTokenTypes.COMMA -> {
                    builder.advanceLexer()
                }
                else -> {
                    builder.advanceLexer() // Skip unexpected token
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

    private fun expectToken(builder: PsiBuilder, tokenType: IElementType): Boolean {
        if (builder.tokenType == tokenType) {
            builder.advanceLexer()
            return true
        }

        builder.error("Expected " + tokenType.toString())
        return false
    }
}
