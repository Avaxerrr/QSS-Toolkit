package io.github.avaxerrr.qsstoolkit.palette

import java.awt.Color
import java.io.Serializable

data class QssColor(
    val name: String,
    val value: Color
) : Serializable {

    /**
     * Converts color to QSS-compatible format.
     * Uses rgba() if color has transparency, otherwise hex.
     */
    fun toQssFormat(): String {
        return if (value.alpha < 255) {
            // Use rgba() for transparent colors (Qt compatible)
            val alpha = value.alpha / 255.0f
            String.format("rgba(%d, %d, %d, %.2f)", value.red, value.green, value.blue, alpha)
        } else {
            // Use hex for opaque colors
            String.format("#%02X%02X%02X", value.red, value.green, value.blue)
        }
    }

    /**
     * Legacy method - always returns 6-digit hex (no alpha).
     * Kept for backwards compatibility.
     */
    fun toHex(): String {
        return String.format("#%02X%02X%02X", value.red, value.green, value.blue)
    }

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
