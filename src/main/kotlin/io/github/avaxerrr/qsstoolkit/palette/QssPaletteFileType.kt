package io.github.avaxerrr.qsstoolkit.palette

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import io.github.avaxerrr.qsstoolkit.QssIcons
import javax.swing.Icon

class QssPaletteFileType : FileType {
    override fun getName(): String = NAME

    override fun getDescription(): String = DESCRIPTION

    override fun getDefaultExtension(): String = QssColorPaletteFileFormat.FILE_EXTENSION

    override fun getIcon(): Icon = QssIcons.PALETTE_FILE

    override fun isBinary(): Boolean = false

    override fun isReadOnly(): Boolean = false

    override fun getCharset(file: VirtualFile, content: ByteArray): String = Charsets.UTF_8.name()

    companion object {
        const val NAME: String = "QSS Palette"
        const val DESCRIPTION: String = "QSS Toolkit color palette"
    }
}
