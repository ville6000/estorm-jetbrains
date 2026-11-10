package io.github.ville6000.estorm.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import io.github.ville6000.estorm.lsp.EstormLspProvider
import io.github.ville6000.estorm.lsp.NodeLocator
import io.github.ville6000.estorm.lsp.NodeNotifier

/** Settings | Languages & Frameworks | estorm. Applying restarts the language server in every open project. */
class EstormConfigurable : BoundConfigurable("estorm") {
    override fun createPanel(): DialogPanel {
        val options = EstormSettings.getInstance().state
        return panel {
            row("Node.js:") {
                textFieldWithBrowseButton(FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor().withTitle("Node.js"))
                    .bindText({ options.nodePath.orEmpty() }, { options.nodePath = it.trim() })
                    .align(AlignX.FILL)
                    .comment(
                        "Runs the language server, which gives errors, completion, go to definition and rename. " +
                            "Empty: <code>node</code> on the PATH. Needs Node.js ${NodeLocator.MIN_MAJOR} or later.",
                    )
            }
            row("Server command:") {
                textField()
                    .bindText({ options.serverCommand.orEmpty() }, { options.serverCommand = it.trim() })
                    .align(AlignX.FILL)
                    .comment(
                        "Runs another estorm language server, in the project folder. Empty: the one bundled with " +
                            "the plugin. From a checkout of estorm: <code>node /path/to/estorm/src/cli.ts lsp</code>",
                    )
            }
        }
    }

    override fun apply() {
        super.apply()
        for (project in ProjectManager.getInstance().openProjects) {
            NodeNotifier.getInstance(project).reset()
            LspClientManager.getInstance(project).stopAndRestartClientsIfNeeded(EstormLspProvider::class.java)
        }
    }
}
