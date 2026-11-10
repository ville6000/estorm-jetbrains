package io.github.ville6000.estorm.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

/** How to run the language server, for every project: Settings | Languages & Frameworks | estorm. */
@Service(Service.Level.APP)
@State(name = "EstormSettings", storages = [Storage("estorm.xml")])
class EstormSettings : SimplePersistentStateComponent<EstormSettings.Options>(Options()) {
    class Options : BaseState() {
        /** The node that runs the bundled server; empty: node on the PATH. */
        var nodePath by string("")

        /** Another language server to run instead of the bundled one, in the project folder; empty: the bundled one. */
        var serverCommand by string("")
    }

    val nodePath: String get() = state.nodePath.orEmpty().trim()

    val serverCommand: String get() = state.serverCommand.orEmpty().trim()

    companion object {
        fun getInstance(): EstormSettings = service()
    }
}
