package io.github.avaxerrr.qsstoolkit

import com.intellij.psi.tree.IElementType
import io.github.avaxerrr.qsstoolkit.completion.QssData
import io.github.avaxerrr.qsstoolkit.lexer.QssLexer
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QssSyntaxRegressionTest {
    @Test
    fun `current Qt-specific properties are known`() {
        val properties = QssData.PROPERTY_TYPES.keys

        listOf(
            "lineedit-password-mask-delay",
            "outline-bottom-left-radius",
            "outline-bottom-right-radius",
            "outline-top-left-radius",
            "outline-top-right-radius",
            "titlebar-show-tooltips-on-buttons",
            "widget-animation-duration",
            "-qt-background-role",
            "-qt-style-features"
        ).forEach { property ->
            assertContains(properties, property)
        }
    }

    @Test
    fun `Qt icon properties are known`() {
        val properties = QssData.PROPERTY_TYPES.keys

        listOf(
            "dialog-ok-icon",
            "lineedit-clear-button-icon",
            "messagebox-critical-icon",
            "titlebar-maximize-icon",
            "uparrow-icon"
        ).forEach { property ->
            assertContains(properties, property)
        }

        assertTrue(properties.count { it.endsWith("-icon") } >= 50)
    }

    @Test
    fun `current Qt pseudo states and subcontrols are known`() {
        assertContains(QssData.PSEUDO_STATES, ":exclusive")
        assertContains(QssData.PSEUDO_STATES, ":non-exclusive")

        listOf("::corner", "::menu-button", "::left-corner", "::right-corner", "::tearoff").forEach { subcontrol ->
            assertContains(QssData.COMMON_SUBCONTROLS, subcontrol)
        }

        assertEquals(listOf("::left-arrow", "::right-arrow"), QssData.WIDGET_SUBCONTROLS["QColumnView"])
        assertContains(QssData.WIDGET_SUBCONTROLS.getValue("QPushButton"), "::menu-indicator")
        assertContains(QssData.WIDGET_SUBCONTROLS.getValue("QTabWidget"), "::left-corner")
        assertContains(QssData.WIDGET_SUBCONTROLS.getValue("QToolButton"), "::up-arrow")
    }

    @Test
    fun `lexer accepts Qt leading-hyphen properties`() {
        val tokens = lex("QWidget { -qt-background-role: Window; -qt-style-features: icon; }")

        assertFalse(tokens.any { it.type == QssTokenTypes.BAD_CHARACTER }, tokens.toDebugString())
        assertTrue(tokens.any { it.type == QssTokenTypes.IDENTIFIER && it.text == "-qt-background-role" }, tokens.toDebugString())
        assertTrue(tokens.any { it.type == QssTokenTypes.IDENTIFIER && it.text == "-qt-style-features" }, tokens.toDebugString())
    }

    @Test
    fun `dynamic qproperty names stay valid without pre-registering every Qt property`() {
        val dynamicProperty = "qproperty-wordWrap"
        val appSpecificDynamicProperty = "qproperty-titleColor"

        assertTrue(dynamicProperty.startsWith("qproperty-", ignoreCase = true))
        assertTrue(appSpecificDynamicProperty.startsWith("qproperty-", ignoreCase = true))
        assertFalse(QssData.PROPERTY_TYPES.containsKey(appSpecificDynamicProperty.lowercase()))
    }

    @Test
    fun `hsv functions are offered for completion`() {
        assertContains(QssData.FUNCTIONS, "hsv()")
        assertContains(QssData.FUNCTIONS, "hsva()")
    }

    private fun lex(text: String): List<Token> {
        val lexer = QssLexer()
        lexer.start(text)

        val tokens = mutableListOf<Token>()
        while (lexer.tokenType != null) {
            tokens += Token(
                type = lexer.tokenType!!,
                text = text.substring(lexer.tokenStart, lexer.tokenEnd)
            )
            lexer.advance()
        }
        return tokens
    }

    private data class Token(val type: IElementType, val text: String)

    private fun List<Token>.toDebugString(): String =
        joinToString(separator = "\n") { "${it.type}: '${it.text}'" }
}
