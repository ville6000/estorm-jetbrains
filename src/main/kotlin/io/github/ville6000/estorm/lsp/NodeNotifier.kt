package io.github.ville6000.estorm.lsp

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import io.github.ville6000.estorm.settings.EstormConfigurable
import io.github.ville6000.estorm.settings.EstormSettings
import java.util.concurrent.atomic.AtomicBoolean

/** Says once per project that there is no Node.js, until the settings change. */
@Service(Service.Level.PROJECT)
class NodeNotifier(private val project: Project) {
    private val shown = AtomicBoolean()

    fun nodeNotFound() {
        if (!shown.compareAndSet(false, true)) return
        val configured = EstormSettings.getInstance().nodePath
        val where = if (configured.isEmpty()) "No <code>node</code> on the PATH" else "No Node.js at <code>$configured</code>"
        NotificationGroupManager.getInstance()
            .getNotificationGroup("estorm")
            .createNotification(
                "estorm: Node.js not found",
                "$where. The language server needs Node.js ${NodeLocator.MIN_MAJOR} or later for errors, " +
                    "completion, go to definition and rename; colours and the preview work without it.",
                NotificationType.WARNING,
            )
            .addAction(
                NotificationAction.createSimpleExpiring("Configure…") {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, EstormConfigurable::class.java)
                },
            )
            .notify(project)
    }

    fun reset() = shown.set(false)

    companion object {
        fun getInstance(project: Project): NodeNotifier = project.service()
    }
}
