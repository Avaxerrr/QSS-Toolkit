package io.github.avaxerrr.qsstoolkit.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import io.github.avaxerrr.qsstoolkit.lexer.QssLexer
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes

class QssSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer {
        return QssLexer()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        val attributes = when (tokenType) {
            QssTokenTypes.COMMENT -> QSS_COMMENT
            QssTokenTypes.IDENTIFIER -> QSS_IDENTIFIER // Default properties (Blue)
            QssTokenTypes.WIDGET_CLASS -> QSS_WIDGET // Widgets (Teal)
            QssTokenTypes.KEYWORD -> QSS_KEYWORD // Keywords (Orange)
            QssTokenTypes.STRING -> QSS_STRING // Strings (Green)
            QssTokenTypes.URL -> QSS_STRING // URLs (Green)
            QssTokenTypes.TEMPLATE_TAG -> QSS_TEMPLATE // Templates (Orange/Keyword color)
            QssTokenTypes.NUMBER -> QSS_NUMBER
            QssTokenTypes.HEX_COLOR -> QSS_COLOR
            QssTokenTypes.RGB_FUNCTION, QssTokenTypes.RGBA_FUNCTION, QssTokenTypes.GRADIENT -> QSS_COLOR
            QssTokenTypes.HASH -> QSS_ID_SELECTOR
            QssTokenTypes.DOT -> QSS_CLASS_SELECTOR
            QssTokenTypes.ASTERISK -> QSS_IDENTIFIER
            QssTokenTypes.PSEUDO_STATE -> QSS_PSEUDO_STATE
            QssTokenTypes.PSEUDO_ELEMENT -> QSS_PSEUDO_ELEMENT
            QssTokenTypes.LPAREN, QssTokenTypes.RPAREN,
            QssTokenTypes.LBRACE, QssTokenTypes.RBRACE,
            QssTokenTypes.LBRACKET, QssTokenTypes.RBRACKET,
            QssTokenTypes.EQUALS, QssTokenTypes.GT, QssTokenTypes.EXCLAMATION,
            QssTokenTypes.SLASH,
            QssTokenTypes.SEMICOLON, QssTokenTypes.COLON, QssTokenTypes.COMMA -> QSS_PUNCTUATION
            QssTokenTypes.BAD_CHARACTER -> QSS_BAD_CHARACTER
            else -> return emptyArray()
        }

        return arrayOf(attributes)
    }

    companion object {
        // Comments - gray, subtle
        val QSS_COMMENT = TextAttributesKey.createTextAttributesKey(
            "QSS_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )

        // Widget names and property names (background-color, padding) - cyan/teal
        val QSS_IDENTIFIER = TextAttributesKey.createTextAttributesKey(
            "QSS_IDENTIFIER",
            DefaultLanguageHighlighterColors.INSTANCE_METHOD
        )
        val QSS_WIDGET = TextAttributesKey.createTextAttributesKey(
            "QSS_WIDGET",
            DefaultLanguageHighlighterColors.STATIC_FIELD
        )
        val QSS_KEYWORD = TextAttributesKey.createTextAttributesKey(
            "QSS_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )

        // String values - green
        val QSS_STRING = TextAttributesKey.createTextAttributesKey(
            "QSS_STRING",
            DefaultLanguageHighlighterColors.STRING
        )
        val QSS_TEMPLATE = TextAttributesKey.createTextAttributesKey(
            "QSS_TEMPLATE",
            DefaultLanguageHighlighterColors.KEYWORD
        )
        val QSS_NUMBER = TextAttributesKey.createTextAttributesKey(
            "QSS_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )

        // Hex color values (#FF0000) - purple/magenta
        val QSS_COLOR = TextAttributesKey.createTextAttributesKey(
            "QSS_COLOR",
            DefaultLanguageHighlighterColors.CONSTANT
        )

        // ID selectors (#myButton) - gold/yellow (changed from purple)
        val QSS_ID_SELECTOR = TextAttributesKey.createTextAttributesKey(
            "QSS_ID_SELECTOR",
            DefaultLanguageHighlighterColors.STATIC_FIELD
        )

        // Class selectors (.myClass) - gold/yellow (changed from purple)
        val QSS_CLASS_SELECTOR = TextAttributesKey.createTextAttributesKey(
            "QSS_CLASS_SELECTOR",
            DefaultLanguageHighlighterColors.STATIC_FIELD
        )

        // Pseudo-states (:hover, :pressed) - keep yellow
        val QSS_PSEUDO_STATE = TextAttributesKey.createTextAttributesKey(
            "QSS_PSEUDO_STATE",
            DefaultLanguageHighlighterColors.METADATA
        )

        // Pseudo-elements (::item, ::indicator) - keep yellow
        val QSS_PSEUDO_ELEMENT = TextAttributesKey.createTextAttributesKey(
            "QSS_PSEUDO_ELEMENT",
            DefaultLanguageHighlighterColors.METADATA
        )

        // Punctuation (braces, semicolons, etc.)
        val QSS_PUNCTUATION = TextAttributesKey.createTextAttributesKey(
            "QSS_PUNCTUATION",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )

        // Bad/invalid characters
        val QSS_BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
            "QSS_BAD_CHARACTER",
            HighlighterColors.BAD_CHARACTER
        )
    }
}