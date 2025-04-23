// QSS Toolkit version 1.0


package io.github.avaxerrr.qsstoolkit.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.github.avaxerrr.qsstoolkit.psi.QssDeclaration
import io.github.avaxerrr.qsstoolkit.psi.QssFile
import io.github.avaxerrr.qsstoolkit.psi.QssRule
import javax.swing.Icon

class QssStructureViewElement(private val element: PsiElement) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) {
        if (element is NavigationItem) {
            element.navigate(requestFocus)
        }
    }

    override fun canNavigate(): Boolean {
        return element is NavigationItem && (element as NavigationItem).canNavigate()
    }

    override fun canNavigateToSource(): Boolean {
        return element is NavigationItem && (element as NavigationItem).canNavigateToSource()
    }

    override fun getAlphaSortKey(): String {
        val name = when (element) {
            is QssFile -> element.name
            is QssRule -> getSelectorText(element)
            is QssDeclaration -> element.propertyName ?: ""
            else -> element.text
        }
        return name
    }

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String? {
                return when (element) {
                    is QssFile -> element.name
                    is QssRule -> getSelectorText(element)
                    is QssDeclaration -> "${element.propertyName}: ${getValuePreview(element)}"
                    else -> element.text
                }
            }

            override fun getIcon(unused: Boolean): Icon? = null

            override fun getLocationString(): String? = null
        }
    }

    override fun getChildren(): Array<TreeElement> {
        if (element is QssFile) {
            // For a file, return all rules
            val rules = PsiTreeUtil.findChildrenOfType(element, QssRule::class.java)
            return rules.map { QssStructureViewElement(it) }.toTypedArray()
        } else if (element is QssRule) {
            // For a rule, return all declarations
            val declarations = PsiTreeUtil.findChildrenOfType(element, QssDeclaration::class.java)
            return declarations.map { QssStructureViewElement(it) }.toTypedArray()
        }

        return emptyArray()
    }

    private fun getSelectorText(rule: QssRule): String {
        // Find the selector text before the opening brace
        val ruleText = rule.text
        val braceIndex = ruleText.indexOf('{')
        return if (braceIndex > 0) {
            ruleText.substring(0, braceIndex).trim()
        } else {
            ruleText
        }
    }

    private fun getValuePreview(declaration: QssDeclaration): String {
        val values = declaration.values
        return values?.values?.firstOrNull()?.text ?: "..."
    }
}
