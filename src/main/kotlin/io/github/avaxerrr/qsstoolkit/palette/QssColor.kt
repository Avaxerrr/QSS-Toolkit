package io.github.avaxerrr.qsstoolkit.palette

import java.awt.Color
import java.io.Serializable

data class QssColor(
    var name: String,
    var value: Color
) : Serializable {

    /**
     * Converts color to QSS-compatible format.
     * Uses rgba() if color has transparency, otherwise hex.
     */
    fun toQssFormat(): String {
        return QssColorFormats.format(value, QssColorFormat.AUTO)
    }

    /**
     * Legacy method - always returns 6-digit hex (no alpha).
     * Kept for backwards compatibility.
     */
    fun toHex(): String {
        return QssColorFormats.toHex(value)
    }

    fun toStorageHex(): String {
        return QssColorFormats.toStorageHex(value)
    }

    fun toRgb(): String {
        return QssColorFormats.toRgb(value)
    }

    fun toRgba(): String {
        return QssColorFormats.toRgba(value)
    }

    fun toHsl(): String {
        return QssColorFormats.toHsl(value)
    }

    fun toHsla(): String {
        return QssColorFormats.toHsla(value)
    }

    fun toHsv(): String {
        return QssColorFormats.toHsv(value)
    }

    fun toHsva(): String {
        return QssColorFormats.toHsva(value)
    }

    override fun toString(): String = name

    companion object {
        fun fromHex(name: String, hex: String): QssColor? {
            val color = QssColorFormats.parseConcreteColor(hex) ?: return null
            return QssColor(name, color)
        }
    }
}
