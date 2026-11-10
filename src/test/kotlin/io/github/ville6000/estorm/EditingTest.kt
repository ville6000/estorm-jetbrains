package io.github.ville6000.estorm

import com.intellij.codeInsight.highlighting.BraceMatchingUtil
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.ville6000.estorm.lang.EstormColors
import io.github.ville6000.estorm.lang.EstormFile
import io.github.ville6000.estorm.lang.EstormFileType
import io.github.ville6000.estorm.lang.EstormSyntaxHighlighter
import io.github.ville6000.estorm.lang.EstormTokenTypes

class EditingTest : BasePlatformTestCase() {
    fun `test opens estorm files as estorm`() {
        val file = myFixture.configureByText("board.estorm", "Customer: Place order -> (Order) -> OrderPlaced")
        assertInstanceOf(file, EstormFile::class.java)
        assertEquals(EstormFileType, file.virtualFile.fileType)
        assertEquals(EstormFileType, com.intellij.openapi.fileTypes.FileTypeManager.getInstance().getFileTypeByExtension("estorm"))
    }

    fun `test colours brackets like their sticky`() {
        val highlighter = EstormSyntaxHighlighter()
        assertEquals(EstormColors.AGGREGATE, highlighter.getTokenHighlights(EstormTokenTypes.AGGREGATE_OPEN).single())
        assertEquals(EstormColors.EXTERNAL, highlighter.getTokenHighlights(EstormTokenTypes.EXTERNAL_CLOSE).single())
        assertEmpty(highlighter.getTokenHighlights(EstormTokenTypes.TEXT))
    }

    fun `test comments and uncomments lines with #`() {
        myFixture.configureByText("board.estorm", "<caret>Customer: Place order -> (Order) -> OrderPlaced\n")
        myFixture.performEditorAction(IdeActions.ACTION_COMMENT_LINE)
        myFixture.checkResult("# Customer: Place order -> (Order) -> OrderPlaced\n")

        myFixture.configureByText("other.estorm", "<caret># Customer: Place order\n")
        myFixture.performEditorAction(IdeActions.ACTION_COMMENT_LINE)
        myFixture.checkResult("Customer: Place order\n")
    }

    fun `test closes an aggregate's and an external's bracket as you type`() {
        myFixture.configureByText("board.estorm", "Customer: Place order -> <caret>")
        myFixture.type('(')
        myFixture.checkResult("Customer: Place order -> (<caret>)")

        myFixture.configureByText("other.estorm", "  then Charge card -> <caret>")
        myFixture.type('[')
        myFixture.checkResult("  then Charge card -> [<caret>]")
    }

    fun `test matches brackets`() {
        val text = "Customer: Place order -> (Order) -> OrderPlaced"
        myFixture.configureByText("board.estorm", text)
        val open = text.indexOf('(')
        val iterator = (myFixture.editor as EditorEx).highlighter.createIterator(open)
        assertTrue(BraceMatchingUtil.matchBrace(myFixture.editor.document.charsSequence, EstormFileType, iterator, true))
        assertEquals(text.indexOf(')'), iterator.start)
    }
}
