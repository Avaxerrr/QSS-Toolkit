package io.github.avaxerrr.qsstoolkit.completion

/**
 * Centralized data for QSS properties, widgets, and values.
 * Used by both CompletionContributor (for suggestions) and ValidationAnnotator (for error checking).
 */
object QssData {

    // Enum to define the expected type of value for validation
    enum class PropertyType {
        COLOR,
        MEASUREMENT, // Requires unit (px, pt) or 0
        NUMBER,      // Raw number (opacity) or Boolean (0/1)
        STRING,      // Generic text or specific enums
        URL,         // Strict URL check (url(...))
        BORDER       // Complex (width style color) - effectively "Any"
    }

    // Map properties to their expected value types
    val PROPERTY_TYPES = mapOf(
        "accent-color" to PropertyType.COLOR,
        "alignment" to PropertyType.STRING,
        "alternate-background-color" to PropertyType.COLOR,
        "background" to PropertyType.BORDER, // Complex (color + url + repeat)
        "background-attachment" to PropertyType.STRING,
        "background-clip" to PropertyType.STRING,
        "background-color" to PropertyType.COLOR,
        "background-image" to PropertyType.BORDER, // Relaxed to allow complex backgrounds
        "background-origin" to PropertyType.STRING,
        "background-position" to PropertyType.STRING,
        "background-repeat" to PropertyType.STRING,
        "border" to PropertyType.BORDER,
        "border-bottom" to PropertyType.BORDER,
        "border-bottom-color" to PropertyType.COLOR,
        "border-bottom-left-radius" to PropertyType.MEASUREMENT,
        "border-bottom-right-radius" to PropertyType.MEASUREMENT,
        "border-bottom-style" to PropertyType.STRING,
        "border-bottom-width" to PropertyType.MEASUREMENT,
        "border-color" to PropertyType.COLOR,
        "border-image" to PropertyType.BORDER, // Relaxed to allow url(...) + numbers
        "border-left" to PropertyType.BORDER,
        "border-left-color" to PropertyType.COLOR,
        "border-left-style" to PropertyType.STRING,
        "border-left-width" to PropertyType.MEASUREMENT,
        "border-radius" to PropertyType.MEASUREMENT,
        "border-right" to PropertyType.BORDER,
        "border-right-color" to PropertyType.COLOR,
        "border-right-style" to PropertyType.STRING,
        "border-right-width" to PropertyType.MEASUREMENT,
        "border-style" to PropertyType.STRING,
        "border-top" to PropertyType.BORDER,
        "border-top-color" to PropertyType.COLOR,
        "border-top-left-radius" to PropertyType.MEASUREMENT,
        "border-top-right-radius" to PropertyType.MEASUREMENT,
        "border-top-style" to PropertyType.STRING,
        "border-top-width" to PropertyType.MEASUREMENT,
        "border-width" to PropertyType.MEASUREMENT,
        "bottom" to PropertyType.MEASUREMENT,
        "button-layout" to PropertyType.NUMBER,
        "color" to PropertyType.COLOR,
        "dialogbuttonbox-buttons-have-icons" to PropertyType.NUMBER,
        "font" to PropertyType.STRING,
        "font-family" to PropertyType.STRING,
        "font-size" to PropertyType.MEASUREMENT,
        "font-style" to PropertyType.STRING,
        "font-weight" to PropertyType.STRING,
        "gridline-color" to PropertyType.COLOR,
        "height" to PropertyType.MEASUREMENT,
        "icon" to PropertyType.BORDER, // Relaxed
        "icon-size" to PropertyType.MEASUREMENT,
        "image" to PropertyType.BORDER, // Relaxed
        "image-position" to PropertyType.STRING,
        "left" to PropertyType.MEASUREMENT,
        "letter-spacing" to PropertyType.MEASUREMENT,
        "lineedit-password-character" to PropertyType.NUMBER,
        "margin" to PropertyType.MEASUREMENT,
        "margin-bottom" to PropertyType.MEASUREMENT,
        "margin-left" to PropertyType.MEASUREMENT,
        "margin-right" to PropertyType.MEASUREMENT,
        "margin-top" to PropertyType.MEASUREMENT,
        "max-height" to PropertyType.MEASUREMENT,
        "max-width" to PropertyType.MEASUREMENT,
        "messagebox-text-interaction-flags" to PropertyType.NUMBER,
        "min-height" to PropertyType.MEASUREMENT,
        "min-width" to PropertyType.MEASUREMENT,
        "opacity" to PropertyType.NUMBER,
        "outline" to PropertyType.BORDER,
        "outline-color" to PropertyType.COLOR,
        "outline-offset" to PropertyType.MEASUREMENT,
        "outline-radius" to PropertyType.MEASUREMENT,
        "outline-style" to PropertyType.STRING,
        "outline-width" to PropertyType.MEASUREMENT,
        "padding" to PropertyType.MEASUREMENT,
        "padding-bottom" to PropertyType.MEASUREMENT,
        "padding-left" to PropertyType.MEASUREMENT,
        "padding-right" to PropertyType.MEASUREMENT,
        "padding-top" to PropertyType.MEASUREMENT,
        "paint-alternating-row-colors-for-empty-area" to PropertyType.NUMBER,
        "placeholder-text-color" to PropertyType.COLOR,
        "position" to PropertyType.STRING,
        "qproperty-alignment" to PropertyType.STRING,
        "qproperty-wordWrap" to PropertyType.NUMBER,
        "right" to PropertyType.MEASUREMENT,
        "selection-background-color" to PropertyType.COLOR,
        "selection-color" to PropertyType.COLOR,
        "show-decoration-selected" to PropertyType.NUMBER,
        "spacing" to PropertyType.MEASUREMENT,
        "subcontrol-origin" to PropertyType.STRING,
        "subcontrol-position" to PropertyType.STRING,
        "text-align" to PropertyType.STRING,
        "text-decoration" to PropertyType.STRING,
        "titlebar-show-maximize-button" to PropertyType.NUMBER,
        "titlebar-show-minimize-button" to PropertyType.NUMBER,
        "top" to PropertyType.MEASUREMENT,
        "width" to PropertyType.MEASUREMENT,
        "word-spacing" to PropertyType.MEASUREMENT
    )

