package io.github.ville6000.estorm.lang

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import io.github.ville6000.estorm.lang.EstormTokenizer.Kind

class EstormTokenType(debugName: String) : IElementType(debugName, EstormLanguage)

/**
 * One token type per estorm token kind, plus the brackets of aggregates,
 * externals and read models, which EstormBraceMatcher pairs, and TEXT for
 * what the tokenizer leaves out.
 */
object EstormTokenTypes {
    @JvmField val COMMENT = EstormTokenType("COMMENT")
    @JvmField val KEYWORD = EstormTokenType("KEYWORD")
    @JvmField val ARROW = EstormTokenType("ARROW")
    @JvmField val PUNCT = EstormTokenType("PUNCT")
    @JvmField val SECTION = EstormTokenType("SECTION")
    @JvmField val ACTOR = EstormTokenType("ACTOR")
    @JvmField val COMMAND = EstormTokenType("COMMAND")
    @JvmField val AGGREGATE = EstormTokenType("AGGREGATE")
    @JvmField val EXTERNAL = EstormTokenType("EXTERNAL")
    @JvmField val EVENT = EstormTokenType("EVENT")
    @JvmField val READ_MODEL = EstormTokenType("READ_MODEL")
    @JvmField val SCHEDULE = EstormTokenType("SCHEDULE")
    @JvmField val HOTSPOT = EstormTokenType("HOTSPOT")
    @JvmField val RULE = EstormTokenType("RULE")

    @JvmField val AGGREGATE_OPEN = EstormTokenType("AGGREGATE_OPEN")
    @JvmField val AGGREGATE_CLOSE = EstormTokenType("AGGREGATE_CLOSE")
    @JvmField val EXTERNAL_OPEN = EstormTokenType("EXTERNAL_OPEN")
    @JvmField val EXTERNAL_CLOSE = EstormTokenType("EXTERNAL_CLOSE")
    @JvmField val READ_MODEL_OPEN = EstormTokenType("READ_MODEL_OPEN")
    @JvmField val READ_MODEL_CLOSE = EstormTokenType("READ_MODEL_CLOSE")

    /** Text the tokenizer leaves out, such as a line with brackets but no arrow. */
    @JvmField val TEXT = EstormTokenType("TEXT")

    @JvmField val COMMENTS = TokenSet.create(COMMENT)

    fun of(kind: Kind): IElementType = when (kind) {
        Kind.COMMENT -> COMMENT
        Kind.KEYWORD -> KEYWORD
        Kind.ARROW -> ARROW
        Kind.PUNCT -> PUNCT
        Kind.SECTION -> SECTION
        Kind.ACTOR -> ACTOR
        Kind.COMMAND -> COMMAND
        Kind.AGGREGATE -> AGGREGATE
        Kind.EXTERNAL -> EXTERNAL
        Kind.EVENT -> EVENT
        Kind.READ_MODEL -> READ_MODEL
        Kind.SCHEDULE -> SCHEDULE
        Kind.HOTSPOT -> HOTSPOT
        Kind.RULE -> RULE
    }

    /** The brackets of a sticky KIND written in them: open char, close char, their token types. */
    class Brackets(val open: Char, val close: Char, val openType: IElementType, val closeType: IElementType)

    fun brackets(kind: Kind): Brackets? = when (kind) {
        Kind.AGGREGATE -> Brackets('(', ')', AGGREGATE_OPEN, AGGREGATE_CLOSE)
        Kind.EXTERNAL -> Brackets('[', ']', EXTERNAL_OPEN, EXTERNAL_CLOSE)
        Kind.READ_MODEL -> Brackets('{', '}', READ_MODEL_OPEN, READ_MODEL_CLOSE)
        else -> null
    }
}
