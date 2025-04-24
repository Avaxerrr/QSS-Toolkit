// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.NlsContexts
import javax.swing.Icon

class QssFileType : LanguageFileType(QssLanguage) {
    override fun getName(): String = NAME

    override fun getDescription(): String = DESCRIPTION

    override fun getDefaultExtension(): String = DEFAULT_EXTENSION

    override fun getIcon(): Icon = QssIcons.FILE

    companion object {
        // Create a singleton instance for backward compatibility
        val INSTANCE = QssFileType()

        // Constants
        const val NAME: String = "QSS"
        const val DESCRIPTION: String = "Qt Style Sheet"
        const val DEFAULT_EXTENSION: String = "qss"
    }
}
