// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import io.github.avaxerrr.qsstoolkit.QssLanguage
import io.github.avaxerrr.qsstoolkit.lexer.QssLexer
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes
import io.github.avaxerrr.qsstoolkit.psi.QssFile
import io.github.avaxerrr.qsstoolkit.psi.QssTypes

class QssParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = QssLexer()

    override fun createParser(project: Project?): PsiParser = QssParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getWhitespaceTokens(): TokenSet = WHITE_SPACES

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = STRINGS

    override fun createElement(node: ASTNode?): PsiElement = QssTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = QssFile(viewProvider)

    companion object {
        val FILE = IFileElementType(QssLanguage)
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(QssTokenTypes.COMMENT)
        val STRINGS = TokenSet.create(QssTokenTypes.STRING)
    }
}
