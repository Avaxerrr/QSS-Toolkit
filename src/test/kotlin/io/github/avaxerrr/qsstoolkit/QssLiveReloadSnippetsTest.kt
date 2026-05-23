package io.github.avaxerrr.qsstoolkit

import io.github.avaxerrr.qsstoolkit.livereload.QssLiveReloadFramework
import io.github.avaxerrr.qsstoolkit.livereload.QssLiveReloadSnippets
import kotlin.test.Test
import kotlin.test.assertContains

class QssLiveReloadSnippetsTest {
    @Test
    fun `generates PySide6 watcher snippet`() {
        val snippet = QssLiveReloadSnippets.generate(
            QssLiveReloadFramework.PYSIDE6,
            "C:/project/styles/app.qss"
        )

        assertContains(snippet, "from PySide6.QtCore import QFileSystemWatcher, QTimer")
        assertContains(snippet, "QSS_PATH = Path(\"C:/project/styles/app.qss\")")
        assertContains(snippet, "qss_watcher.fileChanged.connect(reload_qss)")
        assertContains(snippet, "qss_watcher.directoryChanged.connect(reload_qss)")
        assertContains(snippet, "QTimer.singleShot(QSS_RELOAD_DELAY_MS, apply_qss)")
        assertContains(snippet, "app.setStyleSheet")
    }

    @Test
    fun `generates PyQt6 watcher snippet`() {
        val snippet = QssLiveReloadSnippets.generate(
            QssLiveReloadFramework.PYQT6,
            "C:/project/styles/app.qss"
        )

        assertContains(snippet, "from PyQt6.QtCore import QFileSystemWatcher, QTimer")
        assertContains(snippet, "QSS_PATH = Path(\"C:/project/styles/app.qss\")")
    }

    @Test
    fun `generates Qt5 compatible Python watcher snippet`() {
        val snippet = QssLiveReloadSnippets.generate(
            QssLiveReloadFramework.PYSIDE2_PYQT5,
            "C:/project/styles/app.qss"
        )

        assertContains(snippet, "from PySide2.QtCore import QFileSystemWatcher, QTimer")
        assertContains(snippet, "from PyQt5.QtCore import QFileSystemWatcher, QTimer")
        assertContains(snippet, "QSS_PATH = Path(\"C:/project/styles/app.qss\")")
    }

    @Test
    fun `generates C++ Qt watcher snippet`() {
        val snippet = QssLiveReloadSnippets.generate(
            QssLiveReloadFramework.CPP_QT,
            "C:/project/styles/app.qss"
        )

        assertContains(snippet, "QFileSystemWatcher")
        assertContains(snippet, "QStringLiteral(\"C:/project/styles/app.qss\")")
        assertContains(snippet, "qApp->setStyleSheet")
        assertContains(snippet, "QTimer::singleShot")
        assertContains(snippet, "directoryChanged")
    }
}
