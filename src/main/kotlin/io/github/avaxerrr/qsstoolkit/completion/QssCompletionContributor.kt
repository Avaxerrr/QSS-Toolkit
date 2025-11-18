// QSS Toolkit version 2.0

package io.github.avaxerrr.qsstoolkit.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
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
            WIDGET_SELECTORS.forEach { widget ->
                resultSet.addElement(
                    LookupElementBuilder.create(widget)
                        .withTypeText("Widget Selector")
                        .withIcon(null)
                )
            }

            // Add properties
            PROPERTIES.forEach { property ->
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

            if (currentWidget != null && WIDGET_SUBCONTROLS.containsKey(currentWidget)) {
                // Add sub-controls specific to this widget
                WIDGET_SUBCONTROLS[currentWidget]?.forEach { subcontrol ->
                    resultSet.addElement(
                        LookupElementBuilder.create(subcontrol.removePrefix("::"))
                            .withPresentableText(subcontrol)
                            .withTypeText("Sub-control for $currentWidget")
                            .withIcon(null)
                    )
                }
            } else {
                // Add all common sub-controls if we can't determine the widget
                COMMON_SUBCONTROLS.forEach { subcontrol ->
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

                    if (currentWidget != null && WIDGET_SUBCONTROLS.containsKey(currentWidget)) {
                        // Add sub-controls specific to this widget
                        WIDGET_SUBCONTROLS[currentWidget]?.forEach { subcontrol ->
                            resultSet.addElement(
                                LookupElementBuilder.create(subcontrol.removePrefix("::"))
                                    .withPresentableText(subcontrol)
                                    .withTypeText("Sub-control for $currentWidget")
                                    .withIcon(null)
                            )
                        }
                    } else {
                        // Add all common sub-controls
                        COMMON_SUBCONTROLS.forEach { subcontrol ->
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
            val allStates = PSEUDO_STATES.toMutableList()
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
            FUNCTIONS.forEach { function ->
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
            UNITS.forEach { unit ->
                resultSet.addElement(
                    LookupElementBuilder.create(unit)
                        .withTypeText("Unit")
                )
            }

            // Add common property values
            COMMON_VALUES.forEach { value ->
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

        // Complete list of Qt 6.10 Style Sheet Properties (96 properties)
        private val PROPERTIES = listOf(
            "accent-color", "alternate-background-color", "selection-background-color", "selection-color",
            "background", "background-attachment", "background-clip", "background-color", "background-image",
            "background-origin", "background-position", "background-repeat",
            "border", "border-bottom", "border-bottom-color", "border-bottom-left-radius",
            "border-bottom-right-radius", "border-bottom-style", "border-bottom-width", "border-color",
            "border-image", "border-left", "border-left-color", "border-left-style", "border-left-width",
            "border-radius", "border-right", "border-right-color", "border-right-style", "border-right-width",
            "border-style", "border-top", "border-top-color", "border-top-left-radius", "border-top-right-radius",
            "border-top-style", "border-top-width", "border-width",
            "bottom", "left", "position", "right", "top",
            "button-layout", "dialogbuttonbox-buttons-have-icons",
            "color", "placeholder-text-color",
            "font", "font-family", "font-size", "font-style", "font-weight", "letter-spacing",
            "gridline-color", "icon", "icon-size", "image", "image-position",
            "lineedit-password-character", "lineedit-password-mask-delay",
            "height", "max-height", "max-width", "min-height", "min-width", "width",
            "margin", "margin-bottom", "margin-left", "margin-right", "margin-top",
            "messagebox-text-interaction-flags", "opacity",
            "outline", "outline-bottom-left-radius", "outline-bottom-right-radius", "outline-color",
            "outline-offset", "outline-radius", "outline-style", "outline-top-left-radius", "outline-top-right-radius",
            "padding", "padding-bottom", "padding-left", "padding-right", "padding-top",
            "paint-alternating-row-colors-for-empty-area", "show-decoration-selected",
            "spacing", "subcontrol-origin", "subcontrol-position",
            "text-align", "text-decoration", "titlebar-show-tooltips-on-buttons", "widget-animation-duration"
        )

        // Complete list of styleable Qt widgets (50 widgets)
        private val WIDGET_SELECTORS = listOf(
            "QAbstractButton", "QAbstractItemView", "QAbstractScrollArea",
            "QCheckBox", "QCommandLinkButton", "QComboBox", "QPushButton", "QRadioButton",
            "QDateEdit", "QDateTimeEdit", "QTimeEdit",
            "QDial", "QDoubleSpinBox", "QFontComboBox", "QLCDNumber", "QLineEdit", "QSlider", "QSpinBox",
            "QDialog", "QDialogButtonBox", "QDockWidget", "QFrame", "QGroupBox", "QMainWindow",
            "QSplitter", "QStackedWidget", "QWidget",
            "QLabel", "QProgressBar", "QToolTip",
            "QMenu", "QMenuBar", "QStatusBar", "QToolBar", "QToolBox", "QToolButton",
            "QScrollArea", "QScrollBar", "QSizeGrip",
            "QTabBar", "QTabWidget",
            "QColumnView", "QHeaderView", "QListView", "QListWidget", "QTableView", "QTableWidget",
            "QTreeView", "QTreeWidget", "QTextEdit", "QMessageBox"
        )

        // Widget-specific sub-controls mapping
        private val WIDGET_SUBCONTROLS = mapOf(
            "QCheckBox" to listOf("::indicator"),
            "QColumnView" to listOf("::left-arrow", "::right-arrow"),
            "QComboBox" to listOf("::drop-down", "::down-arrow"),
            "QDateEdit" to listOf("::up-button", "::up-arrow", "::down-button", "::down-arrow"),
            "QDateTimeEdit" to listOf("::up-button", "::up-arrow", "::down-button", "::down-arrow"),
            "QTimeEdit" to listOf("::up-button", "::up-arrow", "::down-button", "::down-arrow"),
            "QDockWidget" to listOf("::title", "::close-button", "::float-button"),
            "QDoubleSpinBox" to listOf("::up-button", "::up-arrow", "::down-button", "::down-arrow"),
            "QGroupBox" to listOf("::title", "::indicator"),
            "QHeaderView" to listOf("::section", "::up-arrow", "::down-arrow"),
            "QListView" to listOf("::item"),
            "QListWidget" to listOf("::item"),
            "QMainWindow" to listOf("::separator"),
            "QMenu" to listOf("::item", "::indicator", "::separator", "::right-arrow", "::left-arrow", "::scroller", "::tearoff"),
            "QMenuBar" to listOf("::item"),
            "QProgressBar" to listOf("::chunk"),
            "QPushButton" to listOf("::menu-indicator"),
            "QRadioButton" to listOf("::indicator"),
            "QScrollBar" to listOf("::handle", "::add-line", "::sub-line", "::add-page", "::sub-page", "::up-arrow", "::down-arrow", "::left-arrow", "::right-arrow"),
            "QSlider" to listOf("::groove", "::handle"),
            "QSpinBox" to listOf("::up-button", "::up-arrow", "::down-button", "::down-arrow"),
            "QSplitter" to listOf("::handle"),
            "QStatusBar" to listOf("::item"),
            "QTabBar" to listOf("::tab", "::close-button", "::tear", "::scroller"),
            "QTabWidget" to listOf("::pane", "::tab-bar", "::left-corner", "::right-corner"),
            "QToolBar" to listOf("::separator", "::handle"),
            "QToolBox" to listOf("::tab"),
            "QToolButton" to listOf("::menu-indicator", "::menu-button", "::menu-arrow", "::up-arrow", "::down-arrow", "::left-arrow", "::right-arrow"),
            "QTreeView" to listOf("::branch", "::item"),
            "QTreeWidget" to listOf("::branch", "::item")
        )

        // Common sub-controls
        private val COMMON_SUBCONTROLS = listOf(
            "::item", "::indicator", "::handle", "::separator", "::title",
            "::up-arrow", "::down-arrow", "::left-arrow", "::right-arrow",
            "::drop-down", "::tab", "::branch", "::chunk", "::groove",
            "::up-button", "::down-button", "::add-line", "::sub-line"
        )

        // QSS functions
        private val FUNCTIONS = listOf(
            "rgb(", "rgba(", "url(", "qlineargradient(", "qradialgradient(", "qconicalgradient("
        )

        // Complete pseudo-states (44 states)
        private val PSEUDO_STATES = listOf(
            ":active", ":adjoins-item", ":alternate", ":bottom", ":checked",
            ":closable", ":closed", ":default", ":disabled", ":editable",
            ":edit-focus", ":enabled", ":exclusive", ":first", ":flat",
            ":floatable", ":focus", ":has-children", ":has-siblings",
            ":horizontal", ":hover", ":indeterminate", ":last", ":left",
            ":maximized", ":middle", ":minimized", ":movable", ":no-frame",
            ":non-exclusive", ":off", ":on", ":only-one", ":open",
            ":next-selected", ":pressed", ":previous-selected", ":read-only",
            ":right", ":selected", ":top", ":unchecked", ":vertical", ":window"
        )

        // CSS Units
        private val UNITS = listOf("px", "pt", "em", "ex", "%")

        // Common property values
        private val COMMON_VALUES = listOf(
            "left", "right", "center", "justify", "top", "bottom", "middle",
            "none", "solid", "dashed", "dotted", "double", "groove", "ridge", "inset", "outset",
            "normal", "bold", "bolder", "lighter",
            "italic", "oblique",
            "underline", "overline", "line-through",
            "auto", "transparent",
            "white", "black", "red", "green", "blue", "yellow", "cyan", "magenta", "gray", "grey"
        )
    }
}
