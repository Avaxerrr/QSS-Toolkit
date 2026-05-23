package io.github.avaxerrr.qsstoolkit

import io.github.avaxerrr.qsstoolkit.palette.QssColorPaletteManager
import io.github.avaxerrr.qsstoolkit.palette.QssColor
import io.github.avaxerrr.qsstoolkit.palette.QssColorFormat
import io.github.avaxerrr.qsstoolkit.palette.QssColorFormats
import java.awt.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QssColorPaletteManagerTest {
    @Test
    fun `formats colors for QSS copy actions`() {
        val opaque = QssColor("Accent", Color(0x33, 0x66, 0x99))
        val transparent = QssColor("Overlay", Color(0x33, 0x66, 0x99, 128))

        assertEquals("#336699", opaque.toHex())
        assertEquals("#336699", opaque.toQssFormat())
        assertEquals("rgb(51, 102, 153)", opaque.toRgb())
        assertEquals("rgba(51, 102, 153, 100%)", opaque.toRgba())
        assertEquals("hsl(210, 50%, 40%)", opaque.toHsl())
        assertEquals("hsla(210, 50%, 40%, 100%)", opaque.toHsla())
        assertEquals("hsv(210, 67%, 60%)", opaque.toHsv())
        assertEquals("hsva(210, 67%, 60%, 100%)", opaque.toHsva())
        assertEquals("rgba(51, 102, 153, 50%)", transparent.toQssFormat())
        assertEquals("rgba(51, 102, 153, 50%)", transparent.toRgba())
        assertEquals("hsla(210, 50%, 40%, 50%)", transparent.toHsla())
        assertEquals("hsva(210, 67%, 60%, 50%)", transparent.toHsva())
    }

    @Test
    fun `parses concrete Qt color formats`() {
        assertEquals(Color(0x33, 0x66, 0x99), QssColorFormats.parseConcreteColor("#336699"))
        assertEquals(Color(0x33, 0x66, 0x99, 0x80), QssColorFormats.parseConcreteColor("#33669980"))
        assertEquals(Color(0x33, 0x66, 0x99), QssColorFormats.parseConcreteColor("rgb(51, 102, 153)"))
        assertEquals(Color(0x33, 0x66, 0x99), QssColorFormats.parseConcreteColor("rgb(20%, 40%, 60%)"))
        assertEquals(Color(0x33, 0x66, 0x99, 128), QssColorFormats.parseConcreteColor("rgba(51, 102, 153, 50%)"))
        assertEquals(Color(0x33, 0x66, 0x99, 128), QssColorFormats.parseConcreteColor("rgba(51, 102, 153, 0.5)"))
        assertEquals(Color(255, 255, 0), QssColorFormats.parseConcreteColor("hsl(60, 100%, 50%)"))
        assertEquals(Color(0, 0, 255, 191), QssColorFormats.parseConcreteColor("hsva(240, 255, 255, 75%)"))
        assertEquals(null, QssColorFormats.parseConcreteColor("palette(WindowText)"))
    }

    @Test
    fun `uses explicit color formats for insert and copy menus`() {
        val opaque = Color(0x33, 0x66, 0x99)
        val transparent = Color(0x33, 0x66, 0x99, 128)

        assertEquals(
            listOf(QssColorFormat.HEX, QssColorFormat.RGB, QssColorFormat.HSL, QssColorFormat.HSV),
            QssColorFormats.explicitFormatsFor(opaque)
        )
        assertEquals(
            listOf(
                QssColorFormat.HEX,
                QssColorFormat.RGB,
                QssColorFormat.HSL,
                QssColorFormat.HSV,
                QssColorFormat.RGBA,
                QssColorFormat.HSLA,
                QssColorFormat.HSVA
            ),
            QssColorFormats.explicitFormatsFor(transparent)
        )
    }

    @Test
    fun `finds concrete colors inside selected editor text`() {
        assertEquals(Color(0xFF, 0x98, 0xAC), QssColorFormats.findConcreteColor("color: #FF98AC;"))
        assertEquals(
            Color(255, 193, 7, 77),
            QssColorFormats.findConcreteColor("background: rgba(255, 193, 7, 0.3);")
        )
        assertEquals(Color(0, 0, 0, 0), QssColorFormats.findConcreteColor("border-color: transparent;"))
        assertEquals(null, QssColorFormats.findConcreteColor("border-color: palette(WindowText);"))
    }

    @Test
    fun `creates palettes with automatic names`() {
        val manager = QssColorPaletteManager()

        val first = manager.createPalette()
        val second = manager.createPalette()

        assertEquals("Folder 1", first.name)
        assertEquals("Folder 2", second.name)

        manager.removePalette(first)
        val replacement = manager.createPalette()

        assertEquals("Folder 1", replacement.name)
    }

    @Test
    fun `renames palettes with trimmed unique names`() {
        val manager = QssColorPaletteManager()
        val first = manager.createPalette("Theme")
        val second = manager.createPalette("Accent")

        assertTrue(manager.renamePalette(second, "  Theme  "))

        assertEquals("Theme", first.name)
        assertEquals("Theme 2", second.name)
        assertFalse(manager.renamePalette(second, "   "))
        assertEquals("Theme 2", second.name)
    }

    @Test
    fun `creates colors with automatic names from their values`() {
        val manager = QssColorPaletteManager()
        val palette = manager.createPalette()

        val first = manager.addColor(palette, Color(0x33, 0x66, 0x99))
        val second = manager.addColor(palette, Color(0x33, 0x66, 0x99))

        assertEquals("#336699", first.name)
        assertEquals("#336699 2", second.name)
    }

    @Test
    fun `updates color values in place`() {
        val manager = QssColorPaletteManager()
        val palette = manager.createPalette()
        val color = manager.addColor(palette, Color.RED, "Accent")

        assertTrue(manager.updateColor(palette, color, Color.BLUE))

        assertEquals(Color.BLUE, color.value)
        assertEquals("Accent", color.name)
    }

    @Test
    fun `renames colors with trimmed unique names`() {
        val manager = QssColorPaletteManager()
        val palette = manager.createPalette()
        val first = manager.addColor(palette, Color.RED, "Accent")
        val second = manager.addColor(palette, Color.BLUE, "Secondary")

        assertTrue(manager.renameColor(palette, second, "  Accent  "))

        assertEquals("Accent", first.name)
        assertEquals("Accent 2", second.name)
        assertFalse(manager.renameColor(palette, second, "   "))
        assertEquals("Accent 2", second.name)
    }

    @Test
    fun `reorders palettes using tree insertion indexes`() {
        val manager = QssColorPaletteManager()
        val first = manager.createPalette("First")
        val second = manager.createPalette("Second")
        val third = manager.createPalette("Third")

        assertTrue(manager.movePalette(first, 3))
        assertEquals(listOf(second, third, first), manager.getAllPalettes())

        assertTrue(manager.movePalette(first, 0))
        assertEquals(listOf(first, second, third), manager.getAllPalettes())
    }

    @Test
    fun `moves colors within and between palettes`() {
        val manager = QssColorPaletteManager()
        val source = manager.createPalette("Source")
        val target = manager.createPalette("Target")
        val first = manager.addColor(source, Color.RED, "First")
        val second = manager.addColor(source, Color.GREEN, "Second")
        val third = manager.addColor(source, Color.BLUE, "Third")

        assertTrue(manager.moveColor(source, first, source, 3))
        assertEquals(listOf(second, third, first), source.getAllColors())

        assertTrue(manager.moveColor(source, third, target, 0))
        assertEquals(listOf(second, first), source.getAllColors())
        assertEquals(listOf(third), target.getAllColors())
    }

    @Test
    fun `moves multiple selected colors together`() {
        val manager = QssColorPaletteManager()
        val source = manager.createPalette("Source")
        val target = manager.createPalette("Target")
        val first = manager.addColor(source, Color.RED, "First")
        val second = manager.addColor(source, Color.GREEN, "Second")
        val third = manager.addColor(source, Color.BLUE, "Third")
        val fourth = manager.addColor(source, Color.BLACK, "Fourth")

        assertTrue(manager.moveColors(source, listOf(second, fourth), target, 0))

        assertEquals(listOf(first, third), source.getAllColors())
        assertEquals(listOf(second, fourth), target.getAllColors())
    }

    @Test
    fun `reorders multiple selected colors together`() {
        val manager = QssColorPaletteManager()
        val source = manager.createPalette("Source")
        val first = manager.addColor(source, Color.RED, "First")
        val second = manager.addColor(source, Color.GREEN, "Second")
        val third = manager.addColor(source, Color.BLUE, "Third")
        val fourth = manager.addColor(source, Color.BLACK, "Fourth")

        assertTrue(manager.moveColors(source, listOf(second, third), source, 4))

        assertEquals(listOf(first, fourth, second, third), source.getAllColors())
    }

    @Test
    fun `moves duplicate-looking colors by identity`() {
        val manager = QssColorPaletteManager()
        val source = manager.createPalette("Source")
        val target = manager.createPalette("Target")
        val first = manager.addColor(source, Color.RED, "Accent")
        val duplicate = manager.addColor(source, Color.RED, "Accent")

        duplicate.name = first.name

        assertTrue(manager.moveColor(source, duplicate, target, 0))

        assertEquals(listOf(first), source.getAllColors())
        assertEquals(listOf(duplicate), target.getAllColors())
    }

    @Test
    fun `loads palettes and colors from persisted state`() {
        val manager = QssColorPaletteManager()
        val palette = manager.createPalette("Theme")
        manager.addColor(palette, Color(0x33, 0x66, 0x99, 128), "Accent")

        val restoredManager = QssColorPaletteManager()
        restoredManager.loadState(manager.state)

        val restoredPalette = restoredManager.getAllPalettes().single()
        val restoredColor = restoredPalette.getAllColors().single()

        assertEquals("Theme", restoredPalette.name)
        assertEquals("Accent", restoredColor.name)
        assertEquals("rgba(51, 102, 153, 50%)", restoredColor.toQssFormat())
    }

    @Test
    fun `notifies listeners when palettes change`() {
        val manager = QssColorPaletteManager()
        var changeCount = 0
        val subscription = manager.addChangeListener { changeCount++ }

        val palette = manager.createPalette()
        val color = manager.addColor(palette, Color.RED)
        manager.updateColor(palette, color, Color.BLUE)

        assertEquals(3, changeCount)
        subscription.close()
        manager.addColor(palette, Color.GREEN)
        assertEquals(3, changeCount)
    }
}
