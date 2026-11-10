package io.github.ville6000.estorm.lsp

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.util.SystemInfo
import io.github.ville6000.estorm.settings.EstormSettings
import java.nio.file.Files
import java.nio.file.Path

/** Finds the node that runs the bundled language server. */
object NodeLocator {
    /** The bundle targets Node.js 20 (esbuild.mjs). */
    const val MIN_MAJOR = 20

    /** The configured node, or node on the PATH; null if there is none. Only looks at files, so cheap. */
    fun find(): Path? {
        val configured = EstormSettings.getInstance().nodePath
        if (configured.isNotEmpty()) return Path.of(configured).takeIf { Files.isExecutable(it) }
        return onPath("node")
    }

    /** The first NAME on the PATH that can run. Not PathEnvironmentVariableUtil's finders: 2026.2 and 2026.3 share none. */
    private fun onPath(name: String): Path? {
        val path = PathEnvironmentVariableUtil.getPathVariableValue() ?: return null
        val names = if (SystemInfo.isWindows) {
            PathEnvironmentVariableUtil.getWindowsExecutableFileExtensions().map { name + (if (it.startsWith(".")) it else ".$it") }
        } else {
            listOf(name)
        }
        return PathEnvironmentVariableUtil.getPathDirs(path).asSequence()
            .flatMap { dir -> names.asSequence().mapNotNull { runCatching { Path.of(dir, it) }.getOrNull() } }
            .firstOrNull { Files.isRegularFile(it) && Files.isExecutable(it) }
    }

    /** Throws unless NODE is Node.js MIN_MAJOR or later. Runs it, so not on the UI thread or in a read action. */
    fun checkVersion(node: Path) {
        val output = CapturingProcessHandler(GeneralCommandLine(node.toString(), "--version")).runProcess(10_000)
        val version = output.stdout.trim()
        val major = Regex("""^v(\d+)\.""").find(version)?.groupValues?.get(1)?.toInt()
        if (output.isTimeout || output.exitCode != 0 || major == null) {
            throw ExecutionException("$node --version failed: ${output.stderr.trim().ifEmpty { version }}")
        }
        if (major < MIN_MAJOR) {
            throw ExecutionException("estorm's language server needs Node.js $MIN_MAJOR or later, and $node is $version")
        }
    }
}
