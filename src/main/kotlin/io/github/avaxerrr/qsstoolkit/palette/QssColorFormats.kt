package io.github.avaxerrr.qsstoolkit.palette

import java.awt.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class QssColorFormat(val label: String) {
    AUTO("Value"),
    HEX("Hex"),
    RGB("RGB"),
    RGBA("RGBA"),
    HSL("HSL"),
    HSLA("HSLA"),
    HSV("HSV"),
    HSVA("HSVA")
}

object QssColorFormats {
    fun explicitFormatsFor(color: Color): List<QssColorFormat> {
        return buildList {
            add(QssColorFormat.HEX)
            add(QssColorFormat.RGB)
            add(QssColorFormat.HSL)
            add(QssColorFormat.HSV)

            if (color.alpha < 255) {
                add(QssColorFormat.RGBA)
                add(QssColorFormat.HSLA)
                add(QssColorFormat.HSVA)
            }
        }
    }

    fun format(color: Color, format: QssColorFormat): String {
        return when (format) {
            QssColorFormat.AUTO -> if (color.alpha < 255) toRgba(color) else toHex(color)
            QssColorFormat.HEX -> toHex(color)
            QssColorFormat.RGB -> toRgb(color)
            QssColorFormat.RGBA -> toRgba(color)
            QssColorFormat.HSL -> toHsl(color)
            QssColorFormat.HSLA -> toHsla(color)
            QssColorFormat.HSV -> toHsv(color)
            QssColorFormat.HSVA -> toHsva(color)
        }
    }

    fun toHex(color: Color): String {
        return String.format("#%02X%02X%02X", color.red, color.green, color.blue)
    }

    fun toStorageHex(color: Color): String {
        return if (color.alpha < 255) {
            String.format("#%02X%02X%02X%02X", color.red, color.green, color.blue, color.alpha)
        } else {
            toHex(color)
        }
    }

    fun toRgb(color: Color): String {
        return "rgb(${color.red}, ${color.green}, ${color.blue})"
    }

    fun toRgba(color: Color): String {
        return "rgba(${color.red}, ${color.green}, ${color.blue}, ${alphaPercent(color)}%)"
    }

    fun toHsl(color: Color): String {
        val hsl = rgbToHsl(color)
        return "hsl(${hsl.hue}, ${hsl.saturationPercent}%, ${hsl.lightnessPercent}%)"
    }

    fun toHsla(color: Color): String {
        val hsl = rgbToHsl(color)
        return "hsla(${hsl.hue}, ${hsl.saturationPercent}%, ${hsl.lightnessPercent}%, ${alphaPercent(color)}%)"
    }

    fun toHsv(color: Color): String {
        val hsv = rgbToHsv(color)
        return "hsv(${hsv.hue}, ${hsv.saturationPercent}%, ${hsv.valuePercent}%)"
    }

    fun toHsva(color: Color): String {
        val hsv = rgbToHsv(color)
        return "hsva(${hsv.hue}, ${hsv.saturationPercent}%, ${hsv.valuePercent}%, ${alphaPercent(color)}%)"
    }

    fun parseConcreteColor(text: String): Color? {
        val trimmed = text.trim()
        return parseHex(trimmed)
            ?: parseFunction(trimmed)
            ?: parseTransparent(trimmed)
    }

    fun findConcreteColor(text: String): Color? {
        parseConcreteColor(text)?.let { return it }

        return CONCRETE_COLOR_PATTERNS
            .asSequence()
            .flatMap { pattern -> pattern.findAll(text).map { it.value } }
            .mapNotNull { parseConcreteColor(it) }
            .firstOrNull()
    }

    private fun parseHex(text: String): Color? {
        val match = Regex("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$").matchEntire(text)
            ?: return null
        val hex = match.groupValues[1]

        return try {
            when (hex.length) {
                3 -> {
                    val r = hex.substring(0, 1).repeat(2).toInt(16)
                    val g = hex.substring(1, 2).repeat(2).toInt(16)
                    val b = hex.substring(2, 3).repeat(2).toInt(16)
                    Color(r, g, b)
                }
                6 -> {
                    val r = hex.substring(0, 2).toInt(16)
                    val g = hex.substring(2, 4).toInt(16)
                    val b = hex.substring(4, 6).toInt(16)
                    Color(r, g, b)
                }
                8 -> {
                    val r = hex.substring(0, 2).toInt(16)
                    val g = hex.substring(2, 4).toInt(16)
                    val b = hex.substring(4, 6).toInt(16)
                    val a = hex.substring(6, 8).toInt(16)
                    Color(r, g, b, a)
                }
                else -> null
            }
        } catch (_: NumberFormatException) {
            null
        }
    }

