package io.github.avaxerrr.qsstoolkit.palette

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import java.awt.Color

@Service(Service.Level.PROJECT)
@State(
    name = "QssColorPaletteManager",
    storages = [Storage("qssColorPalettes.xml")]
)
class QssColorPaletteManager : PersistentStateComponent<QssColorPaletteManager.State> {

    class State {
        var palettes: MutableList<SerializablePalette> = mutableListOf()
    }

    class SerializablePalette {
        var name: String = ""
        var colors: MutableList<SerializableColor> = mutableListOf()
    }

    class SerializableColor {
        var name: String = ""
        var hexValue: String = ""
    }

    private val palettes = mutableListOf<QssColorPalette>()
    private val state = State()
    private val changeListeners = mutableListOf<() -> Unit>()

    fun addChangeListener(listener: () -> Unit): AutoCloseable {
        changeListeners.add(listener)
        return AutoCloseable {
            changeListeners.remove(listener)
        }
    }

    fun createPalette(requestedName: String? = null): QssColorPalette {
        val paletteName = requestedName.toCleanName()
            ?.let { uniqueName(it, palettes.map { palette -> palette.name }) }
            ?: nextPaletteName()
        val palette = QssColorPalette(paletteName)
        addPalette(palette)
        return palette
    }

    fun addPalette(palette: QssColorPalette) {
        palettes.add(palette)
        updateState()
    }

    fun removePalette(palette: QssColorPalette) {
        palettes.remove(palette)
        updateState()
    }

    fun removePalettes(palettesToRemove: List<QssColorPalette>): Int {
        val selectedPalettes = palettesToRemove.toPaletteIdentitySet()
        val removedCount = palettes.count { it in selectedPalettes }
        if (removedCount == 0) return 0

        palettes.removeAll { it in selectedPalettes }
        updateState()
        return removedCount
    }

    fun getAllPalettes(): List<QssColorPalette> = palettes.toList()

    fun getPalette(name: String): QssColorPalette? {
        return palettes.find { it.name == name }
    }

    fun renamePalette(palette: QssColorPalette, requestedName: String): Boolean {
        val cleanName = requestedName.toCleanName() ?: return false
        val existingNames = palettes
            .asSequence()
            .filter { it !== palette }
            .map { it.name }
            .toList()
        palette.name = uniqueName(cleanName, existingNames)
        updateState()
        return true
    }

    fun addColor(palette: QssColorPalette, value: Color, requestedName: String? = null): QssColor {
        val color = QssColor(
            name = colorNameFor(palette, value, requestedName),
            value = value
        )
        palette.addColor(color)
        updateState()
        return color
    }

    fun renameColor(palette: QssColorPalette, color: QssColor, requestedName: String): Boolean {
        if (palette.indexOfColor(color) < 0) return false

        val cleanName = requestedName.toCleanName() ?: return false
        val existingNames = palette.getAllColors()
            .asSequence()
            .filter { it !== color }
            .map { it.name }
            .toList()
        color.name = uniqueName(cleanName, existingNames)
        updateState()
        return true
    }

    fun updateColor(palette: QssColorPalette, color: QssColor, value: Color): Boolean {
        if (palette.indexOfColor(color) < 0) return false

        color.value = value
        updateState()
        return true
    }

    fun removeColor(palette: QssColorPalette, color: QssColor): Boolean {
        val removed = palette.removeColor(color)
        if (removed) {
            updateState()
        }
        return removed
    }

    fun removeColors(selections: List<ColorSelection>): Int {
        val groupedSelections = selections.groupBy { it.palette }
        var removedCount = 0

        for ((palette, group) in groupedSelections) {
            for (selection in group) {
                if (palette.removeColor(selection.color)) {
                    removedCount++
                }
            }
        }

        if (removedCount > 0) {
            updateState()
        }

        return removedCount
    }

    fun movePalette(palette: QssColorPalette, targetIndex: Int): Boolean {
        val sourceIndex = palettes.indexOf(palette)
        if (sourceIndex < 0) return false

        val insertionIndex = adjustedInsertionIndex(
            sourceIndex = sourceIndex,
            targetIndex = targetIndex,
            size = palettes.size
        )
        if (sourceIndex == insertionIndex) return false

        palettes.removeAt(sourceIndex)
        palettes.add(insertionIndex, palette)
        updateState()
        return true
    }

    fun moveColor(
        sourcePalette: QssColorPalette,
        color: QssColor,
        targetPalette: QssColorPalette,
        targetIndex: Int
    ): Boolean {
        val sourceIndex = sourcePalette.indexOfColor(color)
        if (sourceIndex < 0) return false

        if (sourcePalette === targetPalette) {
            val insertionIndex = adjustedInsertionIndex(
                sourceIndex = sourceIndex,
                targetIndex = targetIndex,
                size = sourcePalette.getAllColors().size
            )
            if (sourceIndex == insertionIndex) return false

            sourcePalette.removeColorAt(sourceIndex)
            sourcePalette.addColor(insertionIndex, color)
        } else {
            sourcePalette.removeColorAt(sourceIndex)
            targetPalette.addColor(targetIndex, color)
        }

        updateState()
        return true
    }

