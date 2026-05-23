package io.github.avaxerrr.qsstoolkit

import io.github.avaxerrr.qsstoolkit.palette.QssColorPaletteManager
import io.github.avaxerrr.qsstoolkit.palette.QssColor
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
        assertEquals("rgba(51, 102, 153, 1.00)", opaque.toRgba())
        assertEquals("rgba(51, 102, 153, 0.50)", transparent.toQssFormat())
        assertEquals("rgba(51, 102, 153, 0.50)", transparent.toRgba())
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
        manager.addColor(palette, Color(0x33, 0x66, 0x99), "Accent")

        val restoredManager = QssColorPaletteManager()
        restoredManager.loadState(manager.state)

        val restoredPalette = restoredManager.getAllPalettes().single()
        val restoredColor = restoredPalette.getAllColors().single()

        assertEquals("Theme", restoredPalette.name)
        assertEquals("Accent", restoredColor.name)
        assertEquals("#336699", restoredColor.toHex())
    }
}
