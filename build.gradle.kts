import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.1.0"
  id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "io.github.avaxerrr"
version = "2.0"

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  // Coroutines support
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")

  intellijPlatform {
    intellijIdeaCommunity("2025.1")  // Keep only this one
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
  }
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "241"
      untilBuild = "253.*"
    }

    changeNotes = """
            <h3>Version 2.0 - December 2025</h3>
            <ul>
                <li><b>Complete Qt 6.10 Support:</b> All 96 properties and 50 widgets with full Qt 5/6 backward compatibility</li>
                <li><b>Advanced Selector Support:</b> 82 sub-controls (QScrollBar::handle) and 44 pseudo-states with intelligent chained syntax (QScrollBar::handle:vertical)</li>
                <li><b>Robust Error Detection:</b>
                    <ul>
                        <li><b>Real-time Validation:</b> Detects invalid properties, unknown values, and incorrect units on the fly</li>
                        <li><b>Smart Parser Recovery:</b> Correctly handles missing braces without cascading errors to the rest of the file</li>
                        <li><b>Type Safety:</b> Validates measurements (px, pt), colors, and URLs to prevent silent failures</li>
                    </ul>
                </li>
                <li><b>Template & Dynamic Value Support:</b> First-class support for template tags like <code>{{VARIABLE}}</code>—perfect for Python/C++ template engines</li>
                <li><b>Smart Context-Aware Completion:</b> Relevant suggestions based on widget type - scrollbars show horizontal/vertical, checkboxes show checked/unchecked</li>
                <li><b>Enhanced Syntax Highlighting:</b>
                    <ul>
                        <li><b>Semantic Coloring:</b> Distinct colors for Widgets (Teal), Properties (Blue), Keywords (Orange), and Templates</li>
                        <li><b>Theme Compatibility:</b> Fixed invisible widget/property colors in dark themes using reliable color key mapping (CONSTANT + INSTANCE_METHOD)</li>
                        <li><b>URL & Gradient Support:</b> Proper highlighting for <code>url(...)</code> paths and Qt gradient functions</li>
                        <li><b>Improved Tokenization:</b> Numbers with units and CSS keywords are now visually distinct</li>
                    </ul>
                </li>
                <li><b>Background Color Highlighting:</b>
                    <ul>
                        <li><b>Visual Color Display:</b> See actual colors as backgrounds behind color values (#FF0000 shows solid red background)</li>
                        <li><b>Contrast-Aware Text:</b> Text color automatically adjusts (black/white) based on WCAG luminance for optimal readability</li>
                        <li><b>Accurate Colors:</b> Displays exact RGB values with no forced transparency</li>
                        <li><b>Alpha Transparency Support:</b> Preserves original alpha from rgba() and #RRGGBBAA formats</li>
                        <li><b>Accessibility:</b> Implements W3C WCAG 2.0 standards for color contrast (same algorithm as Chrome DevTools, VS Code)</li>
                    </ul>
                </li>
                <li><b>Instant Value Suggestions:</b> Auto-complete for CSS units (px, pt, em), colors, alignments, and border styles</li>
                <li><b>Professional Auto-Trigger:</b> Automatic completion popup after :: and : for effortless workflow</li>
                <li><b>Smart Editing Features:</b>
                    <ul>
                        <li><b>Auto-Close Brackets & Quotes:</b> Automatic insertion of closing characters</li>
                        <li><b>Smart Indentation:</b> Enter key creates properly formatted, indented blocks</li>
                        <li><b>Comment Toggle:</b> Ctrl+/ (Cmd+/) support</li>
                        <li><b>Bracket Matching:</b> Visual highlighting of matching pairs</li>
                        <li><b>Property Completion:</b> Auto-inserts colon and space for faster typing</li>
                    </ul>
                </li>
                <li><b>Advanced Color Tools:</b>
                    <ul>
                        <li><b>RGB/RGBA Support:</b> Full support with gutter previews and format preservation. Transparent colors automatically convert to rgba() for Qt compatibility</li>
                        <li><b>Streamlined Color Picker:</b> Optimized UI with persistent color mode preference (HSV/HSL/RGB)</li>
                        <li><b>Visual Gutter Icons:</b> Color previews with borders for transparent colors</li>
                        <li><b>Gradient Recognition:</b> Support for <code>qlineargradient()</code>, <code>qradialgradient()</code>, and <code>qconicalgradient()</code></li>
                    </ul>
                </li>
                <li><b>Universal IDE Support:</b> Works in ALL JetBrains IDEs (CLion, PyCharm, IntelliJ, WebStorm, Rider, etc.) with NO Python dependency</li>
            </ul>
            
            <h3>Version 1.1 - April 2025</h3>
            <ul>
                <li><b>New Feature:</b> Create new QSS files directly from the IDE's New menu</li>
                <li>Added file template for QSS files with default styling</li>
                <li>Improved plugin integration with IDE's file creation workflow</li>
            </ul>
            
            <h3>Version 1.0 - Initial Release</h3>
            <ul>
                <li>Initial release with basic QSS editing support</li>
                <li>Syntax highlighting, code completion, and structure navigation</li>
                <li>Color palette management and integrated color tools</li>
                <li>Hex color support with gutter icons and color picker</li>
            </ul>
        """.trimIndent()
  }
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_21)
    }
  }

  buildSearchableOptions {
    enabled = false
  }
}