    private fun parseFunction(text: String): Color? {
        val match = Regex("^([a-zA-Z]+)\\s*\\((.*)\\)$").matchEntire(text) ?: return null
        val functionName = match.groupValues[1].lowercase()
        val args = match.groupValues[2].split(",").map { it.trim() }

        return when (functionName) {
            "rgb" -> parseRgb(args)
            "rgba" -> parseRgba(args)
            "hsl" -> parseHsl(args)
            "hsla" -> parseHsla(args)
            "hsv" -> parseHsv(args)
            "hsva" -> parseHsva(args)
            else -> null
        }
    }

    private fun parseRgb(args: List<String>): Color? {
        if (args.size != 3) return null

        val red = parseRgbComponent(args[0]) ?: return null
        val green = parseRgbComponent(args[1]) ?: return null
        val blue = parseRgbComponent(args[2]) ?: return null
        return Color(red, green, blue)
    }

    private fun parseRgba(args: List<String>): Color? {
        if (args.size != 4) return null

        val red = parseRgbComponent(args[0]) ?: return null
        val green = parseRgbComponent(args[1]) ?: return null
        val blue = parseRgbComponent(args[2]) ?: return null
        val alpha = parseAlphaComponent(args[3]) ?: return null
        return Color(red, green, blue, alpha)
    }

    private fun parseHsl(args: List<String>): Color? {
        if (args.size != 3) return null

        val hue = parseHue(args[0]) ?: return null
        val saturation = parsePercentOrByte(args[1]) ?: return null
        val lightness = parsePercentOrByte(args[2]) ?: return null
        return hslToColor(hue, saturation, lightness, 255)
    }

    private fun parseHsla(args: List<String>): Color? {
        if (args.size != 4) return null

        val hue = parseHue(args[0]) ?: return null
        val saturation = parsePercentOrByte(args[1]) ?: return null
        val lightness = parsePercentOrByte(args[2]) ?: return null
        val alpha = parseAlphaComponent(args[3]) ?: return null
        return hslToColor(hue, saturation, lightness, alpha)
    }

    private fun parseHsv(args: List<String>): Color? {
        if (args.size != 3) return null

        val hue = parseHue(args[0]) ?: return null
        val saturation = parsePercentOrByte(args[1]) ?: return null
        val value = parsePercentOrByte(args[2]) ?: return null
        return hsvToColor(hue, saturation, value, 255)
    }

    private fun parseHsva(args: List<String>): Color? {
        if (args.size != 4) return null

        val hue = parseHue(args[0]) ?: return null
        val saturation = parsePercentOrByte(args[1]) ?: return null
        val value = parsePercentOrByte(args[2]) ?: return null
        val alpha = parseAlphaComponent(args[3]) ?: return null
        return hsvToColor(hue, saturation, value, alpha)
    }

    private fun parseTransparent(text: String): Color? {
        return if (text.equals("transparent", ignoreCase = true)) Color(0, 0, 0, 0) else null
    }

    private fun parseRgbComponent(text: String): Int? {
        if (text.endsWith("%")) {
            val percent = text.dropLast(1).toFloatOrNull() ?: return null
            if (percent !in 0f..100f) return null
            return (percent * 255f / 100f).roundToInt()
        }

        val value = text.toIntOrNull() ?: return null
        if (value !in 0..255) return null
        return value
    }

    private fun parseAlphaComponent(text: String): Int? {
        if (text.endsWith("%")) {
            val percent = text.dropLast(1).toFloatOrNull() ?: return null
            if (percent !in 0f..100f) return null
            return (percent * 255f / 100f).roundToInt()
        }

        val decimal = text.toFloatOrNull() ?: return null
        if (decimal in 0f..1f && text.contains(".")) {
            return (decimal * 255f).roundToInt()
        }

        val value = decimal.roundToInt()
        if (value.toFloat() != decimal || value !in 0..255) return null
        return value
    }

