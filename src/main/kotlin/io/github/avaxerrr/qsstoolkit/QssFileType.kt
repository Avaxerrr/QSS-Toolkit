package io.github.avaxerrr.qsstoolkit

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object QssFileType : LanguageFileType(QssLanguage) {
    override fun getName(): String = "QSS"

    override fun getDescription(): String = "Qt Style Sheet"

    override fun getDefaultExtension(): String = "qss"

    override fun getIcon(): Icon = QssIcons.FILE
}
