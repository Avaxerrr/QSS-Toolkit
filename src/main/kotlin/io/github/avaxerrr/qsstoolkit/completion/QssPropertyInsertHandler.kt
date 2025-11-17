// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

class QssPropertyInsertHandler : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val document = context.document
        val tailOffset = context.tailOffset

        // Insert ": " after the property name
        document.insertString(tailOffset, ": ")

        // Move cursor after the colon and space
        context.editor.caretModel.moveToOffset(tailOffset + 2)
    }
}
