// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.completion

/**
 * Centralized data for QSS properties, widgets, and values.
 * Used by both CompletionContributor (for suggestions) and ValidationAnnotator (for error checking).
 */
object QssData {
    // Complete list of Qt 6.10 Style Sheet Properties (96 properties)
    val PROPERTIES = listOf(
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
    val WIDGET_SELECTORS = listOf(
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
    val WIDGET_SUBCONTROLS = mapOf(
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
    val COMMON_SUBCONTROLS = listOf(
        "::item", "::indicator", "::handle", "::separator", "::title",
        "::up-arrow", "::down-arrow", "::left-arrow", "::right-arrow",
        "::drop-down", "::tab", "::branch", "::chunk", "::groove",
        "::up-button", "::down-button", "::add-line", "::sub-line"
    )

    // QSS functions
    val FUNCTIONS = listOf(
        "rgb(", "rgba(", "url(", "qlineargradient(", "qradialgradient(", "qconicalgradient("
    )

    // Complete pseudo-states (44 states)
    val PSEUDO_STATES = listOf(
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
    val UNITS = listOf("px", "pt", "em", "ex", "%")

    // Common property values
    val COMMON_VALUES = listOf(
        "left", "right", "center", "justify", "top", "bottom", "middle",
        "none", "solid", "dashed", "dotted", "double", "groove", "ridge", "inset", "outset",
        "normal", "bold", "bolder", "lighter",
        "italic", "oblique",
        "underline", "overline", "line-through",
        "auto", "transparent",
        "white", "black", "red", "green", "blue", "yellow", "cyan", "magenta", "gray", "grey"
    )
}
