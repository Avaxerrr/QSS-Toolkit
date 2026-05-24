package io.github.avaxerrr.qsstoolkit.palette

data class QssColorPaletteFilterResult(
    val palette: QssColorPalette,
    val colors: List<QssColor>
)

object QssColorPaletteFilter {
    fun filter(
        palettes: List<QssColorPalette>,
        query: String
    ): List<QssColorPaletteFilterResult> {
        val terms = query.toTerms()
        if (terms.isEmpty()) {
            return palettes.map { palette ->
                QssColorPaletteFilterResult(palette, palette.getAllColors())
            }
        }

        return palettes.mapNotNull { palette ->
            val colors = palette.getAllColors()
            val paletteMatches = palette.name.matchesAll(terms)
            val visibleColors = if (paletteMatches) {
                colors
            } else {
                colors.filter { color -> color.matchesAll(terms) }
            }

            if (visibleColors.isEmpty() && !paletteMatches) {
                null
            } else {
                QssColorPaletteFilterResult(palette, visibleColors)
            }
        }
    }

    private fun String.toTerms(): List<String> {
        return trim()
            .lowercase()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
    }

    private fun String.matchesAll(terms: List<String>): Boolean {
        val searchableText = lowercase()
        return terms.all { term -> term in searchableText }
    }

    private fun QssColor.matchesAll(terms: List<String>): Boolean {
        val searchableText = listOf(
            name,
            toQssFormat(),
            toStorageHex(),
            toHex(),
            toRgb(),
            toRgba(),
            toHsl(),
            toHsla(),
            toHsv(),
            toHsva()
        )
            .distinct()
            .joinToString(separator = "\n")
            .lowercase()

        return terms.all { term -> term in searchableText }
    }
}
