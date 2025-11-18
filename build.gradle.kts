// QSS Toolkit version 1.2

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.1.0"
  id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "io.github.avaxerrr"
version = "1.1"

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}

// Configure dependencies
dependencies {
  // Coroutines support
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

  intellijPlatform {

    intellijIdeaCommunity("2025.1")

    // Test framework
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

  }
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      // Support from 2025.1 (build 241) onwards
      sinceBuild = "241"
      // Allow until 2025.* versions
      untilBuild = "251.*"
    }

    changeNotes = """
            <h3>Version xx (DON'T TOUCH THIS I WILL DECIDED WHICH VERSION TO PUT HERE) - November 2025</h3>
            <ul>
                <li><b>Complete Qt 6.10 Support:</b> All 96 properties and 50 widgets with full Qt 5/6 backward compatibility</li>
                <li><b>Advanced Selector Support:</b> 82 sub-controls (QScrollBar::handle) and 44 pseudo-states with intelligent chained syntax (QScrollBar::handle:vertical)</li>
                <li><b>Smart Context-Aware Completion:</b> Relevant suggestions based on widget type - scrollbars show horizontal/vertical, checkboxes show checked/unchecked</li>
                <li><b>Instant Value Suggestions:</b> Auto-complete for CSS units (px, pt, em), colors, alignments, and border styles</li>
                <li><b>Professional Auto-Trigger:</b> Automatic completion popup after :: and : for effortless workflow</li>
                <li><b>Smart Editing Features:</b> Professional auto-completion and formatting</li>
                <li><b>Comment Toggle:</b> Ctrl+/ (Cmd+/) to quickly comment/uncomment QSS code blocks</li>
                <li><b>Auto-Close Brackets:</b> Automatic insertion of closing braces, parentheses, and brackets</li>
                <li><b>Smart Indentation:</b> Automatic indentation when creating new blocks with proper formatting</li>
                <li><b>Quote Completion:</b> Auto-closes quotes for string values</li>
                <li><b>Bracket Matching:</b> Highlights matching braces, parentheses, and brackets</li>
                <li><b>Smart Enter Key:</b> Pressing Enter between braces creates properly indented block</li>
                <li><b>Property Completion:</b> Auto-inserts colon and space after accepting property suggestions</li>
                <li><b>Function Completions:</b> Added rgb() and rgba() color functions</li>
                <li><b>Resource Functions:</b> Added url() for images and qlineargradient(), qradialgradient(), qconicalgradient() for Qt gradients</li>
                <li><b>Context-Aware Suggestions:</b> Right functions suggested for the right properties</li>
                <li><b>Enhanced Syntax Highlighting:</b> Professional color scheme with proper semantic separation</li>
                <li><b>Improved Number Highlighting:</b> Numbers with units (px, em, %, pt) now properly tokenized and colored</li>
                <li><b>Keyword Recognition:</b> CSS keywords (transparent, solid, bold, etc.) now distinct from identifiers</li>
                <li><b>Better Selector Colors:</b> ID/class selectors visually distinct from property values</li>
                <li><b>RGB/RGBA Support:</b> Full support for rgb() and rgba() color functions with gutter previews</li>
                <li><b>Format Preservation:</b> Color picker preserves original format (hex, RGB, or RGBA)</li>
                <li><b>Streamlined Color Picker:</b> Removed CMYK and Swatches tabs, remembers your preferred mode</li>
                <li><b>Universal IDE Support:</b> Works in ALL JetBrains IDEs (CLion, PyCharm, IntelliJ, WebStorm, Rider, etc.)</li>
                <li><b>No Python Dependency:</b> Perfect for C++ Qt developers in CLion - no Python required!</li>
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
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
  }

  // Disable building searchable options (speeds up build)
  buildSearchableOptions {
    enabled = false
  }
}
