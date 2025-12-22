// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.psi

import com.intellij.psi.PsiElement

interface QssElement : PsiElement

interface QssCompositeElement : QssElement

interface QssRule : QssCompositeElement

interface QssSelectorList : QssCompositeElement

interface QssAttributeSelector : QssCompositeElement

interface QssDeclaration : QssCompositeElement {
    val propertyName: String?
    val values: QssPropertyValues?
}

interface QssPropertyValues : QssCompositeElement {
    val values: List<QssPropertyValue>
}

interface QssPropertyValue : QssCompositeElement

interface QssColorValue : QssPropertyValue {
    val colorText: String?
    val hexValue: String?
}

interface QssNumberValue : QssPropertyValue {
    val numberText: String?
}

interface QssStringValue : QssPropertyValue {
    val stringText: String?
}

interface QssIdentifierValue : QssPropertyValue {
    val identifierText: String?
}
