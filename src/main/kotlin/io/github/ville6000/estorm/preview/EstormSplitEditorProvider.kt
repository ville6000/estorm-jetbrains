package io.github.ville6000.estorm.preview

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.fileEditor.TextEditorWithPreviewProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefApp
import io.github.ville6000.estorm.lang.EstormFileType

/**
 * Opens .estorm files as the text beside a live preview of the board; the
 * editor's toolbar switches between the text, both and the preview. Without
 * JCEF, the text alone.
 */
class EstormSplitEditorProvider : TextEditorWithPreviewProvider(EstormPreviewProvider()) {
    override fun createSplitEditor(firstEditor: TextEditor, secondEditor: FileEditor): FileEditor {
        val split = TextEditorWithPreview(firstEditor, secondEditor, "estorm", TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW)
        (secondEditor as? EstormPreviewEditor)?.split = split
        return split
    }

    override fun getEditorTypeId() = "estorm-split-editor"
}

/** The preview half of the split editor; registered only through EstormSplitEditorProvider. */
class EstormPreviewProvider : FileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile) = file.fileType == EstormFileType && JBCefApp.isSupported()

    override fun acceptRequiresReadAction() = false

    override fun createEditor(project: Project, file: VirtualFile): FileEditor = EstormPreviewEditor(project, file)

    override fun getEditorTypeId() = "estorm-preview"

    override fun getPolicy() = FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
}
