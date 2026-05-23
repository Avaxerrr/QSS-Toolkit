package io.github.avaxerrr.qsstoolkit.livereload

enum class QssLiveReloadFramework(val label: String) {
    PYSIDE6("PySide6"),
    PYQT6("PyQt6"),
    PYSIDE2_PYQT5("PySide2 / PyQt5"),
    CPP_QT("C++ Qt")
}

object QssLiveReloadSnippets {
    fun generate(framework: QssLiveReloadFramework, qssPath: String): String {
        return when (framework) {
            QssLiveReloadFramework.PYSIDE6 -> pythonSnippet(
                importLine = "from PySide6.QtCore import QFileSystemWatcher, QTimer",
                qssPath = qssPath
            )
            QssLiveReloadFramework.PYQT6 -> pythonSnippet(
                importLine = "from PyQt6.QtCore import QFileSystemWatcher, QTimer",
                qssPath = qssPath
            )
            QssLiveReloadFramework.PYSIDE2_PYQT5 -> pythonSnippet(
                importLine = """
                    try:
                        from PySide2.QtCore import QFileSystemWatcher, QTimer
                    except ImportError:
                        from PyQt5.QtCore import QFileSystemWatcher, QTimer
                """.trimIndent(),
                qssPath = qssPath
            )
            QssLiveReloadFramework.CPP_QT -> cppSnippet(qssPath)
        }
    }

    private fun pythonSnippet(importLine: String, qssPath: String): String {
        return """
            # Paste after QApplication is created. This snippet expects your app variable to be named `app`.
            from pathlib import Path
            $importLine
            
            QSS_PATH = Path(${qssPath.toPythonStringLiteral()})
            QSS_RELOAD_DELAY_MS = 80
            
            def apply_qss():
                try:
                    app.setStyleSheet(QSS_PATH.read_text(encoding="utf-8"))
                except OSError as error:
                    print(f"QSS reload failed: {error}")
            
            qss_watcher = QFileSystemWatcher()
            
            def watch_qss():
                directory = str(QSS_PATH.parent)
                if directory not in qss_watcher.directories():
                    qss_watcher.addPath(directory)
            
                file_path = str(QSS_PATH)
                if QSS_PATH.exists() and file_path not in qss_watcher.files():
                    qss_watcher.addPath(file_path)
            
            def reload_qss(_changed_path=None):
                watch_qss()
                QTimer.singleShot(QSS_RELOAD_DELAY_MS, apply_qss)
            
            qss_watcher.fileChanged.connect(reload_qss)
            qss_watcher.directoryChanged.connect(reload_qss)
            
            watch_qss()
            apply_qss()
        """.trimIndent()
    }

    private fun cppSnippet(qssPath: String): String {
        return """
            // Paste after QApplication is created.
            // Required includes:
            // #include <QApplication>
            // #include <QFile>
            // #include <QFileInfo>
            // #include <QFileSystemWatcher>
            // #include <QTimer>
            
            const QString qssPath = QStringLiteral(${qssPath.toCppStringLiteral()});
            const int qssReloadDelayMs = 80;
            
            auto applyQss = [qssPath]() {
                QFile file(qssPath);
                if (file.open(QIODevice::ReadOnly | QIODevice::Text)) {
                    qApp->setStyleSheet(QString::fromUtf8(file.readAll()));
                }
            };
            
            auto watcher = new QFileSystemWatcher(qApp);
            auto watchQss = [watcher, qssPath]() {
                const QFileInfo fileInfo(qssPath);
                const QString directory = fileInfo.absolutePath();
                if (!watcher->directories().contains(directory)) {
                    watcher->addPath(directory);
                }
                if (fileInfo.exists() && !watcher->files().contains(qssPath)) {
                    watcher->addPath(qssPath);
                }
            };
            
            auto reloadQss = [watchQss, applyQss, qssReloadDelayMs]() {
                watchQss();
                QTimer::singleShot(qssReloadDelayMs, qApp, applyQss);
            };
            
            QObject::connect(watcher, &QFileSystemWatcher::fileChanged, qApp, reloadQss);
            QObject::connect(watcher, &QFileSystemWatcher::directoryChanged, qApp, reloadQss);
            
            watchQss();
            applyQss();
        """.trimIndent()
    }

    private fun String.toPythonStringLiteral(): String {
        return "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }

    private fun String.toCppStringLiteral(): String {
        return "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }
}
