package io.github.ville6000.estorm

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * The lexer must colour every line of the test boards like estorm's own
 * tokenizer, which the language server and the browser editor use.
 * build/golden/tokens.txt is what that tokenizer makes of them
 * (js/tokens.mjs, run by Gradle before the tests).
 */
class TokenParityTest {
    @Test
    fun `tokenizes the test boards like estorm`() {
        val golden = File(System.getProperty("estorm.goldenTokens") ?: "build/golden/tokens.txt").readText()
        assertEquals(golden, boards().entries.joinToString("") { (name, text) -> "== $name\n" + describe(text) })
    }

    /** Each line as "<n>: <kind> <from>-<to> ...", the brackets back in their sticky, without the gaps. */
    private fun describe(text: String): String {
        val lines = text.split(Regex("\r?\n"))
        val lineStarts = lines.runningFold(0) { at, line -> at + line.length + 1 }
        val merged = mutableListOf<Triple<String, Int, Int>>()
        for (token in lex(text)) {
            val kind = kindOf(token.type) ?: continue
            val last = merged.lastOrNull()
            if (last != null && last.first == kind && last.third == token.start) {
                merged[merged.lastIndex] = Triple(kind, last.second, token.end)
            } else {
                merged += Triple(kind, token.start, token.end)
            }
        }
        val byLine = merged.groupBy { (_, start, _) -> lineStarts.indexOfLast { it <= start } }
        return lines.indices.joinToString("") { i ->
            val tokens = byLine[i].orEmpty().joinToString("") { (kind, start, end) ->
                " $kind ${start - lineStarts[i]}-${end - lineStarts[i]}"
            }
            "${i + 1}:$tokens\n"
        }
    }
}
