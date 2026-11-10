package io.github.ville6000.estorm.preview

import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColorsListener
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.Alarm
import java.awt.Point
import java.beans.PropertyChangeListener
import javax.swing.JComponent

/**
 * A live preview of the board: estorm's preview page in a JCEF browser,
 * which renders the text itself (host.js), so it needs no Node.js. Sends the
 * text once the page is ready and 150 ms after each change; a click on a
 * sticky or an error selects its line in the text.
 */
class EstormPreviewEditor(private val project: Project, private val file: VirtualFile) : UserDataHolderBase(), FileEditor {
    /** The split editor this is the preview of, for showing the text on a click. */
    var split: TextEditorWithPreview? = null

    private val browser = JBCefBrowser()
    private val query = JBCefJSQuery.create(browser as JBCefBrowserBase)
    private val document = FileDocumentManager.getInstance().getDocument(file)
    private val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    private var ready = false

    init {
        Disposer.register(this, browser)
        Disposer.register(this, query)
        query.addHandler { message ->
            // Off the UI thread; the editor may have closed by the time it runs.
            ApplicationManager.getApplication().invokeLater({ if (!browser.isDisposed) receive(message) }, project.disposed)
            null
        }
        document?.addDocumentListener(
            object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) = schedule()
            },
            this,
        )
        ApplicationManager.getApplication().messageBus.connect(this).apply {
            subscribe(LafManagerListener.TOPIC, LafManagerListener { themeChanged() })
            subscribe(EditorColorsManager.TOPIC, EditorColorsListener { themeChanged() })
        }
        browser.setPageBackgroundColor(ThemeCss.background())
        browser.loadHTML(PreviewHtml.page(ThemeCss.css()) { expression -> query.inject(expression) })
    }

    private fun receive(message: String) {
        when {
            message == "ready" -> {
                ready = true
                show()
            }
            message.startsWith("reveal:") -> message.removePrefix("reveal:").toIntOrNull()?.let(::reveal)
        }
    }

    private fun schedule() {
        alarm.cancelAllRequests()
        alarm.addRequest(::show, DELAY_MS)
    }

    private fun show() {
        if (!ready || browser.isDisposed) return
        val text = document?.text ?: return
        browser.runJavaScript(PreviewHtml.show(text, ThemeCss.boardTheme()))
    }

    private fun themeChanged() {
        if (!ready || browser.isDisposed) return
        browser.setPageBackgroundColor(ThemeCss.background())
        browser.runJavaScript(PreviewHtml.setTheme(ThemeCss.css()))
        show()
    }

    /** Selects LINE (1-based) in the text from its first non-space character, scrolling to it if it's out of view. */
    private fun reveal(line: Int) {
        val split = split ?: return
        if (split.getLayout() == TextEditorWithPreview.Layout.SHOW_PREVIEW) {
            split.setLayout(TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW)
        }
        val editor = split.textEditor.editor
        val doc = editor.document
        if (doc.lineCount == 0) return
        val at = (line - 1).coerceIn(0, doc.lineCount - 1)
        val start = doc.getLineStartOffset(at)
        val end = doc.getLineEndOffset(at)
        val indent = doc.charsSequence.subSequence(start, end).indexOfFirst { !it.isWhitespace() }
        val from = if (indent == -1) end else start + indent
        editor.caretModel.moveToOffset(from)
        editor.selectionModel.setSelection(from, end)
        val y = editor.logicalPositionToXY(editor.offsetToLogicalPosition(from)).y
        val visible = editor.scrollingModel.visibleArea
        if (!visible.contains(Point(visible.x, y)) || !visible.contains(Point(visible.x, y + editor.lineHeight - 1))) {
            editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        }
        IdeFocusManager.getInstance(project).requestFocus(editor.contentComponent, true)
    }

    override fun getComponent(): JComponent = browser.component

    override fun getPreferredFocusedComponent(): JComponent = browser.component

    override fun getName() = "Preview"

    override fun getFile() = file

    override fun setState(state: FileEditorState) = Unit

    override fun isModified() = false

    override fun isValid() = file.isValid

    override fun addPropertyChangeListener(listener: PropertyChangeListener) = Unit

    override fun removePropertyChangeListener(listener: PropertyChangeListener) = Unit

    override fun dispose() = Unit

    private companion object {
        const val DELAY_MS = 150
    }
}