    // Helper for CompletionContributor
    val PROPERTIES = PROPERTY_TYPES.keys.toList()

    // List of standard Qt Widgets (Qt 6 + 5 superset)
    val WIDGET_SELECTORS = listOf(
        "QAbstractItemView",
        "QAbstractScrollArea",
        "QCalendarWidget",
        "QCheckBox",
        "QColumnView",
        "QComboBox",
        "QDateEdit",
        "QDateTimeEdit",
        "QDialog",
        "QDialogButtonBox",
        "QDockWidget",
        "QDoubleSpinBox",
        "QFrame",
        "QGroupBox",
        "QHeaderView",
        "QKeySequenceEdit",
        "QLabel",
        "QLineEdit",
        "QListView",
        "QListWidget",
        "QMainWindow",
        "QMdiArea",
        "QMdiSubWindow",
        "QMenu",
        "QMenuBar",
        "QMessageBox",
        "QPlainTextEdit",
        "QProgressBar",
        "QPushButton",
        "QRadioButton",
        "QScrollArea",
        "QScrollBar",
        "QSizeGrip",
        "QSlider",
        "QSpinBox",
        "QSplitter",
        "QStackedWidget",
        "QStatusBar",
        "QTabBar",
        "QTabWidget",
        "QTableView",
        "QTableWidget",
        "QTextBrowser",
        "QTextEdit",
        "QTimeEdit",
        "QToolBar",
        "QToolBox",
        "QToolButton",
        "QToolTip",
        "QTreeView",
        "QTreeWidget",
        "QWidget",
        "QWizard",
        "QWizardPage"
    )

    // List of pseudo-states
    val PSEUDO_STATES = listOf(
        ":active", ":adjoins-item", ":alternate", ":bottom", ":checked", ":closable", ":closed",
        ":default", ":dir", ":dir(ltr)", ":dir(rtl)", ":disabled", ":edit-focus", ":editable",
        ":enabled", ":first", ":flat", ":floatable", ":focus", ":has-children", ":has-siblings",
        ":horizontal", ":hover", ":indeterminate", ":last", ":left", ":maximized", ":middle",
        ":minimized", ":movable", ":next-selected", ":no-frame", ":non-exclusive-indicator",
        ":off", ":on", ":only-one", ":open", ":pressed", ":previous-selected", ":read-only",
        ":right", ":selected", ":top", ":unchecked", ":vertical", ":window"
    )

