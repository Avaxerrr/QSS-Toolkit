// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.highlighting

import java.awt.Color

class ColorHistoryManager private constructor() {
    private val colorHistory = mutableListOf<Color>()
    private val maxHistorySize = 20

    fun addColor(color: Color) {
        if (colorHistory.contains(color)) {
            colorHistory.remove(color)
        }
        colorHistory.add(0, color)
        if (colorHistory.size > maxHistorySize) {
            colorHistory.removeAt(colorHistory.size - 1)
        }
    }

    fun getColors(): List<Color> = colorHistory.toList()

    companion object {
        private var instance: ColorHistoryManager? = null

        fun getInstance(): ColorHistoryManager {
            if (instance == null) {
                instance = ColorHistoryManager()
            }
            return instance!!
        }
    }
}
