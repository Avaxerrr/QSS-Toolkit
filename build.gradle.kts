// QSS Toolkit version 1.1

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
            <h3>Version xx (not yet sure) - November 2025</h3>
            <ul>
                <li><b>Universal IDE Support:</b> Now works in ALL JetBrains IDEs (CLion, IntelliJ IDEA, PyCharm, WebStorm, etc.)</li>
                <li><b>No Python Dependency:</b> Removed Python requirement - perfect for C++ Qt developers in CLion</li>
            </ul>
            <h3>Version 1.1 - April 2025</h3>
            <ul>
                <li><b>New Feature:</b> Create new QSS files directly from the IDE's New menu</li>
                <li>Added file template for QSS files with default styling</li>
                <li>Improved plugin integration with IDE's file creation workflow</li>
            </ul>
            <h3>Version 1.0</h3>
            <ul>
                <li>Initial release with basic QSS editing support</li>
                <li>Syntax highlighting, code completion, and structure navigation</li>
                <li>Color palette management and integrated color tools</li>
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