    // Map specific sub-controls to widgets
    val WIDGET_SUBCONTROLS = mapOf(
        "QAbstractItemView" to listOf("::item"),
        "QCheckBox" to listOf("::indicator"),
        "QComboBox" to listOf("::drop-down", "::down-arrow"),
        "QDateTimeEdit" to listOf("::up-button", "::down-button", "::up-arrow", "::down-arrow"),
        "QDockWidget" to listOf("::title", "::close-button", "::float-button"),
        "QDoubleSpinBox" to listOf("::up-button", "::down-button", "::up-arrow", "::down-arrow"),
        "QGroupBox" to listOf("::title", "::indicator"),
        "QHeaderView" to listOf("::section", "::down-arrow", "::up-arrow"),
        "QListView" to listOf("::item"),
        "QListWidget" to listOf("::item"),
        "QMdiSubWindow" to listOf("::title", "::close-button", "::minimize-button", "::maximize-button"),
        "QMenu" to listOf("::item", "::indicator", "::separator", "::tear-off", "::icon", "::right-arrow", "::scroller"),
        "QMenuBar" to listOf("::item"),
        "QProgressBar" to listOf("::chunk"),
        "QRadioButton" to listOf("::indicator"),
        "QScrollBar" to listOf("::handle", "::add-line", "::sub-line", "::add-page", "::sub-page", "::up-arrow", "::down-arrow", "::left-arrow", "::right-arrow"),
        "QSlider" to listOf("::groove", "::handle", "::add-page", "::sub-page"),
        "QSpinBox" to listOf("::up-button", "::down-button", "::up-arrow", "::down-arrow"),
        "QTabBar" to listOf("::tab", "::scroller", "::tear", "::close-button"),
        "QTabWidget" to listOf("::pane", "::tab-bar"),
        "QTableView" to listOf("::item"),
        "QTableWidget" to listOf("::item"),
        "QToolBox" to listOf("::tab"),
        "QToolButton" to listOf("::menu-button", "::menu-indicator", "::menu-arrow"),
        "QTreeView" to listOf("::item", "::branch"),
        "QTreeWidget" to listOf("::item", "::branch"),
        "QPushButton" to emptyList(),
        "QLabel" to emptyList(),
        "QLineEdit" to emptyList(),
        "QTextEdit" to emptyList(),
        "QPlainTextEdit" to emptyList(),
        "QFrame" to emptyList(),
        "QWidget" to emptyList(),
        "QDialog" to emptyList(),
        "QMainWindow" to emptyList(),
        "QStatusBar" to emptyList()
    )

    // Common Subcontrols fallback
    val COMMON_SUBCONTROLS = listOf(
        "::indicator", "::item", "::icon", "::text", "::title", "::menu-arrow", "::down-arrow", "::up-arrow",
        "::drop-down", "::scroller", "::tear", "::handle", "::groove", "::add-page", "::sub-page",
        "::add-line", "::sub-line", "::chunk", "::separator", "::section", "::tab-bar", "::pane", "::tab"
    )

    // Values for Completion
    val COMMON_VALUES = listOf(
        "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque", "black", "blanchedalmond",
        "blue", "blueviolet", "bold", "bottom", "brown", "burlywood", "cadetblue", "center", "chartreuse",
        "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson", "cyan", "darkblue", "darkcyan",
        "darkgoldenrod", "darkgray", "darkgreen", "darkgrey", "darkkhaki", "darkmagenta", "darkolivegreen",
        "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen", "darkslateblue", "darkslategray",
        "darkslategrey", "darkturquoise", "darkviolet", "deeppink", "deepskyblue", "dimgray", "dimgrey",
        "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro", "ghostwhite", "gold",
        "goldenrod", "gray", "green", "greenyellow", "grey", "honeydew", "hotpink", "indianred", "indigo",
        "italic", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "left", "lemonchiffon",
        "lightblue", "lightcoral", "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey",
        "lightpink", "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey",
        "lightsteelblue", "lightyellow", "lime", "limegreen", "linen", "magenta", "maroon", "mediumaquamarine",
        "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen",
        "mediumturquoise", "mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin", "navajowhite",
        "navy", "none", "normal", "oldlace", "olive", "olivedrab", "orange", "orangered", "orchid",
        "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru",
        "pink", "plum", "powderblue", "purple", "red", "repeat", "repeat-x", "repeat-y", "right",
        "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen", "seashell", "sienna",
        "silver", "skyblue", "slateblue", "slategray", "slategrey", "snow", "solid", "springgreen",
        "steelblue", "tan", "teal", "thistle", "tomato", "top", "transparent", "turquoise", "underline",
        "violet", "wheat", "white", "whitesmoke", "yellow", "yellowgreen"
    )

    // Functions
    val FUNCTIONS = listOf(
        "url()", "rgb()", "rgba()", "hsl()", "hsla()", "qlineargradient()",
        "qradialgradient()", "qconicalgradient()", "palette()"
    )

    // Units
    val UNITS = listOf("px", "pt", "em", "ex")
}