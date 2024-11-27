import org.aya.gradle.BuildUtil
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

fun properties(key: String) = providers.gradleProperty(key)
var deps: Properties by rootProject.ext

deps = Properties()
file("gradle/deps.properties").reader().use(deps::load)

val javaVersion = properties("javaVersion").get().toInt()
val ayaVersion = deps.getProperty("version.aya").toString()

plugins {
  // Java support
  java
  // Kotlin support
  kotlin("jvm") version "2.1.0"
  // https://github.com/JetBrains/gradle-intellij-plugin
  id("org.jetbrains.intellij.platform") version "2.1.0"
  // https://github.com/JetBrains/gradle-changelog-plugin
  id("org.jetbrains.changelog") version "2.2.1"
  // https://github.com/JetBrains/gradle-grammar-kit-plugin
  id("org.jetbrains.grammarkit") version "2022.3.2.2"
}

group = properties("pluginGroup")
version = deps.getProperty("version.project")

// Configure project's dependencies
repositories {
  mavenLocal()
  mavenCentral()
  if (ayaVersion.endsWith("SNAPSHOT")) {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }

  intellijPlatform.defaultRepositories()
}

intellijPlatform.pluginConfiguration {
  name = properties("pluginName")
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
  groups.set(emptyList())
}

java {
  withSourcesJar()
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(javaVersion))
  }
}

val genDir = file("src/main/gen")
idea.module.generatedSourceDirs.add(genDir)
sourceSets.main {
  java.srcDirs(genDir)
}

val genAyaPsiParser = tasks.register<GenerateParserTask>("genAyaParser") {
  group = "build setup"
  sourceFile.set(file("src/main/grammar/AyaPsiParser.bnf"))
  targetRootOutputDir.set(file("src/main/gen"))
  pathToParser.set("org/aya/parser/AyaPsiParser.java")
  pathToPsiRoot.set("org/aya/intellij/psi")
  purgeOldFiles.set(true)

  doLast {
    // Already exists in "org.aya-prover:parser"
    file("src/main/gen/org/aya/parser/AyaPsiParser.java").delete()
    // We only need the Factory class
    val src = file("src/main/gen/org/aya/parser/AyaPsiElementTypes.java")
    val dst = file("src/main/gen/org/aya/parser/AyaPsiElementTypesFactory.java")
    dst.writer().use { out ->
      src.readLines().forEach { line ->
        if (line == "public interface AyaPsiElementTypes {")
          out.write(
            """
            import static org.aya.parser.AyaPsiElementTypes.*;
            public interface AyaPsiElementTypesFactory {
          """.trimIndent(),
          )
        else if (!line.contains("IElementType (.+) = new AyaPsi(Element|Token)Type\\(\\\"(.+)\\\"\\);".toRegex()))
          out.write("$line\n")
      }
    }
    src.delete()
  }
}

tasks {
  compileJava.configure {
    dependsOn(genAyaPsiParser)
  }
  named("sourcesJar").configure {
    dependsOn(genAyaPsiParser)
  }

  withType<JavaCompile>().configureEach {
    modularity.inferModulePath.set(true)
    options.apply {
      encoding = "UTF-8"
      isDeprecation = true
      release.set(javaVersion)
      compilerArgs.addAll(listOf("-Xlint:unchecked", "--enable-preview"))
    }

    doLast {
      val tree = fileTree(destinationDirectory)
      tree.include("**/*.class")
      tree.exclude("module-info.class")
      val root = project.layout.buildDirectory.asFile.get().toPath().resolve("classes/java/main")
      tree.forEach { BuildUtil.stripPreview(root, it.toPath()) }
    }
    dependsOn(genAyaPsiParser)
  }

  withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    dependsOn(genAyaPsiParser)
  }

  withType<Test>().configureEach {
    jvmArgs = listOf(
      "--add-opens=java.base/java.lang=ALL-UNNAMED",
      "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
      "--add-opens=java.base/java.io=ALL-UNNAMED",
      "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
      "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
      "--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
      "--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED",
    )
    enableAssertions = true
    reports.junitXml.mergeReruns.set(true)
  }

  patchPluginXml {
    version = project.version.toString()
    sinceBuild.set(properties("pluginSinceBuild"))
    untilBuild.set(properties("pluginUntilBuild"))

    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
    pluginDescription.set(
      projectDir.resolve("README.md").readText().lines().run {
        val start = "<!-- Plugin description -->"
        val end = "<!-- Plugin description end -->"

        if (!containsAll(listOf(start, end))) {
          throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
        }
        subList(indexOf(start) + 1, indexOf(end))
      }.joinToString("\n").run { markdownToHTML(this) },
    )

    // Get the latest available change notes from the changelog file
    val changelog = project.changelog // local variable for configuration cache compatibility
    // Get the latest available change notes from the changelog file
    changeNotes.set(
      with(changelog) {
        renderItem(
          (getOrNull(project.version.toString()) ?: getUnreleased())
            .withHeader(false)
            .withEmptySections(false),
          Changelog.OutputType.HTML,
        )
      },
    )
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }
}

dependencies {
  intellijPlatform {
    intellijIdeaCommunity("2024.3")
    bundledPlugin("com.intellij.java")
  }

  implementation("org.aya-prover", "producer", ayaVersion) {
    exclude("org.aya-prover.upstream", "ij-parsing-core")
    exclude("org.aya-prover.upstream", "ij-util-text")
    exclude("org.aya-prover.upstream", "lang-syntax")
  }
  implementation("org.aya-prover", "ide-lsp", ayaVersion) {
    exclude("org.aya-prover.upstream", "ij-parsing-core")
    exclude("org.aya-prover.upstream", "ij-util-text")
    exclude("org.aya-prover.upstream", "lang-syntax")
  }
  testImplementation(kotlin("test-junit"))
  testImplementation(group = "org.opentest4j", name = "opentest4j", version = "1.2.0")
}
