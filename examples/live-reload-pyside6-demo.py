import sys
from datetime import datetime
from pathlib import Path

from PySide6.QtCore import QFileSystemWatcher, QObject, QTimer, Qt
from PySide6.QtWidgets import (
    QApplication,
    QCheckBox,
    QFrame,
    QHBoxLayout,
    QLabel,
    QLineEdit,
    QMainWindow,
    QPushButton,
    QSlider,
    QVBoxLayout,
    QWidget,
)


QSS_PATH = Path(__file__).with_name("live-reload-demo.qss").resolve()
RELOAD_DELAY_MS = 80


class LiveReloadStyleSheet(QObject):
    def __init__(self, app, qss_path, status_label):
        super().__init__()
        self.app = app
        self.qss_path = Path(qss_path).resolve()
        self.status_label = status_label
        self.watcher = QFileSystemWatcher()
        self.reload_timer = QTimer(self)
        self.reload_timer.setSingleShot(True)
        self.reload_timer.setInterval(RELOAD_DELAY_MS)
        self.reload_timer.timeout.connect(self.reload)

        self.watcher.fileChanged.connect(self.schedule_reload)
        self.watcher.directoryChanged.connect(self.schedule_reload)
        self.watch_paths()
        self.reload()

    def watch_paths(self):
        directory = str(self.qss_path.parent)
        if directory not in self.watcher.directories():
            self.watcher.addPath(directory)

        file_path = str(self.qss_path)
        if self.qss_path.exists() and file_path not in self.watcher.files():
            self.watcher.addPath(file_path)

    def schedule_reload(self, _changed_path=None):
        self.reload_timer.start()

    def reload(self):
        self.watch_paths()

        try:
            qss_text = self.qss_path.read_text(encoding="utf-8")
        except OSError as error:
            self.status_label.setText(f"QSS reload failed: {error}")
            return

        self.app.setStyleSheet(qss_text)
        self.repolish_widgets()
        timestamp = datetime.now().strftime("%H:%M:%S")
        self.status_label.setText(f"Loaded {self.qss_path.name} at {timestamp}")

    def repolish_widgets(self):
        for widget in self.app.allWidgets():
            widget.style().unpolish(widget)
            widget.style().polish(widget)
            widget.update()


class DemoWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QSS Live Reload Demo")
        self.resize(520, 380)

        self.status_label = QLabel()
        self.status_label.setObjectName("ReloadStatus")

        title = QLabel("QSS Live Reload Demo")
        title.setObjectName("Title")

        description = QLabel(f"Edit and save:\n{QSS_PATH}")
        description.setObjectName("Description")
        description.setTextInteractionFlags(Qt.TextSelectableByMouse)

        primary_button = QPushButton("Primary Button")
        primary_button.setObjectName("PrimaryButton")

        secondary_button = QPushButton("Secondary Button")
        secondary_button.setObjectName("SecondaryButton")

        button_row = QHBoxLayout()
        button_row.addWidget(primary_button)
        button_row.addWidget(secondary_button)

        input_field = QLineEdit()
        input_field.setPlaceholderText("Try changing border, text, and focus styles")

        slider = QSlider(Qt.Horizontal)
        slider.setValue(42)

        checkbox = QCheckBox("Checkbox style also reloads")
        checkbox.setChecked(True)

        card = QFrame()
        card.setObjectName("DemoCard")
        card_layout = QVBoxLayout(card)
        card_layout.addWidget(QLabel("Card content"))
        card_layout.addWidget(input_field)
        card_layout.addWidget(slider)
        card_layout.addWidget(checkbox)

        root = QWidget()
        layout = QVBoxLayout(root)
        layout.addWidget(title)
        layout.addWidget(description)
        layout.addLayout(button_row)
        layout.addWidget(card)
        layout.addStretch()
        layout.addWidget(self.status_label)

        self.setCentralWidget(root)


def main():
    app = QApplication(sys.argv)
    window = DemoWindow()
    reloader = LiveReloadStyleSheet(app, QSS_PATH, window.status_label)
    window._qss_reloader = reloader
    window.show()
    sys.exit(app.exec())


if __name__ == "__main__":
    main()
