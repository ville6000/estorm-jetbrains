package io.github.ville6000.estorm

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.jcef.JBCefApp
import io.github.ville6000.estorm.preview.EstormPreviewProvider
import io.github.ville6000.estorm.preview.PreviewHtml
import io.github.ville6000.estorm.preview.ThemeCss

class PreviewTest : BasePlatformTestCase() {
    private fun resource(name: String) = javaClass.getResourceAsStream("/preview/$name")!!.readAllBytes().decodeToString()

    fun `test inlines the page, so it loads nothing`() {
        val page = PreviewHtml.page(":root {}") { expression -> "cefQuery_1({request: $expression});" }
        for (file in listOf("preview.css", "host.js", "preview.js")) assertTrue(file, page.contains(resource(file).trim().take(200)))
        assertTrue(page.contains("cefQuery_1({request: message});"))
        // The only URL in it is SVG's namespace, which loads nothing.
        val urls = Regex("""https?://[^"' )`]+""").findAll(page).map { it.value }.toSet()
        assertEquals(setOf("http://www.w3.org/2000/svg"), urls)
        // url(#…) points into the SVG itself.
        assertFalse(Regex("""(src|href)\s*=|@import|url\((?!#)""").containsMatchIn(page))
    }

    fun `test loads the host before the page`() {
        val page = PreviewHtml.page("") { "" }
        val host = page.indexOf("window.estormHost")
        val renderer = page.indexOf(resource("host.js").trim().take(100))
        val preview = page.indexOf(resource("preview.js").trim().take(100))
        assertTrue(host in 0 until renderer)
        assertTrue(renderer < preview)
    }

    fun `test sets every colour variable of estorm's page`() {
        val css = ThemeCss.css()
        val names = Regex("""(--[a-z-]+):""").findAll(resource("preview.css")).map { it.groupValues[1] }.toSet()
        assertTrue(names.containsAll(listOf("--bg", "--fg", "--font", "--error-fg")))
        for (name in names) assertTrue("$name in\n$css", Regex("""\Q$name\E: [^;]+;""").containsMatchIn(css))
        assertTrue(ThemeCss.boardTheme() in setOf("light", "dark"))
    }

    fun `test writes text as a JavaScript string`() {
        assertEquals("\"a\\\"b\\\\c\\nd\\u2028e\\u0000\\u003c/script>\"", PreviewHtml.jsString("a\"b\\c\nd e\u0000</script>"))
        assertEquals("estormShow(\"== Sales ==\", \"dark\");", PreviewHtml.show("== Sales ==", "dark"))
    }

    fun `test previews estorm files where the IDE has a browser`() {
        val provider = EstormPreviewProvider()
        val board = myFixture.configureByText("board.estorm", "").virtualFile
        val notes = myFixture.configureByText("notes.txt", "").virtualFile
        assertEquals(JBCefApp.isSupported(), provider.accept(project, board))
        assertFalse(provider.accept(project, notes))
    }
}