    fun moveColors(
        sourcePalette: QssColorPalette,
        colors: List<QssColor>,
        targetPalette: QssColorPalette,
        targetIndex: Int
    ): Boolean {
        if (colors.isEmpty()) return false

        val selectedColors = colors.toIdentitySet()
        val sourceColors = sourcePalette.getAllColors()
        val movingColors = sourceColors.filter { it in selectedColors }
        if (movingColors.isEmpty()) return false

        if (sourcePalette === targetPalette) {
            val boundedTargetIndex = targetIndex.coerceIn(0, sourceColors.size)
            val selectedBeforeTarget = sourceColors
                .take(boundedTargetIndex)
                .count { it in selectedColors }
            val insertionIndex = boundedTargetIndex - selectedBeforeTarget
            val remainingColors = sourceColors.filterNot { it in selectedColors }
            val newColors = remainingColors.toMutableList()
            newColors.addAll(insertionIndex.coerceIn(0, newColors.size), movingColors)
            if (newColors.sameIdentityOrder(sourceColors)) return false

            sourcePalette.replaceColors(newColors)
        } else {
            val targetColors = targetPalette.getAllColors().toMutableList()
            val insertionIndex = targetIndex.coerceIn(0, targetColors.size)
            sourcePalette.replaceColors(sourceColors.filterNot { it in selectedColors })
            targetColors.addAll(insertionIndex, movingColors)
            targetPalette.replaceColors(targetColors)
        }

        updateState()
        return true
    }

    private fun updateState() {
        state.palettes.clear()

        for (palette in palettes) {
            val serPalette = SerializablePalette()
            serPalette.name = palette.name

            for (color in palette.getAllColors()) {
                val serColor = SerializableColor()
                serColor.name = color.name
                serColor.hexValue = color.toStorageHex()
                serPalette.colors.add(serColor)
            }

            state.palettes.add(serPalette)
        }

        notifyChangeListeners()
    }

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
        palettes.clear()

        for (serPalette in state.palettes) {
            val palette = QssColorPalette(serPalette.name)

            for (serColor in serPalette.colors) {
                val color = QssColor.fromHex(serColor.name, serColor.hexValue)
                if (color != null) {
                    palette.addColor(color)
                }
            }

            palettes.add(palette)
        }
    }

    private fun nextPaletteName(): String {
        val existingNames = palettes.map { it.name }.toSet()
        var index = 1
        while (true) {
            val candidate = "Folder $index"
            if (candidate !in existingNames) {
                return candidate
            }
            index++
        }
    }

    private fun colorNameFor(palette: QssColorPalette, value: Color, requestedName: String?): String {
        val color = QssColor(name = "", value = value)
        val baseName = requestedName.toCleanName() ?: color.toQssFormat()
        return uniqueName(baseName, palette.getAllColors().map { it.name })
    }

    private fun adjustedInsertionIndex(sourceIndex: Int, targetIndex: Int, size: Int): Int {
        val boundedTarget = targetIndex.coerceIn(0, size)
        return if (sourceIndex < boundedTarget) boundedTarget - 1 else boundedTarget
    }

    private fun uniqueName(baseName: String, existingNames: Iterable<String>): String {
        val existing = existingNames.toSet()
        if (baseName !in existing) return baseName

        var suffix = 2
        while (true) {
            val candidate = "$baseName $suffix"
            if (candidate !in existing) {
                return candidate
            }
            suffix++
        }
    }

    private fun String?.toCleanName(): String? {
        return this?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun notifyChangeListeners() {
        for (listener in changeListeners.toList()) {
            listener()
        }
    }

    data class ColorSelection(
        val palette: QssColorPalette,
        val color: QssColor
    )

    companion object {
        fun getInstance(project: Project): QssColorPaletteManager {
            return project.getService(QssColorPaletteManager::class.java)
        }
    }
}

private fun List<QssColor>.toIdentitySet(): Set<QssColor> {
    return java.util.Collections.newSetFromMap(java.util.IdentityHashMap<QssColor, Boolean>()).apply {
        addAll(this@toIdentitySet)
    }
}

private fun List<QssColorPalette>.toPaletteIdentitySet(): Set<QssColorPalette> {
    return java.util.Collections.newSetFromMap(java.util.IdentityHashMap<QssColorPalette, Boolean>()).apply {
        addAll(this@toPaletteIdentitySet)
    }
}

private fun List<QssColor>.sameIdentityOrder(other: List<QssColor>): Boolean {
    if (size != other.size) return false
    return indices.all { this[it] === other[it] }
}
