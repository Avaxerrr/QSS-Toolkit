package io.github.avaxerrr.qsstoolkit.palette

import java.io.Serializable

class QssColorPalette(val name: String) : Serializable {
    private val colors = mutableListOf<QssColor>()

    fun addColor(color: QssColor) {
        colors.add(color)
    }

    fun removeColor(color: QssColor) {
        colors.remove(color)
    }

    fun getAllColors(): List<QssColor> = colors.toList()
}
