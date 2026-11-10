import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.20.1"
}

group = "io.github.ville6000.estorm"
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(25)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledModule("intellij.platform.lsp")
        bundledPlugin("com.intellij.modules.jcef")
        bundledModule("intellij.platform.ui.jcef")
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
}

val pluginVersion = project.version.toString()

intellijPlatform {
    pluginConfiguration {
        version = pluginVersion
        ideaVersion {
            sinceBuild = "262"
            untilBuild = provider { null }
        }
        // The changelog's "## <version>" section, for the Marketplace's "What's New".
        changeNotes = run {
            val heading = "## $pluginVersion"
            providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText.map { changelog ->
                changelog.lines()
                    .dropWhile { it != heading }.drop(1)
                    .takeWhile { !it.startsWith("## ") }
                    .joinToString("\n")
                    .split(Regex("""\n(?=- )"""))
                    .map { it.trim().removePrefix("- ").replace(Regex("""\s+"""), " ") }
                    .filter { it.isNotEmpty() }
                    .joinToString("", "<ul>", "</ul>") { "<li>${it.replace(Regex("`([^`]+)`"), "<code>$1</code>")}</li>" }
            }
        }
    }
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
    pluginVerification {
        ides {
            // -PverifyIdes=<path>,<path> checks installed IDEs instead of downloading these.
            val installed = providers.gradleProperty("verifyIdes").orNull
            if (installed != null) {
                installed.split(',').forEach { local(file(it.trim())) }
            } else {
                create(IntelliJPlatformType.IntellijIdea, "2026.2")
                create(IntelliJPlatformType.WebStorm, "2026.3")
                create(IntelliJPlatformType.Rider, "2026.2")
            }
        }
    }
}

// The JavaScript the plugin ships (esbuild.mjs), from node_modules: run `npm ci` first.
val buildJs = tasks.register<Exec>("buildJs") {
    description = "Bundles the language server and the preview's renderer, and copies estorm's preview page."
    inputs.files("esbuild.mjs", "package-lock.json")
    inputs.dir("js")
    inputs.dir("node_modules/@villev/estorm/dist").optional()
    outputs.dir(layout.buildDirectory.dir("js"))
    commandLine("node", "esbuild.mjs")
}

// What estorm's own tokenizer makes of the test boards, for TokenParityTest.
val goldenTokens = tasks.register<Exec>("goldenTokens") {
    description = "Writes estorm's tokens for the test boards to build/golden/tokens.txt."
    inputs.files("js/tokens.mjs", "package-lock.json")
    inputs.dir("src/test/resources/grammar")
    inputs.dir("node_modules/@villev/estorm/dist").optional()
    outputs.file(layout.buildDirectory.file("golden/tokens.txt"))
    commandLine("node", "js/tokens.mjs")
}

tasks {
    // The preview's host.js and estorm's preview page, loaded by PreviewHtml from /preview/ in the jar.
    processResources {
        from(buildJs) {
            include("preview/**")
        }
    }
    // node runs the server from a file, so it goes in the plugin directory (server/server.mjs), not the jar.
    withType<PrepareSandboxTask>().configureEach {
        from(buildJs) {
            include("server/**")
            into(pluginName)
        }
    }
    test {
        dependsOn(goldenTokens)
        systemProperty("estorm.goldenTokens", layout.buildDirectory.file("golden/tokens.txt").get().asFile.path)
    }
}
