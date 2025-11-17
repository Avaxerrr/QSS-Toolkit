// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.editing

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes

class QssBraceMatcher : PairedBraceMatcher {
    private val pairs = arrayOf(
        BracePair(QssTokenTypes.LBRACE, QssTokenTypes.RBRACE, true),       // { }
        BracePair(QssTokenTypes.LPAREN, QssTokenTypes.RPAREN, false),      // ( )
        BracePair(QssTokenTypes.LBRACKET, QssTokenTypes.RBRACKET, false)   // [ ]
    )

    override fun getPairs(): Array<BracePair> = pairs

    override fun isPairedBracesAllowedBeforeType(
        lbraceType: IElementType,
        contextType: IElementType?
    ): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }
}
