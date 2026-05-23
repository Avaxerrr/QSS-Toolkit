package io.github.avaxerrr.qsstoolkit.palette

import java.awt.Color
import java.io.Serializable
import java.util.Locale

data class QssColor(
    var name: String,
    val value: Color
) : Serializable {

    /**
     * Converts color to QSS-compatible format.
     * Uses rgba() if color has transparency, otherwise hex.
     */
    fun toQssFormat(): String {
        return if (value.alpha < 255) {
            // Use rgba() for transparent colors (Qt compatible)
            toRgba()
        } else {
            // Use hex for opaque colors
            toHex()
        }
    }

    /**
     * Legacy method - always returns 6-digit hex (no alpha).
     * Kept for backwards compatibility.
     */
    fun toHex(): String {
        return String.format("#%02X%02X%02X", value.red, value.green, value.blue)
    }

    fun toRgb(): String {
        return "rgb(${value.red}, ${value.green}, ${value.blue})"
    }

    fun toRgba(): String {
        val alpha = value.alpha / 255.0f
        return String.format(Locale.US, "rgba(%d, %d, %d, %.2f)", value.red, value.green, value.blue, alpha)
    }

    override fun toString(): String = name

    companion object {
        fun fromHex(name: String, hex: String): QssColor? {
            return try {
                val color = Color.decode(hex)
                QssColor(name, color)
            } catch (e: Exception) {
                null
            }
        }
    }
}
