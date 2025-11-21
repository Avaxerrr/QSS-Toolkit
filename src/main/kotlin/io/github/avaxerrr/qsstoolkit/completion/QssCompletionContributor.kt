// QSS Toolkit version 2.0

package io.github.avaxerrr.qsstoolkit.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import io.github.avaxerrr.qsstoolkit.QssLanguage
import io.github.avaxerrr.qsstoolkit.lexer.QssTokenTypes
import com.intellij.psi.PsiElement

class QssCompletionContributor : CompletionContributor() {

    init {
        // Property name and widget selector completion
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement(QssTokenTypes.IDENTIFIER)
                .withLanguage(QssLanguage),
            QssPropertyCompletionProvider()
        )

        // Sub-control completion (after ::)
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement(QssTokenTypes.IDENTIFIER)
                .withLanguage(QssLanguage)
                .afterLeaf(PlatformPatterns.psiElement(QssTokenTypes.PSEUDO_ELEMENT)),
            QssSubControlCompletionProvider()
        )

        // Alternative: trigger on any position to catch ::
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(QssLanguage),
            QssSubControlTextCompletionProvider()
        )

        // Property value completion (after colon inside braces)
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(QssLanguage)
                .afterLeaf(PlatformPatterns.psiElement(QssTokenTypes.COLON)),
            QssValueCompletionProvider()
        )

        // NEW: Chained pseudo-state completion (after sub-control)
        // This handles patterns like QScrollBar::handle:vertical
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(QssLanguage),
            QssChainedPseudoStateProvider()
        )
    }

    // This method tells IntelliJ to auto-trigger completion after :: and :
    @Deprecated("Deprecated in IntelliJ Platform 2024.2+")
    override fun invokeAutoPopup(position: PsiElement, typeChar: Char): Boolean {
        // Auto-trigger after second colon in ::
        if (typeChar == ':') {
            val text = position.containingFile.text
            val offset = position.textRange.startOffset

            // Check if previous character is also a colon (for ::)
            if (offset > 0 && text.getOrNull(offset - 1) == ':') {
                return true  // Auto-trigger for sub-controls
            }

            // Also auto-trigger for property values and chained pseudo-states
            return true
        }

        return false
    }

    // Standard widget selectors and properties completion provider
    private class QssPropertyCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            // Add widget selectors
            QssData.WIDGET_SELECTORS.forEach { widget ->
                resultSet.addElement(
                    LookupElementBuilder.create(widget)
                        .withTypeText("Widget Selector")
                        .withIcon(null)
                )
            }

            // Add properties
            QssData.PROPERTIES.forEach { property ->
                resultSet.addElement(
                    LookupElementBuilder.create(property)
                        .withTypeText("Property")
                        .withInsertHandler(QssPropertyInsertHandler())
                )
            }
        }
    }

    // Sub-control completion provider (after ::)
    private class QssSubControlCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val fileText = parameters.originalFile.text
            val currentWidget = findCurrentWidget(fileText, parameters.offset)

            if (currentWidget != null && QssData.WIDGET_SUBCONTROLS.containsKey(currentWidget)) {
                // Add sub-controls specific to this widget
                QssData.WIDGET_SUBCONTROLS[currentWidget]?.forEach { subcontrol ->
                    resultSet.addElement(
                        LookupElementBuilder.create(subcontrol.removePrefix("::"))
                            .withPresentableText(subcontrol)
                            .withTypeText("Sub-control for $currentWidget")
                            .withIcon(null)
                    )
                }
            } else {
                // Add all common sub-controls if we can't determine the widget
                QssData.COMMON_SUBCONTROLS.forEach { subcontrol ->
                    resultSet.addElement(
                        LookupElementBuilder.create(subcontrol.removePrefix("::"))
                            .withPresentableText(subcontrol)
                            .withTypeText("Sub-control")
                            .withIcon(null)
                    )
                }
            }
        }
    }

    // Alternative sub-control completion that checks text context
    private class QssSubControlTextCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val fileText = parameters.originalFile.text
            val offset = parameters.offset

            // Check if we're right after ::
            if (offset >= 2) {
                val textBefore = fileText.substring(maxOf(0, offset - 10), offset)
                if (textBefore.contains("::")) {
                    val currentWidget = findCurrentWidget(fileText, offset)

                    if (currentWidget != null && QssData.WIDGET_SUBCONTROLS.containsKey(currentWidget)) {
                        // Add sub-controls specific to this widget
                        QssData.WIDGET_SUBCONTROLS[currentWidget]?.forEach { subcontrol ->
                            resultSet.addElement(
                                LookupElementBuilder.create(subcontrol.removePrefix("::"))
                                    .withPresentableText(subcontrol)
                                    .withTypeText("Sub-control for $currentWidget")
                                    .withIcon(null)
                            )
                        }
                    } else {
                        // Add all common sub-controls
                        QssData.COMMON_SUBCONTROLS.forEach { subcontrol ->
                            resultSet.addElement(
                                LookupElementBuilder.create(subcontrol.removePrefix("::"))
                                    .withPresentableText(subcontrol)
                                    .withTypeText("Sub-control")
                                    .withIcon(null)
                            )
                        }
                    }
                }
            }
        }
    }

    // NEW: Chained pseudo-state completion provider
    // Handles: QScrollBar::handle:vertical, QPushButton:hover, etc.
    private class QssChainedPseudoStateProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val fileText = parameters.originalFile.text
            val offset = parameters.offset

            // Look back to see if we're after a potential pseudo-state position
            if (offset < 2) return

            val textBefore = fileText.substring(maxOf(0, offset - 50), offset)

            // Check if we're in a selector context (not inside braces for properties)
            val lastOpenBrace = textBefore.lastIndexOf('{')
            val lastCloseBrace = textBefore.lastIndexOf('}')

            // If we're inside braces, this is property value context, not selector context
            if (lastOpenBrace > lastCloseBrace && lastOpenBrace != -1) {
                return  // Skip - we're inside a declaration block
            }

            // Check if we're after a single colon (not ::)
            if (textBefore.endsWith(":") && !textBefore.endsWith("::")) {
                // Determine context - are we after a sub-control or widget?
                val hasSubControl = textBefore.contains("::")
                val currentWidget = findCurrentWidget(fileText, offset)
                val currentSubControl = if (hasSubControl) findCurrentSubControl(textBefore) else null

                // Get context-aware pseudo-states
                val relevantPseudoStates = getContextAwarePseudoStates(currentWidget, currentSubControl)

                relevantPseudoStates.forEach { pseudoState ->
                    resultSet.addElement(
                        LookupElementBuilder.create(pseudoState.removePrefix(":"))
                            .withPresentableText(pseudoState)
                            .withTypeText(if (hasSubControl) "Pseudo-state for sub-control" else "Pseudo-state")
                            .withIcon(null)
                    )
                }
            }
        }

        // Helper to find current sub-control
        private fun findCurrentSubControl(textBefore: String): String? {
            val subControlPattern = Regex("""::([a-z-]+)""")
            val matches = subControlPattern.findAll(textBefore)
            return matches.lastOrNull()?.groupValues?.get(1)
        }

        // Context-aware pseudo-state suggestions
        private fun getContextAwarePseudoStates(widget: String?, subControl: String?): List<String> {
            val allStates = QssData.PSEUDO_STATES.toMutableList()
            val priorityStates = mutableListOf<String>()

            // Prioritize based on widget type
            when {
                widget in listOf("QScrollBar", "QSlider") || subControl in listOf("handle", "groove") -> {
                    priorityStates.addAll(listOf(":horizontal", ":vertical", ":hover", ":pressed"))
                }
                widget in listOf("QCheckBox", "QRadioButton") || subControl == "indicator" -> {
                    priorityStates.addAll(listOf(":checked", ":unchecked", ":hover", ":disabled", ":enabled"))
                }
                widget in listOf("QTabBar", "QTabWidget") || subControl == "tab" -> {
                    priorityStates.addAll(listOf(":selected", ":hover", ":first", ":last", ":only-one"))
                }
                widget in listOf("QMenu", "QMenuBar") || subControl == "item" -> {
                    priorityStates.addAll(listOf(":selected", ":hover", ":disabled", ":enabled"))
                }
                widget == "QPushButton" -> {
                    priorityStates.addAll(listOf(":hover", ":pressed", ":default", ":flat", ":disabled"))
                }
                widget == "QComboBox" -> {
                    priorityStates.addAll(listOf(":hover", ":on", ":off", ":editable", ":disabled"))
                }
                widget == "QLineEdit" -> {
                    priorityStates.addAll(listOf(":focus", ":read-only", ":disabled", ":enabled"))
                }
            }

            // Return priority states first, then all others
            return (priorityStates + allStates).distinct()
        }
    }

    // Value completion provider (for properties inside braces)
    private class QssValueCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            resultSet: CompletionResultSet
        ) {
            val fileText = parameters.originalFile.text
            val offset = parameters.offset

            // Check if we're inside braces (property value context)
            val textBefore = fileText.substring(0, offset)
            val lastOpenBrace = textBefore.lastIndexOf('{')
            val lastCloseBrace = textBefore.lastIndexOf('}')

            // Only show value completion if we're inside braces
            if (lastOpenBrace <= lastCloseBrace || lastOpenBrace == -1) {
                return  // Not in a declaration block
            }

            // Add CSS functions
            QssData.FUNCTIONS.forEach { function ->
                resultSet.addElement(
                    LookupElementBuilder.create(function)
                        .withTypeText("Function")
                        .withInsertHandler { insertContext, _ ->
                            val editor = insertContext.editor
                            val document = editor.document
                            val caretOffset = editor.caretModel.offset
                            document.insertString(caretOffset, ")")
                        }
                )
            }

            // Add common CSS units
            QssData.UNITS.forEach { unit ->
                resultSet.addElement(
                    LookupElementBuilder.create(unit)
                        .withTypeText("Unit")
                )
            }

            // Add common property values
            QssData.COMMON_VALUES.forEach { value ->
                resultSet.addElement(
                    LookupElementBuilder.create(value)
                        .withTypeText("Value")
                )
            }
        }
    }

    companion object {
        // Helper function to find the current widget being styled
        private fun findCurrentWidget(fileText: String, offset: Int): String? {
            // Look backwards from cursor position to find the widget selector
            val textBeforeCursor = fileText.substring(0, minOf(offset, fileText.length))

            // Fixed regex: { doesn't need escaping inside character class
            val widgetPattern = Regex("""(Q[A-Za-z]+)(?:::|:|[\s{])""")
            val matches = widgetPattern.findAll(textBeforeCursor)

            // Get the last matched widget before current position
            return matches.lastOrNull()?.groupValues?.get(1)
        }
    }
}
