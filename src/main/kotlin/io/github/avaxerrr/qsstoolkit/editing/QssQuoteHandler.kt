// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.editing

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes

class QssQuoteHandler : SimpleTokenSetQuoteHandler(QssTokenTypes.STRING)
