package io.github.avaxerrr.qsstoolkit

import io.github.avaxerrr.qsstoolkit.livereload.QssLiveReloadFramework
import io.github.avaxerrr.qsstoolkit.livereload.QssLivePreviewHelpers
import io.github.avaxerrr.qsstoolkit.livereload.QssLiveReloadSnippets
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class QssLiveReloadSnippetsTest {
    @Test
    fun `uses project helper file names by framework`() {
        assertEquals("qss_live_preview.py", QssLivePreviewHelpers.helperFileName(QssLiveReloadFramework.PYSIDE6))
        assertEquals("qss_live_preview.py", QssLivePreviewHelpers.helperFileName(QssLiveReloadFramework.PYQT6))
        assertEquals("qss_live_preview.py", QssLivePreviewHelpers.helperFileName(QssLiveReloadFramework.PYSIDE2_PYQT5))
        assertEquals("qss_live_preview.h", QssLivePreviewHelpers.helperFileName(QssLiveReloadFramework.CPP_QT))
    }

    @Test
    fun `generates PySide6 live preview helper`() {
        val helper = QssLivePreviewHelpers.generateHelper(
            QssLiveReloadFramework.PYSIDE6,
            "C:/project/styles/app.qss"
        )

        assertContains(helper, "from PySide6.QtCore import QFileSystemWatcher, QTimer")
        assertContains(helper, "QSS_PATH = Path(\"C:/project/styles/app.qss\")")
        assertContains(helper, "def enable_qss_live_preview")
        assertContains(helper, "watcher.fileChanged.connect(reload_qss)")
        assertContains(helper, "watcher.directoryChanged.connect(reload_qss)")
        assertContains(helper, "return watcher")
    }

    @Test
    fun `generates PyQt6 live preview helper`() {
        val helper = QssLivePreviewHelpers.generateHelper(
            QssLiveReloadFramework.PYQT6,
            "C:/project/styles/app.qss"
        )

        assertContains(helper, "from PyQt6.QtCore import QFileSystemWatcher, QTimer")
        assertContains(helper, "QSS_PATH = Path(\"C:/project/styles/app.qss\")")
    }

    @Test
    fun `generates Qt5 compatible Python live preview helper`() {
        val helper = QssLivePreviewHelpers.generateHelper(
            QssLiveReloadFramework.PYSIDE2_PYQT5,
            "C:/project/styles/app.qss"
        )

        assertContains(helper, "from PySide2.QtCore import QFileSystemWatcher, QTimer")
        assertContains(helper, "from PyQt5.QtCore import QFileSystemWatcher, QTimer")
        assertContains(helper, "QSS_PATH = Path(\"C:/project/styles/app.qss\")")
    }

    @Test
    fun `generates C++ Qt live preview helper`() {
        val helper = QssLivePreviewHelpers.generateHelper(
            QssLiveReloadFramework.CPP_QT,
            "C:/project/styles/app.qss"
        )

        assertContains(helper, "#pragma once")
        assertContains(helper, "enableQssLivePreview")
        assertContains(helper, "QStringLiteral(\"C:/project/styles/app.qss\")")
        assertContains(helper, "QFileSystemWatcher")
        assertContains(helper, "QTimer::singleShot")
        assertContains(helper, "return watcher;")
    }

    @Test
    fun `generates setup call for created helpers`() {
        assertContains(
            QssLivePreviewHelpers.setupCall(QssLiveReloadFramework.PYSIDE6),
            "qss_watcher = enable_qss_live_preview(app)"
        )
        assertContains(
            QssLivePreviewHelpers.setupCall(QssLiveReloadFramework.CPP_QT),
            "enableQssLivePreview(qApp);"
        )
    }

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
