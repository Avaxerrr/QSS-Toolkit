package io.github.avaxerrr.qsstoolkit

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class QssFileHighlightingTest : BasePlatformTestCase() {
    fun testCommonExistingQssSyntaxStillHasNoHighlightingErrors() {
        val file = myFixture.configureByText(
            "common-existing.qss",
            """
            QPushButton:hover {
                background-color: #336699;
                border: 1px solid palette(WindowText);
                border-radius: 4px;
                color: rgba(255, 255, 255, 180);
                padding: 6px 12px;
            }

            QScrollBar::handle:vertical {
                background: qlineargradient(x1:0, y1:0, x2:0, y2:1, stop:0 #eeeeee, stop:1 #cccccc);
                min-height: 20px;
            }

            QMenu::item:selected {
                selection-background-color: hsv(210, 80, 190);
            }

            QCheckBox::indicator:checked {
                image: url(:/icons/check.svg);
            }
            """.trimIndent()
        )

        assertEquals("QSS", file.fileType.name)
        assertNoHighlightingErrors()
    }

    fun testValidCurrentQtSyntaxInQssFileHasNoHighlightingErrors() {
        val file = myFixture.configureByText(
            "valid-current-qt.qss",
            """
            QWidget {
                -qt-background-role: Window;
                -qt-style-features: icon;
                widget-animation-duration: 150;
            }

            QLineEdit {
                lineedit-password-mask-delay: 500;
                lineedit-clear-button-icon: url(:/icons/clear.svg);
                qproperty-wordWrap: 1;
                qproperty-titleColor: palette(WindowText);
            }

            QToolButton::up-arrow:exclusive {
                image: url(:/icons/up.svg);
            }

            QMenu::tearoff {
                image: url(:/icons/tearoff.svg);
            }

            QTabWidget::left-corner {
                width: 12px;
            }

            QPushButton::menu-indicator {
                image: url(:/icons/menu.svg);
            }
            """.trimIndent()
        )

        assertEquals("QSS", file.fileType.name)
        assertNoHighlightingErrors()
    }

    private fun assertNoHighlightingErrors() {
        val errors = myFixture.doHighlighting()
            .filter { it.severity == HighlightSeverity.ERROR }
            .map { "${it.description ?: it.text}: ${it.startOffset}-${it.endOffset}" }

        assertEquals(emptyList<String>(), errors)
    }
}
