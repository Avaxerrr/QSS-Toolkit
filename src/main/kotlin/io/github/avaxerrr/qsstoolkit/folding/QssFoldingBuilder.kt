// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.github.avaxerrr.qsstoolkit.psi.QssRule
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes

class QssFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        // Find all rule blocks for folding
        val rules = PsiTreeUtil.findChildrenOfType(root, QssRule::class.java)
        for (rule in rules) {
            // Find the opening and closing braces
            val leftBraceOffset = rule.text.indexOf('{')
            val rightBraceOffset = rule.text.lastIndexOf('}')

            if (leftBraceOffset >= 0 && rightBraceOffset > leftBraceOffset) {
                val startOffset = rule.textRange.startOffset + leftBraceOffset + 1
                val endOffset = rule.textRange.startOffset + rightBraceOffset

                if (endOffset > startOffset) {
                    descriptors.add(FoldingDescriptor(
                        rule.node,
                        TextRange(startOffset, endOffset),
                        null
                    ))
                }
            }
        }

        // Find and fold multiline comments
        foldComments(root, descriptors)

        return descriptors.toTypedArray()
    }

    private fun foldComments(element: PsiElement, descriptors: MutableList<FoldingDescriptor>) {
        PsiTreeUtil.processElements(element) { psiElement ->
            if (psiElement.node.elementType == QssTokenTypes.COMMENT) {
                val commentText = psiElement.text
                if (commentText.startsWith("/*") && commentText.endsWith("*/") && commentText.contains("\n")) {
                    descriptors.add(FoldingDescriptor(
                        psiElement.node,
                        psiElement.textRange,
                        null
                    ))
                }
            }
            true
        }
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return if (node.psi is QssRule) " { ... }" else "/* ... */"
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
