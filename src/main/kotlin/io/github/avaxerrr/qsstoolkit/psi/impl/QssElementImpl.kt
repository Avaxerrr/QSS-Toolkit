// QSS Toolkit version 1.0

package io.github.avaxerrr.qsstoolkit.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes
import io.github.avaxerrr.qsstoolkit.psi.*
import com.intellij.psi.PsiElement

open class QssCompositeElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), QssCompositeElement

class QssRuleImpl(node: ASTNode) : QssCompositeElementImpl(node), QssRule

class QssSelectorListImpl(node: ASTNode) : QssCompositeElementImpl(node), QssSelectorList

class QssDeclarationImpl(node: ASTNode) : QssCompositeElementImpl(node), QssDeclaration {
    override val propertyName: String?
        get() = findChildByType<PsiElement>(QssTokenTypes.IDENTIFIER)?.text

    override val values: QssPropertyValues?
        get() = findChildByClass(QssPropertyValuesImpl::class.java)
}

class QssPropertyValuesImpl(node: ASTNode) : QssCompositeElementImpl(node), QssPropertyValues {
    override val values: List<QssPropertyValue>
        get() = findChildrenByClass(QssPropertyValueImpl::class.java).toList()
}

class QssPropertyValueImpl(node: ASTNode) : QssCompositeElementImpl(node), QssPropertyValue

class QssColorValueImpl(node: ASTNode) : QssCompositeElementImpl(node), QssColorValue {
    override val colorText: String?
        get() = node.text

    override val hexValue: String?
        get() = colorText
}

class QssNumberValueImpl(node: ASTNode) : QssCompositeElementImpl(node), QssNumberValue {
    override val numberText: String?
        get() = node.text
}

class QssStringValueImpl(node: ASTNode) : QssCompositeElementImpl(node), QssStringValue {
    override val stringText: String?
        get() = node.text
}

class QssIdentifierValueImpl(node: ASTNode) : QssCompositeElementImpl(node), QssIdentifierValue {
    override val identifierText: String?
        get() = node.text
}
