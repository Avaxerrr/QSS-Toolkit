plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.25"
  id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "io.github.avaxerrr"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

// Configure Gradle IntelliJ Plugin
dependencies {
  intellijPlatform {
    // Change to PyCharm Community Edition
    // The "PC" type indicates PyCharm Community Edition
    create("PC", "2024.2.5")
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

    // For PyCharm Community, only use PythonCore (not Pythonid)
    bundledPlugin("PythonCore")
  }
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "242"
    }

    changeNotes = """
            Initial version
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
}
