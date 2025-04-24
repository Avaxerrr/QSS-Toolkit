// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import io.github.avaxerrr.qsstoolkit.QssFileType
import io.github.avaxerrr.qsstoolkit.QssLanguage

class QssFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, QssLanguage) {
    // Change this line to return the INSTANCE
    override fun getFileType() = QssFileType.INSTANCE

    override fun toString() = "QSS File"
}
