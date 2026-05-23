package io.github.avaxerrr.qsstoolkit

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Paths

class QssFileHighlightingTest : BasePlatformTestCase() {
    fun testCommonExistingQssSyntaxStillHasNoHighlightingErrors() {
        val file = myFixture.configureByText(
            "common-existing.qss",
            readSmokeTestFile("common-existing.qss")
        )

        assertEquals("QSS", file.fileType.name)
        assertNoHighlightingProblems()
    }

    fun testValidCurrentQtSyntaxInQssFileHasNoHighlightingErrors() {
        val file = myFixture.configureByText(
            "valid-current-qt.qss",
            readSmokeTestFile("valid-current-qt.qss")
        )

        assertEquals("QSS", file.fileType.name)
        assertNoHighlightingProblems()
    }

    fun testIncompleteBorderStillReportsWeakWarning() {
        myFixture.configureByText(
            "incomplete-border.qss",
            """
            QPushButton {
                border: solid;
            }
            """.trimIndent()
        )

        val warnings = myFixture.doHighlighting()
            .filter { it.severity == HighlightSeverity.WEAK_WARNING }
            .mapNotNull { it.description }

        assertTrue(warnings.any { it.contains("Incomplete border declaration") })
    }

    private fun readSmokeTestFile(fileName: String): String =
        Paths.get("src", "test", "testData", "qss", "manual-smoke", fileName).toFile().readText()

    private fun assertNoHighlightingProblems() {
        val problems = myFixture.doHighlighting()
            .filter {
                it.severity == HighlightSeverity.ERROR ||
                    it.severity == HighlightSeverity.WARNING ||
                    it.severity == HighlightSeverity.WEAK_WARNING
            }
            .map { "${it.description ?: it.text}: ${it.startOffset}-${it.endOffset}" }

        assertEquals(emptyList<String>(), problems)
    }
}
