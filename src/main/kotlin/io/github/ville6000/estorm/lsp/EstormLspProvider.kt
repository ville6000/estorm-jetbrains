package io.github.ville6000.estorm.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.api.lsWidget.LspClientWidgetItem
import io.github.ville6000.estorm.lang.EstormIcons
import io.github.ville6000.estorm.settings.EstormConfigurable
import io.github.ville6000.estorm.settings.EstormSettings

/** Starts the estorm language server for .estorm files, once per project. */
class EstormLspProvider : LspIntegrationProvider {
    override fun fileOpened(project: Project, file: VirtualFile, clientStarter: LspIntegrationProvider.LspClientStarter) {
        if (!EstormLspDescriptor.isEstorm(file)) return
        // Without a custom command, the bundled server needs node. In a read action here, so only look for it.
        if (EstormSettings.getInstance().serverCommand.isEmpty() && NodeLocator.find() == null) {
            NodeNotifier.getInstance(project).nodeNotFound()
            return
        }
        clientStarter.ensureClientStarted(EstormLspDescriptor(project))
    }

    /** The server in the status bar's Language Services widget: restart, stop, and a link to the settings. */
    override fun createWidgetItem(lspClient: LspClient, currentFile: VirtualFile?) =
        LspClientWidgetItem(lspClient, currentFile, EstormIcons.FILE, EstormConfigurable::class.java)
}
