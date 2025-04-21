package io.github.avaxerrr.qsstoolkit

import com.intellij.lang.Language

object QssLanguage : Language("QSS", "text/x-qss") {
    override fun getDisplayName(): String = "Qt Style Sheet"

    override fun isCaseSensitive(): Boolean = true
}
