package io.github.ville6000.estorm

import com.intellij.execution.ExecutionException
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.ville6000.estorm.lsp.EstormLspDescriptor
import io.github.ville6000.estorm.lsp.NodeLocator
import io.github.ville6000.estorm.settings.EstormSettings
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

class LspTest : BasePlatformTestCase() {
    private val options get() = EstormSettings.getInstance().state

    override fun tearDown() {
        try {
            options.nodePath = ""
            options.serverCommand = ""
        } finally {
            super.tearDown()
        }
    }

    fun `test serves estorm files only`() {
        val descriptor = EstormLspDescriptor(project)
        assertTrue(descriptor.isSupportedFile(myFixture.configureByText("board.estorm", "").virtualFile))
        assertFalse(descriptor.isSupportedFile(myFixture.configureByText("notes.txt", "").virtualFile))
    }

    fun `test runs a server command in the project folder, quotes and all`() {
        options.serverCommand = "node '/path/to my/estorm/src/cli.ts' lsp"
        val command = EstormLspDescriptor(project).createCommandLine()
        assertEquals(listOf("node", "/path/to my/estorm/src/cli.ts", "lsp"), listOf(command.exePath) + command.parametersList.list)
        assertEquals(project.basePath?.let(Path::of), command.workingDirectory)
    }

    fun `test runs the bundled server with the configured node`() {
        val node = fakeNode("v22.18.0")
        options.nodePath = node.toString()
        assertEquals(node, NodeLocator.find())
        // The tests run the plugin from its sandbox, laid out as when installed: server/ beside lib/.
        val command = EstormLspDescriptor(project).createCommandLine()
        assertEquals(node.toString(), command.exePath)
        val server = Path.of(command.parametersList.list.single())
        assertTrue("$server", server.endsWith("server/server.mjs") && Files.isRegularFile(server))
    }

    fun `test finds node on the PATH`() {
        // The tests run where the build does, and the build needs node.
        val node = NodeLocator.find()
        assertNotNull(node)
        NodeLocator.checkVersion(node!!)
    }

    fun `test refuses a node that is missing or too old`() {
        options.nodePath = "/no/such/node"
        assertNull(NodeLocator.find())
        val old = fakeNode("v18.20.4")
        val error = runCatching { NodeLocator.checkVersion(old) }.exceptionOrNull()
        assertTrue("$error", error is ExecutionException && error.message!!.contains("needs Node.js 20 or later"))
        NodeLocator.checkVersion(fakeNode("v20.0.0"))
    }

    /** A script that answers --version like a node of VERSION. */
    private fun fakeNode(version: String): Path {
        val dir = Files.createTempDirectory("estorm-node")
        val node = dir.resolve("node")
        Files.writeString(node, "#!/bin/sh\necho $version\n")
        Files.setPosixFilePermissions(node, PosixFilePermissions.fromString("rwxr-xr-x"))
        return node
    }
}
