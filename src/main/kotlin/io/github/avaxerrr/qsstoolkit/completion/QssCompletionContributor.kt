// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import io.github.avaxerrr.qsstoolkit.QssLanguage
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes
import io.github.avaxerrr.qsstoolkit.psi.QssDeclaration
import io.github.avaxerrr.qsstoolkit.psi.QssFile
import io.github.avaxerrr.qsstoolkit.psi.QssTypes
import com.intellij.psi.PsiElement


class QssCompletionContributor : CompletionContributor() {

    init {
        // Property name completion
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement(QssTokenTypes.IDENTIFIER)
                .withLanguage(QssLanguage),
            QssPropertyCompletionProvider()
        )

        // Property value completion
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(QssLanguage)
                .afterLeaf(PlatformPatterns.psiElement(QssTokenTypes.COLON)),
            QssValueCompletionProvider()
        )

        // Selector completion
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(QssLanguage)
                .andNot(PlatformPatterns.psiElement().inside(
                    PlatformPatterns.psiElement(QssTypes.DECLARATION))),
            QssSelectorCompletionProvider()
        )
    }

    private class QssPropertyCompletionProvider : CompletionProvider<CompletionParameters>() {
        companion object {
            private val QSS_PROPERTIES = listOf(
                "background", "background-color", "background-image", "border", "border-top",
                "border-right", "border-bottom", "border-left", "border-color", "border-width",
                "border-style", "border-radius", "color", "font", "font-family", "font-size",
                "font-style", "font-weight", "height", "margin", "margin-top", "margin-right",
                "margin-bottom", "margin-left", "max-height", "max-width", "min-height",
                "min-width", "padding", "padding-top", "padding-right", "padding-bottom",
                "padding-left", "selection-background-color", "selection-color", "width"
            )
        }

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            // Add QSS property completions
            for (property in QSS_PROPERTIES) {
                result.addElement(
                    LookupElementBuilder.create(property)
                        .withTypeText("QSS Property")
                        .withBoldness(true)
                )
            }
        }
    }

    private class QssValueCompletionProvider : CompletionProvider<CompletionParameters>() {
        companion object {
            private val COLOR_KEYWORDS = listOf(
                "transparent", "black", "white", "red", "green", "blue", "yellow",
                "cyan", "magenta", "gray", "lightgray", "darkgray"
            )

            private val COMMON_VALUES = mapOf(
                "border-style" to listOf("none", "solid", "dashed", "dotted", "double", "groove", "ridge", "inset", "outset"),
                "font-style" to listOf("normal", "italic", "oblique"),
                "font-weight" to listOf("normal", "bold", "100", "200", "300", "400", "500", "600", "700", "800", "900")
            )
        }

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            // Get current property
            val element = parameters.position
            val declaration = findParentDeclaration(element)
            val propertyName = declaration?.propertyName

            // Add common values for the property if available
            if (propertyName != null && COMMON_VALUES.containsKey(propertyName)) {
                for (value in COMMON_VALUES[propertyName]!!) {
                    result.addElement(LookupElementBuilder.create(value))
                }
            }

            // Add color values for color properties
            if (propertyName?.contains("color") == true ||
                propertyName?.contains("background") == true) {

                // Add color keywords
                for (color in COLOR_KEYWORDS) {
                    result.addElement(LookupElementBuilder.create(color))
                }

                // Add common color hex values
                result.addElement(LookupElementBuilder.create("#000000").withTypeText("Black"))
                result.addElement(LookupElementBuilder.create("#FFFFFF").withTypeText("White"))
                result.addElement(LookupElementBuilder.create("#FF0000").withTypeText("Red"))
                result.addElement(LookupElementBuilder.create("#00FF00").withTypeText("Green"))
                result.addElement(LookupElementBuilder.create("#0000FF").withTypeText("Blue"))
            }
        }

        private fun findParentDeclaration(element: PsiElement): QssDeclaration? {
            var current: PsiElement? = element
            while (current != null && current !is QssFile) {
                if (current is QssDeclaration) {
                    return current
                }
                current = current.parent
            }
            return null
        }
    }

    private class QssSelectorCompletionProvider : CompletionProvider<CompletionParameters>() {
        companion object {
            private val QT_WIDGETS = listOf(
                "QAbstractButton", "QCheckBox", "QComboBox", "QCommandLinkButton", "QDateEdit",
                "QDateTimeEdit", "QDial", "QDoubleSpinBox", "QFontComboBox", "QHeaderView",
                "QLabel", "QLCDNumber", "QLineEdit", "QMainWindow", "QMenu", "QMenuBar",
                "QPushButton", "QProgressBar", "QRadioButton", "QScrollArea", "QScrollBar",
                "QSlider", "QSpinBox", "QTabBar", "QTabWidget", "QTableView", "QTextEdit",
                "QTimeEdit", "QToolBar", "QToolButton", "QTreeView"
            )

            private val PSEUDO_STATES = listOf(
                ":hover", ":pressed", ":checked", ":selected", ":disabled", ":focus", ":active"
            )
        }

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            // Add Qt widget selectors
            for (widget in QT_WIDGETS) {
                result.addElement(
                    LookupElementBuilder.create(widget)
                        .withTypeText("Qt Widget")
                )
            }

            // Add pseudo-states
            for (state in PSEUDO_STATES) {
                result.addElement(
                    LookupElementBuilder.create(state)
                        .withTypeText("Pseudo-state")
                )
            }
        }
    }
}
