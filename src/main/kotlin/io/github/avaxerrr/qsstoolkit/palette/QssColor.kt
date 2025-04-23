package io.github.avaxerrr.qsstoolkit.palette

import java.awt.Color
import java.io.Serializable

data class QssColor(
    val name: String,
    val value: Color
) : Serializable {
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
