package io.github.avaxerrr.qsstoolkit.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import io.github.avaxerrr.qsstoolkit.QssLanguage
import io.github.avaxerrr.qsstoolkit.psi.impl.*

interface QssTypes {
    companion object {
        // File
        val FILE = QssElementType("FILE")

        // Rules
        val RULE = QssElementType("RULE")
        val SELECTOR_LIST = QssElementType("SELECTOR_LIST")

        // Declarations
        val DECLARATION = QssElementType("DECLARATION")
        val PROPERTY_VALUES = QssElementType("PROPERTY_VALUES")
        val PROPERTY_VALUE = QssElementType("PROPERTY_VALUE")
        val COLOR_VALUE = QssElementType("COLOR_VALUE")
        val NUMBER_VALUE = QssElementType("NUMBER_VALUE")
        val STRING_VALUE = QssElementType("STRING_VALUE")
        val IDENTIFIER_VALUE = QssElementType("IDENTIFIER_VALUE")
    }

    object Factory {
        fun createElement(node: ASTNode?): PsiElement {
            val type = node?.elementType

            return when(type) {
                RULE -> QssRuleImpl(node!!)
                SELECTOR_LIST -> QssSelectorListImpl(node!!)
                DECLARATION -> QssDeclarationImpl(node!!)
                PROPERTY_VALUES -> QssPropertyValuesImpl(node!!)
                PROPERTY_VALUE -> QssPropertyValueImpl(node!!)
                COLOR_VALUE -> QssColorValueImpl(node!!)
                NUMBER_VALUE -> QssNumberValueImpl(node!!)
                STRING_VALUE -> QssStringValueImpl(node!!)
                IDENTIFIER_VALUE -> QssIdentifierValueImpl(node!!)
                else -> QssCompositeElementImpl(node!!)
            }
        }
    }
}

class QssElementType(debugName: String) : IElementType(debugName, QssLanguage)
