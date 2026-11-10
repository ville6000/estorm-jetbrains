package io.github.ville6000.estorm

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenameHandler
import com.intellij.refactoring.rename.RenameHandlerRegistry
import com.intellij.openapi.roots.ContentEntry
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.impl.TempDirTestFixtureImpl
import io.github.ville6000.estorm.lsp.EstormLspProvider

/**
 * The bundled language server through the IDE's own LSP client, as in an
 * installed plugin: the tests run the plugin from its sandbox, server/ and all.
 */
class ServerTest : BasePlatformTestCase() {
    // Files on disk in the project, which the client sends to the server, rather than in memory.
    override fun createTempDirTestFixture() = TempDirTestFixtureImpl()

    private lateinit var root: ContentEntry

    override fun setUp() {
        super.setUp()
        root = PsiTestUtil.addContentRoot(module, myFixture.tempDirFixture.getFile("")!!)!!
    }

    override fun tearDown() {
        try {
            LspClientManager.getInstance(project).stopClients(EstormLspProvider::class.java)
            PsiTestUtil.removeContentEntry(module, root.file!!)
        } catch (e: Throwable) {
            addSuppressedException(e)
        } finally {
            super.tearDown()
        }
    }

    private fun startServer() {
        val manager = LspClientManager.getInstance(project)
        manager.startClientsIfNeeded(EstormLspProvider::class.java)
        PlatformTestUtil.waitWithEventsDispatching(
            "the estorm language server didn't start",
            { manager.getClients(EstormLspProvider::class.java).any { it.state == LspServerState.Running } },
            30,
        )
    }

    /** The highlights once one matches FOUND, as the server's answers arrive after the edit. */
    private fun highlightsUntil(found: (HighlightInfo) -> Boolean): List<HighlightInfo> {
        var infos = emptyList<HighlightInfo>()
        PlatformTestUtil.waitWithEventsDispatching("no such highlight in $infos", {
            infos = myFixture.doHighlighting()
            infos.any(found)
        }, 30)
        return infos
    }

    fun `test reports parse errors as you type`() {
        myFixture.configureByText("board.estorm", "Customer: Place order -> (Order) -> OrderPlaced\n<caret>")
        startServer()
        myFixture.type("Customer: -> (")
        val error = highlightsUntil { it.severity == HighlightSeverity.ERROR }.first { it.severity == HighlightSeverity.ERROR }
        assertEquals(2, myFixture.editor.document.getLineNumber(error.startOffset) + 1)
    }

    fun `test completes names already on the board`() {
        myFixture.configureByText("board.estorm", "Customer: Place order -> (Order) -> OrderPlaced\nwhen Ord<caret>\n")
        startServer()
        var names = emptyList<String>()
        PlatformTestUtil.waitWithEventsDispatching("no OrderPlaced in $names", {
            names = myFixture.completeBasic()?.map { it.lookupString }.orEmpty()
            "OrderPlaced" in names || myFixture.editor.document.text.contains("when OrderPlaced")
        }, 30)
    }

    fun `test goes from an event in a when to the step that produces it`() {
        myFixture.configureByText("board.estorm", "Customer: Place order -> (Order) -> OrderPlaced\nwhen Order<caret>Placed\n")
        startServer()
        val target = myFixture.editor.document.text.indexOf("OrderPlaced")
        PlatformTestUtil.waitWithEventsDispatching("didn't go to the definition", {
            myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION)
            myFixture.caretOffset in target..target + "OrderPlaced".length
        }, 30)
    }

    fun `test highlights every mention of the event under the caret`() {
        myFixture.configureByText(
            "board.estorm",
            "Customer: Place order -> (Order) -> Order<caret>Placed\nwhen OrderPlaced\n  then Ship -> (Order) -> Shipped\n",
        )
        startServer()
        // The server's handler, once the client has the file open; it highlights when the server answers.
        var handler: HighlightUsagesHandlerBase<*>? = null
        PlatformTestUtil.waitWithEventsDispatching("no highlighting from the server", {
            handler = HighlightUsagesHandler.createCustomHandler<PsiElement>(myFixture.editor, myFixture.file)
            handler != null
        }, 30)
        handler!!.highlightUsages()
        var ranges = emptyList<String>()
        PlatformTestUtil.waitWithEventsDispatching({ "highlighted $ranges" }, {
            ranges = myFixture.editor.markupModel.allHighlighters
                .filter { it.textAttributesKey in setOf(EditorColors.SEARCH_RESULT_ATTRIBUTES, EditorColors.WRITE_SEARCH_RESULT_ATTRIBUTES) }
                .map { myFixture.editor.document.getText(it.textRange) }
            ranges.size == 2
        }, 30)
        assertEquals(listOf("OrderPlaced", "OrderPlaced"), ranges)
    }

    fun `test renames an event everywhere on the board`() {
        rename(
            "Customer: Place order -> (Order) -> Order<caret>Placed\nwhen OrderPlaced\n",
            "OrderSubmitted",
            "Customer: Place order -> (Order) -> OrderSubmitted\nwhen OrderSubmitted\n",
        )
    }

    fun `test renames an external of several words`() {
        rename(
            "when Paid\n  then Charge -> [Payment <caret>provider] -> Charged\n  then Refund -> [Payment provider] -> Refunded\n",
            "Card gateway",
            "when Paid\n  then Charge -> [Card gateway] -> Charged\n  then Refund -> [Card gateway] -> Refunded\n",
        )
    }

    /** Renames the name at the caret in BOARD to NAME with Shift+F6's handler, which renames in place like a template. */
    private fun rename(board: String, name: String, expected: String) {
        TemplateManagerImpl.setTemplateTesting(testRootDisposable)
        myFixture.configureByText("board.estorm", board)
        startServer()
        val context = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.EDITOR, myFixture.editor)
            .add(CommonDataKeys.PSI_FILE, myFixture.file)
            .add(CommonDataKeys.VIRTUAL_FILE, myFixture.file.virtualFile)
            .build()
        // The server's handler, once the client has the file open.
        var handler: RenameHandler? = null
        PlatformTestUtil.waitWithEventsDispatching("no rename from the server", {
            handler = RenameHandlerRegistry.getInstance().getRenameHandler(context)
            handler != null
        }, 30)
        handler!!.invoke(project, myFixture.editor, myFixture.file, context)
        val template = TemplateManagerImpl.getTemplateState(myFixture.editor)
        assertNotNull("no inline rename", template)
        WriteCommandAction.runWriteCommandAction(project) {
            val range = template!!.currentVariableRange!!
            myFixture.editor.document.replaceString(range.startOffset, range.endOffset, name)
        }
        // As pressing Enter does, in a command.
        CommandProcessor.getInstance().executeCommand(project, { template!!.gotoEnd(false) }, "Rename", null)
        PlatformTestUtil.waitWithEventsDispatching({ "didn't rename: ${myFixture.editor.document.text}" }, {
            myFixture.editor.document.text == expected
        }, 30)
    }
}
