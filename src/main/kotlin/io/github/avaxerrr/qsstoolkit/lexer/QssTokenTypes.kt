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
        val LPAREN = QssElementType("LPAREN") // (
        val RPAREN = QssElementType("RPAREN") // )
        val LBRACKET = QssElementType("LBRACKET") // [
        val RBRACKET = QssElementType("RBRACKET") // ]
        val EQUALS = QssElementType("EQUALS") // =
        val GT = QssElementType("GT") // >
        val EXCLAMATION = QssElementType("EXCLAMATION") // !
        val SLASH = QssElementType("SLASH") // /


        // Property values
        val STRING = QssElementType("STRING")
        val NUMBER = QssElementType("NUMBER")
        val HEX_COLOR = QssElementType("HEX_COLOR")
        val KEYWORD = QssElementType("KEYWORD")
        val URL = QssElementType("URL") // url(...)
        val TEMPLATE_TAG = QssElementType("TEMPLATE_TAG") // {{...}}

        // Color functions
        val RGB_FUNCTION = QssElementType("RGB_FUNCTION")    // rgb(...)
        val RGBA_FUNCTION = QssElementType("RGBA_FUNCTION")  // rgba(...)
        val GRADIENT = QssElementType("GRADIENT")            // qlineargradient(...)

        // Special selectors
        val HASH = QssElementType("HASH") // #
        val DOT = QssElementType("DOT") // .
        val ASTERISK = QssElementType("ASTERISK") // *
        val PSEUDO_STATE = QssElementType("PSEUDO_STATE") // :hover, etc.
        val PSEUDO_ELEMENT = QssElementType("PSEUDO_ELEMENT") // ::item, etc.

        // Widget Class (e.g. QPushButton) - identified by Uppercase start
        val WIDGET_CLASS = QssElementType("WIDGET_CLASS")

        // Invalid or unexpected token
        val BAD_CHARACTER = TokenType.BAD_CHARACTER
    }
}

class QssElementType(debugName: String) : IElementType(debugName, QssLanguage)