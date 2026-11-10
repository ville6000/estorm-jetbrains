package io.github.ville6000.estorm

import com.intellij.psi.TokenType
import io.github.ville6000.estorm.lang.EstormTokenTypes as T
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EstormLexerTest {
    private fun tokens(text: String) =
        lex(text).filter { it.type != TokenType.WHITE_SPACE }.map { it.type.toString() to text.substring(it.start, it.end) }

    @Test
    fun `colours a step like the stickies it becomes`() {
        assertEquals(
            listOf(
                "ACTOR" to "Customer", "PUNCT" to ":", "COMMAND" to "Place order", "ARROW" to "->",
                "AGGREGATE_OPEN" to "(", "AGGREGATE" to "Order", "AGGREGATE_CLOSE" to ")", "ARROW" to "->",
                "EVENT" to "OrderPlaced", "PUNCT" to ",", "EVENT" to "OrderRejected",
            ),
            tokens("Customer: Place order -> (Order) -> OrderPlaced, OrderRejected"),
        )
    }

    @Test
    fun `splits the brackets off externals and read models`() {
        assertEquals(
            listOf(
                "KEYWORD" to "then", "COMMAND" to "Charge card", "ARROW" to "->",
                "EXTERNAL_OPEN" to "[", "EXTERNAL" to "Payment provider", "EXTERNAL_CLOSE" to "]", "ARROW" to "->",
                "EVENT" to "PaymentCaptured",
            ),
            tokens("  then Charge card -> [Payment provider] -> PaymentCaptured"),
        )
        assertEquals(
            listOf("READ_MODEL_OPEN" to "{", "READ_MODEL" to "Shopping cart", "READ_MODEL_CLOSE" to "}"),
            tokens("{Shopping cart}"),
        )
        assertEquals(listOf("READ_MODEL_OPEN" to "{", "READ_MODEL_CLOSE" to "}"), tokens("{}"))
        assertEquals(
            listOf("COMMAND" to "Go", "ARROW" to "->", "AGGREGATE_OPEN" to "("),
            tokens("Go -> ("),
        )
    }

    @Test
    fun `leaves text it doesn't know plain`() {
        assertEquals(listOf("TEXT" to "Something", "TEXT" to "(odd)"), tokens("Something (odd)"))
    }

    @Test
    fun `indents with tabs as well as spaces`() {
        assertEquals(
            listOf("KEYWORD" to "after", "SCHEDULE" to "30 minutes", "KEYWORD" to "unless", "EVENT" to "Paid"),
            tokens("\tafter 30 minutes unless Paid"),
        )
    }

    @Test
    fun `reads Windows line ends`() {
        assertEquals(listOf("SECTION" to "== Sales ==", "EVENT" to "OrderPlaced"), tokens("== Sales ==\r\nOrderPlaced\r\n"))
    }

    @Test
    fun `covers every character, in order`() {
        for ((name, text) in boards() + ("empty" to "") + ("blank lines" to "\n\n  \n")) {
            var at = 0
            for (token in lex(text)) {
                assertEquals("$name: token at ${token.start}", at, token.start)
                assertTrue("$name: empty token at ${token.start}", token.end > token.start)
                at = token.end
            }
            assertEquals(name, text.length, at)
        }
    }

    @Test
    fun `restarts only at line starts, and lexes the same from any of them`() {
        for ((name, text) in boards()) {
            val all = lex(text)
            val lineStarts = (listOf(0) + text.indices.filter { text[it] == '\n' }.map { it + 1 }).filter { it < text.length }
            assertEquals(name, lineStarts, all.filter { it.state == 0 }.map { it.start })
            for (start in lineStarts) {
                assertEquals("$name from $start", all.filter { it.start >= start }, lex(text, start))
            }
        }
    }

    @Test
    fun `keeps comments, hotspots, rules and sections whole`() {
        assertEquals(listOf("COMMENT" to "# Customer: Place order -> (Order)"), tokens("# Customer: Place order -> (Order)"))
        assertEquals(listOf("HOTSPOT" to "! What if it fails?"), tokens("  ! What if it fails?"))
        assertEquals(listOf("RULE" to "* Paid within 30 days"), tokens("  * Paid within 30 days"))
        assertEquals(listOf("SECTION" to "== Payments =="), tokens("== Payments =="))
        assertEquals(T.COMMENT, lex("# a").single().type)
    }
}
