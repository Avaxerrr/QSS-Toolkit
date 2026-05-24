package io.github.avaxerrr.qsstoolkit.palette

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser

data class QssColorPaletteImportResult(
    val importedPalettes: Int,
    val importedColors: Int,
    val skippedColors: List<QssColorPaletteImportSkippedColor>
)

data class QssColorPaletteImportSkippedColor(
    val paletteName: String,
    val colorName: String?,
    val value: String?,
    val reason: String
)

class QssColorPaletteFileException(message: String) : RuntimeException(message)

object QssColorPaletteFileFormat {
    const val FILE_EXTENSION = "qsspalette"

    private val IMPORT_EXTENSIONS = setOf(FILE_EXTENSION, "json")
    private const val SCHEMA = "qss-toolkit.palette"
    private const val VERSION = 1

    private val gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()

    fun isSupportedImportFileName(fileName: String): Boolean {
        return fileName.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase() in IMPORT_EXTENSIONS
    }

    fun exportPalettes(palettes: List<QssColorPalette>): String {
        val root = JsonObject()
        root.addProperty("schema", SCHEMA)
        root.addProperty("version", VERSION)
        root.add("palettes", JsonArray().apply {
            for (palette in palettes) {
                add(palette.toJsonObject())
            }
        })
        return gson.toJson(root)
    }

    fun importInto(
        paletteManager: QssColorPaletteManager,
        text: String
    ): QssColorPaletteImportResult {
        val palettes = parsePalettes(text)
        var importedColors = 0
        val skippedColors = mutableListOf<QssColorPaletteImportSkippedColor>()

        for (paletteData in palettes) {
            val palette = paletteManager.createPalette(paletteData.name)

            for (colorData in paletteData.colors) {
                val value = colorData.value
                val color = value?.let { QssColorFormats.parseConcreteColor(it) }

                if (value == null || color == null) {
                    skippedColors.add(
                        QssColorPaletteImportSkippedColor(
                            paletteName = paletteData.name,
                            colorName = colorData.name,
                            value = value,
                            reason = "Unsupported or missing color value"
                        )
                    )
                    continue
                }

                paletteManager.addColor(palette, color, colorData.name)
                importedColors++
            }
        }

        return QssColorPaletteImportResult(
            importedPalettes = palettes.size,
            importedColors = importedColors,
            skippedColors = skippedColors
        )
    }

    private fun QssColorPalette.toJsonObject(): JsonObject {
        return JsonObject().apply {
            addProperty("name", name)
            add("colors", JsonArray().apply {
                for (color in getAllColors()) {
                    add(JsonObject().apply {
                        addProperty("name", color.name)
                        addProperty("value", color.toStorageHex())
                    })
                }
            })
        }
    }

    private fun parsePalettes(text: String): List<PaletteData> {
        val root = parseRootObject(text)
        validateSchema(root)
        validateVersion(root)

        val palettesElement = root.get("palettes")
        if (palettesElement == null || !palettesElement.isJsonArray) {
            throw QssColorPaletteFileException("Palette file must contain a palettes array.")
        }

        return palettesElement.asJsonArray.mapIndexed { index, element ->
            parsePalette(element, index)
        }
    }

    private fun parseRootObject(text: String): JsonObject {
        val element = try {
            JsonParser.parseString(text)
        } catch (_: JsonParseException) {
            throw QssColorPaletteFileException("Palette file is not valid JSON.")
        } catch (_: IllegalStateException) {
            throw QssColorPaletteFileException("Palette file is not valid JSON.")
        }

        if (!element.isJsonObject) {
            throw QssColorPaletteFileException("Palette file must contain a JSON object.")
        }

        return element.asJsonObject
    }

    private fun validateSchema(root: JsonObject) {
        val schema = root.optionalString("schema") ?: return
        if (schema != SCHEMA) {
            throw QssColorPaletteFileException("Palette file schema is not supported.")
        }
    }

    private fun validateVersion(root: JsonObject) {
        val versionElement = root.get("version")
        if (versionElement == null || !versionElement.isJsonPrimitive || !versionElement.asJsonPrimitive.isNumber) {
            throw QssColorPaletteFileException("Palette file must contain version $VERSION.")
        }

        val version = try {
            versionElement.asInt
        } catch (_: NumberFormatException) {
            throw QssColorPaletteFileException("Palette file version is not supported.")
        }

        if (version != VERSION) {
            throw QssColorPaletteFileException("Palette file version is not supported.")
        }
    }

    private fun parsePalette(element: JsonElement, index: Int): PaletteData {
        if (!element.isJsonObject) {
            throw QssColorPaletteFileException("Palette entry ${index + 1} must be an object.")
        }

        val paletteObject = element.asJsonObject
        val name = paletteObject.optionalString("name")?.trim()?.takeIf { it.isNotEmpty() }
            ?: "Imported Folder"
        val colors = parseColors(paletteObject, name)
        return PaletteData(name, colors)
    }

    private fun parseColors(paletteObject: JsonObject, paletteName: String): List<ColorData> {
        val colorsElement = paletteObject.get("colors") ?: return emptyList()
        if (!colorsElement.isJsonArray) {
            throw QssColorPaletteFileException("Colors for '$paletteName' must be an array.")
        }

        return colorsElement.asJsonArray.mapIndexed { index, element ->
            if (!element.isJsonObject) {
                throw QssColorPaletteFileException("Color entry ${index + 1} in '$paletteName' must be an object.")
            }

            val colorObject = element.asJsonObject
            ColorData(
                name = colorObject.optionalString("name")?.trim()?.takeIf { it.isNotEmpty() },
                value = colorObject.optionalString("value")?.trim()?.takeIf { it.isNotEmpty() }
            )
        }
    }

    private fun JsonObject.optionalString(name: String): String? {
        val element = get(name) ?: return null
        if (!element.isJsonPrimitive || !element.asJsonPrimitive.isString) return null
        return element.asString
    }

    private data class PaletteData(
        val name: String,
        val colors: List<ColorData>
    )

    private data class ColorData(
        val name: String?,
        val value: String?
    )
}
