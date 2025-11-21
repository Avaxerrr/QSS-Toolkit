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

    companion object {
        // QSS keyword values (property values that are predefined)
        // These are primarily property names, not values
        private val KEYWORDS = setOf(
            // Border styles
            "none", "solid", "dashed", "dotted", "double", "groove", "ridge", "inset", "outset",
            // Colors (named colors)
            "transparent", "white", "black", "red", "green", "blue", "yellow", "cyan", "magenta",
            "gray", "grey", "darkred", "darkgreen", "darkblue", "darkcyan", "darkmagenta", "darkyellow",
            "lightgray", "lightgrey",
            // Font weights
            "normal", "bold", "bolder", "lighter",
            // Font styles
            "italic", "oblique",
            // Text decoration
            "underline", "overline", "line-through",
            // Text alignment (only when used as VALUES, not property names)
            "left", "right", "center", "top", "bottom", "middle",
            // Display
            "block", "inline", "inline-block",
            // Boolean/special
            "true", "false", "on", "off", "yes", "no",
            // Repeat
            "repeat", "repeat-x", "repeat-y", "no-repeat",
            // Qt-specific positioning (when used as values)
            "stretch", "fixed"
        )
    }

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
            // Handle Template Tags {{...}}
            buffer[currentPosition] == '{' && currentPosition + 1 < bufferEnd &&
                    buffer[currentPosition + 1] == '{' -> {
                scanTemplateTag()
                currentToken = QssTokenTypes.TEMPLATE_TAG
            }
            // Handle double-colon pseudo-elements (::item)
            buffer[currentPosition] == ':' && currentPosition + 1 < bufferEnd &&
                    buffer[currentPosition + 1] == ':' -> {
                currentPosition += 2 // Skip the ::
                if (currentPosition < bufferEnd && isIdentifierStart(buffer[currentPosition])) {
                    scanIdentifier()
                    currentToken = QssTokenTypes.PSEUDO_ELEMENT
                } else {
                    currentToken = QssTokenTypes.COLON
                }
            }
            // Handle single-colon pseudo-states (:hover, :active)
            buffer[currentPosition] == ':' -> {
                currentPosition++ // Skip the :
                if (currentPosition < bufferEnd && isIdentifierStart(buffer[currentPosition])) {
                    scanIdentifier()
                    currentToken = QssTokenTypes.PSEUDO_STATE
                } else {
                    currentToken = QssTokenTypes.COLON
                }
            }
            // Handle comments
            buffer[currentPosition] == '/' && currentPosition + 1 < bufferEnd &&
                    (buffer[currentPosition + 1] == '/' || buffer[currentPosition + 1] == '*') -> {
                scanComment()
                currentToken = QssTokenTypes.COMMENT
            }
            // Handle standalone slash
            buffer[currentPosition] == '/' -> {
                currentPosition++
                currentToken = QssTokenTypes.SLASH
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
            buffer[currentPosition] == '(' -> {
                currentPosition++
                currentToken = QssTokenTypes.LPAREN
            }
            buffer[currentPosition] == ')' -> {
                currentPosition++
                currentToken = QssTokenTypes.RPAREN
            }
            buffer[currentPosition] == '[' -> {
                currentPosition++
                currentToken = QssTokenTypes.LBRACKET
            }
            buffer[currentPosition] == ']' -> {
                currentPosition++
                currentToken = QssTokenTypes.RBRACKET
            }
            buffer[currentPosition] == '=' -> {
                currentPosition++
                currentToken = QssTokenTypes.EQUALS
            }
            buffer[currentPosition] == '>' -> {
                currentPosition++
                currentToken = QssTokenTypes.GT
            }
            buffer[currentPosition] == '!' -> {
                currentPosition++
                currentToken = QssTokenTypes.EXCLAMATION
            }
            buffer[currentPosition] == '#' -> {
                scanHashOrColor()
            }
            buffer[currentPosition] == '*' -> {
                currentPosition++
                currentToken = QssTokenTypes.ASTERISK
            }
            buffer[currentPosition] == '.' -> {
                scanDotSelector()
                currentToken = QssTokenTypes.DOT
            }
            buffer[currentPosition] == '\'' || buffer[currentPosition] == '"' -> {
                scanString()
                currentToken = QssTokenTypes.STRING
            }
            isDigit(buffer[currentPosition]) ||
                    (buffer[currentPosition] == '-' && currentPosition + 1 < bufferEnd &&
                            isDigit(buffer[currentPosition + 1])) -> {
                scanNumber()
                currentToken = QssTokenTypes.NUMBER
            }
            isIdentifierStart(buffer[currentPosition]) -> {
                val start = currentPosition
                scanIdentifier()
                val text = buffer.substring(start, currentPosition) // Keep case for check
                val lowerText = text.lowercase()

                // Check for Functions
                if (currentPosition < bufferEnd && buffer[currentPosition] == '(') {
                    if (lowerText == "url") {
                        scanUrl()
                        currentToken = QssTokenTypes.URL
                    } else if (lowerText == "rgb" || lowerText == "rgba") {
                        scanParenthesisBlock()
                        currentToken = if (lowerText == "rgb") QssTokenTypes.RGB_FUNCTION else QssTokenTypes.RGBA_FUNCTION
                    } else if (lowerText.startsWith("q") && lowerText.endsWith("gradient")) {
                        scanParenthesisBlock()
                        currentToken = QssTokenTypes.GRADIENT
                    } else {
                        currentToken = QssTokenTypes.IDENTIFIER
                    }
                }
                else if (KEYWORDS.contains(lowerText)) {
                    currentToken = QssTokenTypes.KEYWORD
                } else if (text.isNotEmpty() && text[0].isUpperCase()) {
                    // Separate Widgets (Capitalized) from Properties (lowercase)
                    currentToken = QssTokenTypes.WIDGET_CLASS
                } else {
                    currentToken = QssTokenTypes.IDENTIFIER
                }
            }
            else -> {
                currentPosition++
                currentToken = QssTokenTypes.BAD_CHARACTER
            }
        }

        tokenEnd = currentPosition
    }

    // Scans {{...}}
    private fun scanTemplateTag() {
        currentPosition += 2 // Skip {{
        while (currentPosition + 1 < bufferEnd) {
            if (buffer[currentPosition] == '}' && buffer[currentPosition + 1] == '}') {
                currentPosition += 2
                return
            }
            currentPosition++
        }
        // If EOF reached without closing, just consume until end
        currentPosition = bufferEnd
    }

    private fun scanParenthesisBlock() {
        if (currentPosition < bufferEnd && buffer[currentPosition] == '(') {
            currentPosition++
            var depth = 1
            while (currentPosition < bufferEnd && depth > 0) {
                when (buffer[currentPosition]) {
                    '(' -> depth++
                    ')' -> {
                        depth--
                        if (depth == 0) {
                            currentPosition++
                            return
                        }
                    }
                }
                currentPosition++
            }
        }
    }

    private fun scanUrl() {
        if (currentPosition < bufferEnd && buffer[currentPosition] == '(') {
            currentPosition++
            while (currentPosition < bufferEnd && buffer[currentPosition] != ')') {
                currentPosition++
            }
            if (currentPosition < bufferEnd && buffer[currentPosition] == ')') {
                currentPosition++
            }
        }
    }

    private fun scanWhitespace() {
        while (currentPosition < bufferEnd && isWhitespace(buffer[currentPosition])) {
            currentPosition++
        }
    }

    private fun scanComment() {
        if (buffer[currentPosition] == '/' && currentPosition + 1 < bufferEnd) {
            if (buffer[currentPosition + 1] == '/') {
                currentPosition += 2
                while (currentPosition < bufferEnd && buffer[currentPosition] != '\n') {
                    currentPosition++
                }
                if (currentPosition < bufferEnd) currentPosition++
            } else if (buffer[currentPosition + 1] == '*') {
                currentPosition += 2
                while (currentPosition + 1 < bufferEnd &&
                    !(buffer[currentPosition] == '*' && buffer[currentPosition + 1] == '/')) {
                    currentPosition++
                }
                if (currentPosition + 1 < bufferEnd) currentPosition += 2
            }
        }
    }

    private fun scanHashOrColor() {
        currentPosition++
        val start = currentPosition
        while (currentPosition < bufferEnd && isHexDigit(buffer[currentPosition])) {
            currentPosition++
        }
        val length = currentPosition - start
        if (length == 3 || length == 6 || length == 8) {
            currentToken = QssTokenTypes.HEX_COLOR
        } else {
            while (currentPosition < bufferEnd &&
                (isIdentifierPart(buffer[currentPosition]) || buffer[currentPosition] == '-')) {
                currentPosition++
            }
            currentToken = QssTokenTypes.HASH
        }
    }

    private fun scanDotSelector() {
        currentPosition++
        while (currentPosition < bufferEnd &&
            (isIdentifierPart(buffer[currentPosition]) || buffer[currentPosition] == '-')) {
            currentPosition++
        }
    }

    private fun scanString() {
        val quote = buffer[currentPosition]
        currentPosition++
        while (currentPosition < bufferEnd && buffer[currentPosition] != quote) {
            if (buffer[currentPosition] == '\\' && currentPosition + 1 < bufferEnd) {
                currentPosition += 2
            } else {
                currentPosition++
            }
        }
        if (currentPosition < bufferEnd) currentPosition++
    }

    private fun scanNumber() {
        if (buffer[currentPosition] == '-') currentPosition++
        while (currentPosition < bufferEnd && isDigit(buffer[currentPosition])) currentPosition++
        if (currentPosition < bufferEnd && buffer[currentPosition] == '.' &&
            currentPosition + 1 < bufferEnd && isDigit(buffer[currentPosition + 1])) {
            currentPosition++
            while (currentPosition < bufferEnd && isDigit(buffer[currentPosition])) currentPosition++
        }
        if (currentPosition < bufferEnd && buffer[currentPosition] == '%') {
            currentPosition++
            return
        }
        if (currentPosition < bufferEnd && isIdentifierStart(buffer[currentPosition])) {
            while (currentPosition < bufferEnd && buffer[currentPosition].isLetterOrDigit()) {
                currentPosition++
            }
        }
    }

    private fun scanIdentifier() {
        while (currentPosition < bufferEnd &&
            (isIdentifierPart(buffer[currentPosition]) || buffer[currentPosition] == '-')) {
            currentPosition++
        }
    }

    private fun isWhitespace(c: Char): Boolean = c.isWhitespace()
    private fun isDigit(c: Char): Boolean = c in '0'..'9'
    private fun isIdentifierStart(c: Char): Boolean = c.isLetter() || c == '_'
    private fun isIdentifierPart(c: Char): Boolean = c.isLetterOrDigit() || c == '_' || c == '-'
    private fun isHexDigit(c: Char): Boolean = c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F'
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = bufferEnd
}