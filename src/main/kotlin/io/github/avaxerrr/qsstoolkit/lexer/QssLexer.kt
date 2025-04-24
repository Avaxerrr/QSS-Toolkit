// QSS Toolkit version 1.1


package io.github.avaxerrr.qsstoolkit.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

class QssLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var bufferEnd: Int = 0
    private var bufferStart: Int = 0
    private var currentPosition: Int = 0
    private var tokenStart: Int = 0
    private var tokenEnd: Int = 0
    private var currentToken: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.bufferStart = startOffset
        this.bufferEnd = endOffset
        this.currentPosition = startOffset
        advance()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? {
        return currentToken
    }

    override fun getTokenStart(): Int = tokenStart

    override fun getTokenEnd(): Int = tokenEnd

    override fun advance() {
        if (currentPosition >= bufferEnd) {
            currentToken = null
            tokenStart = bufferEnd
            tokenEnd = bufferEnd
            return
        }

        tokenStart = currentPosition

        when {
            isWhitespace(buffer[currentPosition]) -> {
                scanWhitespace()
                currentToken = QssTokenTypes.WHITE_SPACE
            }
            // Handle double-colon pseudo-elements (::item)
            buffer[currentPosition] == ':' && currentPosition + 1 < bufferEnd &&
                    buffer[currentPosition + 1] == ':' -> {
                currentPosition += 2 // Skip the ::

                // Check if there's an identifier following (like in ::item)
                if (currentPosition < bufferEnd && isIdentifierStart(buffer[currentPosition])) {
                    scanIdentifier()
                    currentToken = QssTokenTypes.PSEUDO_ELEMENT
                } else {
                    currentToken = QssTokenTypes.COLON // Fallback if no identifier
                }
            }
            // Handle single-colon pseudo-states (:hover, :active)
            buffer[currentPosition] == ':' -> {
                currentPosition++ // Skip the :

                // Check if there's an identifier following (like in :hover)
                if (currentPosition < bufferEnd && isIdentifierStart(buffer[currentPosition])) {
                    scanIdentifier()
                    currentToken = QssTokenTypes.PSEUDO_STATE
                } else {
                    currentToken = QssTokenTypes.COLON
                }
            }
            buffer[currentPosition] == '/' && currentPosition + 1 < bufferEnd &&
                    (buffer[currentPosition + 1] == '/' || buffer[currentPosition + 1] == '*') -> {
                scanComment()
                currentToken = QssTokenTypes.COMMENT
            }
            buffer[currentPosition] == '{' -> {
                currentPosition++
                currentToken = QssTokenTypes.LBRACE
            }
            buffer[currentPosition] == '}' -> {
                currentPosition++
                currentToken = QssTokenTypes.RBRACE
            }
            buffer[currentPosition] == ';' -> {
                currentPosition++
                currentToken = QssTokenTypes.SEMICOLON
            }
            buffer[currentPosition] == ',' -> {
                currentPosition++
                currentToken = QssTokenTypes.COMMA
            }
            buffer[currentPosition] == '#' -> {
                scanHashOrColor()
            }
            buffer[currentPosition] == '.' -> {
                scanDotSelector()
                currentToken = QssTokenTypes.DOT
            }
            buffer[currentPosition] == '\'' || buffer[currentPosition] == '"' -> {
                scanString()
                currentToken = QssTokenTypes.STRING
            }
            isIdentifierStart(buffer[currentPosition]) -> {
                scanIdentifier()
                currentToken = QssTokenTypes.IDENTIFIER
            }
            else -> {
                currentPosition++
                currentToken = QssTokenTypes.BAD_CHARACTER
            }
        }

        tokenEnd = currentPosition
    }

    private fun scanWhitespace() {
        while (currentPosition < bufferEnd && isWhitespace(buffer[currentPosition])) {
            currentPosition++
        }
    }

    private fun scanComment() {
        if (buffer[currentPosition] == '/' && currentPosition + 1 < bufferEnd) {
            if (buffer[currentPosition + 1] == '/') {
                // Line comment
                currentPosition += 2
                while (currentPosition < bufferEnd && buffer[currentPosition] != '\n') {
                    currentPosition++
                }
                if (currentPosition < bufferEnd) {
                    currentPosition++ // Include the newline
                }
            } else if (buffer[currentPosition + 1] == '*') {
                // Block comment
                currentPosition += 2
                while (currentPosition + 1 < bufferEnd &&
                    !(buffer[currentPosition] == '*' && buffer[currentPosition + 1] == '/')) {
                    currentPosition++
                }
                if (currentPosition + 1 < bufferEnd) {
                    currentPosition += 2 // Include the */
                }
            }
        }
    }

    private fun scanHashOrColor() {
        currentPosition++ // Skip the #
        val start = currentPosition

        // Check if it's a color
        var isColor = true
        while (currentPosition < bufferEnd && isHexDigit(buffer[currentPosition])) {
            currentPosition++
        }

        val length = currentPosition - start
        if (length == 3 || length == 6 || length == 8) {
            currentToken = QssTokenTypes.HEX_COLOR
        } else {
            // It's an ID selector
            currentToken = QssTokenTypes.HASH
        }
    }

    private fun scanDotSelector() {
        currentPosition++ // Skip the .
        while (currentPosition < bufferEnd &&
            (isIdentifierPart(buffer[currentPosition]) || buffer[currentPosition] == '-')) {
            currentPosition++
        }
    }

    private fun scanString() {
        val quote = buffer[currentPosition]
        currentPosition++ // Skip the opening quote

        while (currentPosition < bufferEnd && buffer[currentPosition] != quote) {
            if (buffer[currentPosition] == '\\' && currentPosition + 1 < bufferEnd) {
                // Skip escaped characters
                currentPosition += 2
            } else {
                currentPosition++
            }
        }

        if (currentPosition < bufferEnd) {
            currentPosition++ // Skip the closing quote
        }
    }

    private fun scanIdentifier() {
        while (currentPosition < bufferEnd &&
            (isIdentifierPart(buffer[currentPosition]) || buffer[currentPosition] == '-')) {
            currentPosition++
        }
    }

    private fun isWhitespace(c: Char): Boolean = c.isWhitespace()

    private fun isIdentifierStart(c: Char): Boolean = c.isLetter() || c == '_'

    private fun isIdentifierPart(c: Char): Boolean = c.isLetterOrDigit() || c == '_' || c == '-'

    private fun isHexDigit(c: Char): Boolean = c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F'

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = bufferEnd
}
