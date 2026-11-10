package io.github.ville6000.estorm.lsp

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.platform.lsp.api.ProjectWideLspClientDescriptor
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.platform.lsp.api.customization.LspDocumentHighlightsCustomizer
import com.intellij.platform.lsp.api.customization.LspDocumentHighlightsSupport
import com.intellij.platform.lsp.api.customization.LspRenameCustomizer
import com.intellij.platform.lsp.api.customization.LspRenameSupport
import com.intellij.platform.lsp.api.customization.LspSemanticTokensCustomizer
import com.intellij.platform.lsp.api.customization.LspSemanticTokensDisabled
import com.intellij.util.execution.ParametersListUtil
import io.github.ville6000.estorm.settings.EstormSettings
import java.nio.file.Files
import java.nio.file.Path

/**
 * The estorm language server: errors and lint warnings, highlights of the
 * event under the caret, completion, go to definition and rename. By default
 * the server bundled in the plugin's server/server.mjs, run with node.
 */
class EstormLspDescriptor(project: Project) : ProjectWideLspClientDescriptor(project, "estorm") {
    override fun isSupportedFile(file: VirtualFile) = isEstorm(file)

    override fun createCommandLine(): GeneralCommandLine {
        val custom = EstormSettings.getInstance().serverCommand
        if (custom.isNotEmpty()) {
            return GeneralCommandLine(ParametersListUtil.parse(custom, false, true)).withWorkingDirectory(project.basePath?.let(Path::of))
        }
        val node = NodeLocator.find() ?: throw ExecutionException("Node.js not found: set it in Settings | Languages & Frameworks | estorm")
        NodeLocator.checkVersion(node)
        val server = serverScript()
        if (!Files.isRegularFile(server)) throw ExecutionException("The bundled language server is missing: $server")
        return GeneralCommandLine(node.toString(), server.toString())
    }

    override val lspCustomization: LspCustomization = object : LspCustomization() {
        /** The lexer colours the text (EstormSyntaxHighlighter), the same way as the server's semantic tokens. */
        override val semanticTokensCustomizer: LspSemanticTokensCustomizer = LspSemanticTokensDisabled

        // By default the IDE asks the server for these only in plain text and TextMate files, leaving languages
        // with a parser to their own; estorm's parser only splits tokens, so they come from the server.
        override val renameCustomizer: LspRenameCustomizer = object : LspRenameSupport() {
            override fun shouldRunRename(psiFile: PsiFile) = true
        }
        override val documentHighlightsCustomizer: LspDocumentHighlightsCustomizer = object : LspDocumentHighlightsSupport() {
            override fun shouldAskServerForDocumentHighlights(psiFile: PsiFile) = true
        }
    }

    companion object {
        fun isEstorm(file: VirtualFile) = file.extension == "estorm"

        /** server/server.mjs in the plugin's directory, next to lib/ with this class's jar. */
        fun serverScript(): Path =
            Path.of(PathManager.getJarPathForClass(EstormLspDescriptor::class.java)!!).parent.parent.resolve("server/server.mjs")
    }
}
