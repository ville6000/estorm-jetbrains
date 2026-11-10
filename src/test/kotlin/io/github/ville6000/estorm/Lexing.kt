package io.github.ville6000.estorm

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import io.github.ville6000.estorm.lang.EstormLexer
import io.github.ville6000.estorm.lang.EstormTokenTypes as T
import java.io.File

/** A token from EstormLexer, with the state the lexer reported for it. */
data class Lexed(val type: IElementType, val start: Int, val end: Int, val state: Int)

fun lex(text: CharSequence, start: Int = 0): List<Lexed> {
    val lexer = EstormLexer()
    lexer.start(text, start, text.length, 0)
    val out = mutableListOf<Lexed>()
    while (lexer.tokenType != null) {
        out += Lexed(lexer.tokenType!!, lexer.tokenStart, lexer.tokenEnd, lexer.state)
        lexer.advance()
    }
    return out
}

/** The boards in src/test/resources/grammar, by file name. */
fun boards(): Map<String, String> =
    File("src/test/resources/grammar").listFiles { f -> f.extension == "estorm" }!!
        .sortedBy { it.name }
        .associate { it.name to it.readText() }

/** estorm's name for the token kind TYPE stands for, brackets included in their sticky; null for gaps. */
fun kindOf(type: IElementType): String? = when (type) {
    T.COMMENT -> "comment"
    T.KEYWORD -> "keyword"
    T.ARROW -> "arrow"
    T.PUNCT -> "punct"
    T.SECTION -> "section"
    T.ACTOR -> "actor"
    T.COMMAND -> "command"
    T.AGGREGATE, T.AGGREGATE_OPEN, T.AGGREGATE_CLOSE -> "aggregate"
    T.EXTERNAL, T.EXTERNAL_OPEN, T.EXTERNAL_CLOSE -> "external"
    T.EVENT -> "event"
    T.READ_MODEL, T.READ_MODEL_OPEN, T.READ_MODEL_CLOSE -> "read-model"
    T.SCHEDULE -> "schedule"
    T.HOTSPOT -> "hotspot"
    T.RULE -> "rule"
    T.TEXT, TokenType.WHITE_SPACE -> null
    else -> error("unexpected token type $type")
}
