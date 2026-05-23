package io.github.avaxerrr.qsstoolkit.palette

import java.io.Serializable

class QssColorPalette(var name: String) : Serializable {
    private val colors = mutableListOf<QssColor>()

    fun addColor(color: QssColor) {
        colors.add(color)
    }

    fun addColor(index: Int, color: QssColor) {
        colors.add(index.coerceIn(0, colors.size), color)
    }

    fun removeColor(color: QssColor): Boolean {
        val index = indexOfColor(color)
        if (index < 0) return false

        colors.removeAt(index)
        return true
    }

    fun removeColorAt(index: Int): QssColor {
        return colors.removeAt(index)
    }

    fun indexOfColor(color: QssColor): Int {
        return colors.indexOfFirst { it === color }
    }

    fun getAllColors(): List<QssColor> = colors.toList()

    fun replaceColors(newColors: List<QssColor>) {
        colors.clear()
        colors.addAll(newColors)
    }

    override fun toString(): String = name
}