    private fun parseHue(text: String): Float? {
        val hue = text.toFloatOrNull() ?: return null
        if (hue !in 0f..359f) return null
        return hue
    }

    private fun parsePercentOrByte(text: String): Float? {
        if (text.endsWith("%")) {
            val percent = text.dropLast(1).toFloatOrNull() ?: return null
            if (percent !in 0f..100f) return null
            return percent / 100f
        }

        val value = text.toFloatOrNull() ?: return null
        if (value !in 0f..255f) return null
        return value / 255f
    }

    private fun hsvToColor(hue: Float, saturation: Float, value: Float, alpha: Int): Color {
        val rgb = Color.HSBtoRGB(hue / 360f, saturation, value)
        return Color(Color(rgb).red, Color(rgb).green, Color(rgb).blue, alpha)
    }

    private fun hslToColor(hue: Float, saturation: Float, lightness: Float, alpha: Int): Color {
        val chroma = (1f - abs(2f * lightness - 1f)) * saturation
        val huePrime = hue / 60f
        val x = chroma * (1f - abs(huePrime % 2f - 1f))

        val (r1, g1, b1) = when {
            huePrime < 1f -> Triple(chroma, x, 0f)
            huePrime < 2f -> Triple(x, chroma, 0f)
            huePrime < 3f -> Triple(0f, chroma, x)
            huePrime < 4f -> Triple(0f, x, chroma)
            huePrime < 5f -> Triple(x, 0f, chroma)
            else -> Triple(chroma, 0f, x)
        }

        val m = lightness - chroma / 2f
        val red = ((r1 + m) * 255f).roundToInt().coerceIn(0, 255)
        val green = ((g1 + m) * 255f).roundToInt().coerceIn(0, 255)
        val blue = ((b1 + m) * 255f).roundToInt().coerceIn(0, 255)
        return Color(red, green, blue, alpha)
    }

    private fun rgbToHsv(color: Color): Hsv {
        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        return Hsv(
            hue = normalizedHue((hsb[0] * 360f).roundToInt()),
            saturationPercent = (hsb[1] * 100f).roundToInt(),
            valuePercent = (hsb[2] * 100f).roundToInt()
        )
    }

    private fun rgbToHsl(color: Color): Hsl {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val maxValue = max(red, max(green, blue))
        val minValue = min(red, min(green, blue))
        val delta = maxValue - minValue
        val lightness = (maxValue + minValue) / 2f

        val hue = when {
            delta == 0f -> 0f
            maxValue == red -> 60f * (((green - blue) / delta) % 6f)
            maxValue == green -> 60f * (((blue - red) / delta) + 2f)
            else -> 60f * (((red - green) / delta) + 4f)
        }.let { if (it < 0f) it + 360f else it }

        val saturation = if (delta == 0f) {
            0f
        } else {
            delta / (1f - abs(2f * lightness - 1f))
        }

        return Hsl(
            hue = normalizedHue(hue.roundToInt()),
            saturationPercent = (saturation * 100f).roundToInt(),
            lightnessPercent = (lightness * 100f).roundToInt()
        )
    }

    private fun alphaPercent(color: Color): Int {
        return (color.alpha * 100f / 255f).roundToInt().coerceIn(0, 100)
    }

    private fun normalizedHue(hue: Int): Int {
        return if (hue >= 360) 0 else hue.coerceIn(0, 359)
    }

    private data class Hsl(
        val hue: Int,
        val saturationPercent: Int,
        val lightnessPercent: Int
    )

    private data class Hsv(
        val hue: Int,
        val saturationPercent: Int,
        val valuePercent: Int
    )

    private val CONCRETE_COLOR_PATTERNS = listOf(
        Regex("#(?:[0-9a-fA-F]{8}|[0-9a-fA-F]{6}|[0-9a-fA-F]{3})\\b"),
        Regex("\\b(?:rgb|rgba|hsl|hsla|hsv|hsva)\\s*\\([^)]*\\)", RegexOption.IGNORE_CASE),
        Regex("\\btransparent\\b", RegexOption.IGNORE_CASE)
    )
}
