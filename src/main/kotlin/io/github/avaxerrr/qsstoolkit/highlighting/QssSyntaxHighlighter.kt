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
            // Comments
            QssTokenTypes.COMMENT -> QSS_COMMENT

            // Selectors & Structure
            QssTokenTypes.WIDGET_CLASS -> QSS_WIDGET_CLASS  // QPushButton (Purple/Bold)
            QssTokenTypes.IDENTIFIER -> QSS_PROPERTY        // background-color (Cyan/Yellow - DISTINCT!)
            QssTokenTypes.HASH -> QSS_ID_SELECTOR           // # in #myButton (Purple/Bold)
            QssTokenTypes.DOT -> QSS_CLASS_SELECTOR         // . in .myClass (Purple/Bold)
            QssTokenTypes.ASTERISK -> QSS_OPERATOR          // * universal selector

            // Pseudo-selectors
            QssTokenTypes.PSEUDO_STATE -> QSS_PSEUDO_STATE      // :hover, :pressed
            QssTokenTypes.PSEUDO_ELEMENT -> QSS_PSEUDO_ELEMENT  // ::item, ::indicator

            // Values - Keywords
            QssTokenTypes.KEYWORD -> QSS_KEYWORD            // bold, center, solid (Orange)

            // Values - Literals
            QssTokenTypes.STRING -> QSS_STRING              // "Arial", 'text' (Green)
            QssTokenTypes.NUMBER -> QSS_NUMBER              // 12, 1.5 (Blue)
            QssTokenTypes.HEX_COLOR -> QSS_HEX_COLOR        // #FF0000 (Purple)

            // Functions
            QssTokenTypes.RGB_FUNCTION -> QSS_FUNCTION      // rgb(...) (Yellow)
            QssTokenTypes.RGBA_FUNCTION -> QSS_FUNCTION     // rgba(...) (Yellow)
            QssTokenTypes.GRADIENT -> QSS_FUNCTION          // qlineargradient(...) (Yellow)
            QssTokenTypes.URL -> QSS_FUNCTION               // url(...) (Yellow)

            // Template variables
            QssTokenTypes.TEMPLATE_TAG -> QSS_TEMPLATE      // {{VARIABLE}} (Yellow-Green)

            // Punctuation - Paired symbols
            QssTokenTypes.LBRACE, QssTokenTypes.RBRACE -> QSS_BRACES        // { }
            QssTokenTypes.LBRACKET, QssTokenTypes.RBRACKET -> QSS_BRACKETS  // [ ]
            QssTokenTypes.LPAREN, QssTokenTypes.RPAREN -> QSS_PARENTHESES   // ( )

            // Punctuation - Separators
            QssTokenTypes.SEMICOLON -> QSS_SEMICOLON        // ;
            QssTokenTypes.COLON -> QSS_COLON                // :
            QssTokenTypes.COMMA -> QSS_COMMA                // ,

            // Operators
            QssTokenTypes.EQUALS -> QSS_OPERATOR            // =
            QssTokenTypes.GT -> QSS_OPERATOR                // >
            QssTokenTypes.EXCLAMATION -> QSS_OPERATOR       // !
            QssTokenTypes.SLASH -> QSS_OPERATOR             // /

            // Invalid characters
            QssTokenTypes.BAD_CHARACTER -> QSS_BAD_CHARACTER

            else -> return emptyArray()
        }

        return arrayOf(attributes)
    }

    companion object {
        // ═══════════════════════════════════════════════════════════════════════════
        // STRUCTURE & SELECTORS - THE REAL FIX (OPTION 4)
        // ═══════════════════════════════════════════════════════════════════════════

        // Widget class names (QPushButton, QLabel) - PURPLE (always visible)
        val QSS_WIDGET_CLASS = TextAttributesKey.createTextAttributesKey(
            "QSS_WIDGET_CLASS",
            DefaultLanguageHighlighterColors.CONSTANT
        )

        // Property names (background-color, padding) - CYAN/YELLOW (GUARANTEED DISTINCT!)
        // INSTANCE_METHOD is NEVER the same color as CONSTANT in any theme
        val QSS_PROPERTY = TextAttributesKey.createTextAttributesKey(
            "QSS_PROPERTY",
            DefaultLanguageHighlighterColors.INSTANCE_METHOD
        )

        // ID selectors (#myButton) - Purple/Bold
        val QSS_ID_SELECTOR = TextAttributesKey.createTextAttributesKey(
            "QSS_ID_SELECTOR",
            DefaultLanguageHighlighterColors.CONSTANT
        )

        // Class selectors (.myClass) - Purple/Bold
        val QSS_CLASS_SELECTOR = TextAttributesKey.createTextAttributesKey(
            "QSS_CLASS_SELECTOR",
            DefaultLanguageHighlighterColors.CONSTANT
        )

        // ═══════════════════════════════════════════════════════════════════════════
        // PSEUDO-SELECTORS
        // ═══════════════════════════════════════════════════════════════════════════

        // Pseudo-states (:hover, :pressed) - Yellow-Green
        val QSS_PSEUDO_STATE = TextAttributesKey.createTextAttributesKey(
            "QSS_PSEUDO_STATE",
            DefaultLanguageHighlighterColors.METADATA
        )

        // Pseudo-elements (::item, ::indicator) - Yellow-Green
        val QSS_PSEUDO_ELEMENT = TextAttributesKey.createTextAttributesKey(
            "QSS_PSEUDO_ELEMENT",
            DefaultLanguageHighlighterColors.METADATA
        )

        // ═══════════════════════════════════════════════════════════════════════════
        // VALUES
        // ═══════════════════════════════════════════════════════════════════════════

        // Keywords (bold, center, solid) - Orange
        val QSS_KEYWORD = TextAttributesKey.createTextAttributesKey(
            "QSS_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )

        // String values ("Arial", 'text') - Green
        val QSS_STRING = TextAttributesKey.createTextAttributesKey(
            "QSS_STRING",
            DefaultLanguageHighlighterColors.STRING
        )

        // Numbers (12, 1.5) - Blue
        val QSS_NUMBER = TextAttributesKey.createTextAttributesKey(
            "QSS_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )

        // Hex colors (#FF0000) - Purple
        val QSS_HEX_COLOR = TextAttributesKey.createTextAttributesKey(
            "QSS_HEX_COLOR",
            DefaultLanguageHighlighterColors.CONSTANT
        )

        // ═══════════════════════════════════════════════════════════════════════════
        // FUNCTIONS
        // ═══════════════════════════════════════════════════════════════════════════

        // Functions (rgb(), url(), qlineargradient()) - Yellow
        val QSS_FUNCTION = TextAttributesKey.createTextAttributesKey(
            "QSS_FUNCTION",
            DefaultLanguageHighlighterColors.FUNCTION_CALL
        )

        // Template variables ({{VARIABLE}}) - Yellow-Green
        val QSS_TEMPLATE = TextAttributesKey.createTextAttributesKey(
            "QSS_TEMPLATE",
            DefaultLanguageHighlighterColors.METADATA
        )

        // ═══════════════════════════════════════════════════════════════════════════
        // PUNCTUATION
        // ═══════════════════════════════════════════════════════════════════════════

        // Braces { }
        val QSS_BRACES = TextAttributesKey.createTextAttributesKey(
            "QSS_BRACES",
            DefaultLanguageHighlighterColors.BRACES
        )

        // Brackets [ ]
        val QSS_BRACKETS = TextAttributesKey.createTextAttributesKey(
            "QSS_BRACKETS",
            DefaultLanguageHighlighterColors.BRACKETS
        )

        // Parentheses ( )
        val QSS_PARENTHESES = TextAttributesKey.createTextAttributesKey(
            "QSS_PARENTHESES",
            DefaultLanguageHighlighterColors.PARENTHESES
        )

        // Semicolon ;
        val QSS_SEMICOLON = TextAttributesKey.createTextAttributesKey(
            "QSS_SEMICOLON",
            DefaultLanguageHighlighterColors.SEMICOLON
        )

        // Colon :
        val QSS_COLON = TextAttributesKey.createTextAttributesKey(
            "QSS_COLON",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )

        // Comma ,
        val QSS_COMMA = TextAttributesKey.createTextAttributesKey(
            "QSS_COMMA",
            DefaultLanguageHighlighterColors.COMMA
        )

        // ═══════════════════════════════════════════════════════════════════════════
        // OPERATORS & SPECIAL
        // ═══════════════════════════════════════════════════════════════════════════

        // Operators (=, >, !, /, *)
        val QSS_OPERATOR = TextAttributesKey.createTextAttributesKey(
            "QSS_OPERATOR",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )

        // Comments - Gray
        val QSS_COMMENT = TextAttributesKey.createTextAttributesKey(
            "QSS_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )

        // Bad/invalid characters - Red underline
        val QSS_BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
            "QSS_BAD_CHARACTER",
            HighlighterColors.BAD_CHARACTER
        )
    }
}