// QSS Toolkit version 1.0

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
            QssTokenTypes.IDENTIFIER -> QSS_IDENTIFIER
            QssTokenTypes.KEYWORD -> QSS_KEYWORD
            QssTokenTypes.STRING -> QSS_STRING
            QssTokenTypes.NUMBER -> QSS_NUMBER
            QssTokenTypes.HEX_COLOR -> QSS_COLOR
            QssTokenTypes.HASH, QssTokenTypes.DOT -> QSS_SELECTOR
            QssTokenTypes.PSEUDO_STATE -> QSS_PSEUDO_STATE
            QssTokenTypes.LBRACE, QssTokenTypes.RBRACE,
            QssTokenTypes.SEMICOLON, QssTokenTypes.COLON, QssTokenTypes.COMMA -> QSS_PUNCTUATION
            QssTokenTypes.BAD_CHARACTER -> QSS_BAD_CHARACTER
            else -> return emptyArray()
        }

        return arrayOf(attributes)
    }

    companion object {
        // Define TextAttributesKey for each token type
        val QSS_COMMENT = TextAttributesKey.createTextAttributesKey("QSS_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val QSS_IDENTIFIER = TextAttributesKey.createTextAttributesKey("QSS_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
        val QSS_KEYWORD = TextAttributesKey.createTextAttributesKey("QSS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val QSS_STRING = TextAttributesKey.createTextAttributesKey("QSS_STRING", DefaultLanguageHighlighterColors.STRING)
        val QSS_NUMBER = TextAttributesKey.createTextAttributesKey("QSS_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val QSS_COLOR = TextAttributesKey.createTextAttributesKey("QSS_COLOR", DefaultLanguageHighlighterColors.CONSTANT)
        val QSS_SELECTOR = TextAttributesKey.createTextAttributesKey("QSS_SELECTOR", DefaultLanguageHighlighterColors.CLASS_NAME)
        val QSS_PSEUDO_STATE = TextAttributesKey.createTextAttributesKey("QSS_PSEUDO_STATE", DefaultLanguageHighlighterColors.METADATA)
        val QSS_PUNCTUATION = TextAttributesKey.createTextAttributesKey("QSS_PUNCTUATION", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val QSS_BAD_CHARACTER = TextAttributesKey.createTextAttributesKey("QSS_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)
    }
}
