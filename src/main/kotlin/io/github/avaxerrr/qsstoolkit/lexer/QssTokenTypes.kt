package io.github.avaxerrr.qsstoolkit.lexer

import com.intellij.psi.tree.IElementType
import com.intellij.psi.TokenType
import io.github.avaxerrr.qsstoolkit.QssLanguage

interface QssTokenTypes {
    companion object {
        // Basic tokens
        val WHITE_SPACE = TokenType.WHITE_SPACE
        val COMMENT = QssElementType("COMMENT")

        // Identifiers
        val IDENTIFIER = QssElementType("IDENTIFIER")

        // Punctuation
        val LBRACE = QssElementType("LBRACE") // {
        val RBRACE = QssElementType("RBRACE") // }
        val SEMICOLON = QssElementType("SEMICOLON") // ;
        val COLON = QssElementType("COLON") // :
        val COMMA = QssElementType("COMMA") // ,

        // Property values
        val STRING = QssElementType("STRING")
        val NUMBER = QssElementType("NUMBER")
        val HEX_COLOR = QssElementType("HEX_COLOR")
        val KEYWORD = QssElementType("KEYWORD")

        // Special selectors
        val HASH = QssElementType("HASH") // #
        val DOT = QssElementType("DOT") // .
        val PSEUDO_STATE = QssElementType("PSEUDO_STATE") // :hover, etc.

        // Invalid or unexpected token
        val BAD_CHARACTER = TokenType.BAD_CHARACTER
    }
}

class QssElementType(debugName: String) : IElementType(debugName, QssLanguage)
