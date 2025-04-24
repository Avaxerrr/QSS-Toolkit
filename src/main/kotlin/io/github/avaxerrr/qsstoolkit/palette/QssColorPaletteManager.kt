// QSS Toolkit version 1.1

package io.github.avaxerrr.qsstoolkit.palette

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

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

    fun addPalette(palette: QssColorPalette) {
        palettes.add(palette)
        updateState()
    }

    fun removePalette(palette: QssColorPalette) {
        palettes.remove(palette)
        updateState()
    }

    fun getAllPalettes(): List<QssColorPalette> = palettes.toList()

    fun getPalette(name: String): QssColorPalette? {
        return palettes.find { it.name == name }
    }

    fun updateState() {
        state.palettes.clear()

        for (palette in palettes) {
            val serPalette = SerializablePalette()
            serPalette.name = palette.name

            for (color in palette.getAllColors()) {
                val serColor = SerializableColor()
                serColor.name = color.name
                serColor.hexValue = color.toHex()
                serPalette.colors.add(serColor)
            }

            state.palettes.add(serPalette)
        }
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

    companion object {
        fun getInstance(project: Project): QssColorPaletteManager {
            return project.getService(QssColorPaletteManager::class.java)
        }
    }
}
