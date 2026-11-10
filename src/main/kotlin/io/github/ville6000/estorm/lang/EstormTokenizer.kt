package io.github.ville6000.estorm.lang

/**
 * A port of estorm's src/highlight.ts: splits one line of estorm source into
 * tokens named after the sticky they become on the board. Works line by line,
 * like estorm's parser, and never fails: text it doesn't recognise is left
 * out. TokenParityTest keeps it equal to estorm's own, which the language
 * server and the browser editor use.
 */
object EstormTokenizer {
    /** estorm's TokenKind; [id] is its name there. */
    enum class Kind(val id: String) {
        COMMENT("comment"),
        KEYWORD("keyword"),
        ARROW("arrow"),
        PUNCT("punct"),
        SECTION("section"),
        ACTOR("actor"),
        COMMAND("command"),
        AGGREGATE("aggregate"),
        EXTERNAL("external"),
        EVENT("event"),
        READ_MODEL("read-model"),
        SCHEDULE("schedule"),
        HOTSPOT("hotspot"),
        RULE("rule"),
    }

    /** LINE[from, to) is a [kind]. */
    data class Token(val kind: Kind, val from: Int, val to: Int)

    fun tokenize(line: String): List<Token> {
        val tokens = mutableListOf<Token>()

        /** Adds a token for line[from, to), without surrounding spaces. */
        fun add(kind: Kind, from: Int, to: Int) {
            var f = from
            var t = to
            while (f < t && line[f] == ' ') f++
            while (t > f && line[t - 1] == ' ') t--
            if (f < t) tokens += Token(kind, f, t)
        }

        /** Events from FROM to TO, separated by commas. */
        fun events(from: Int, to: Int) {
            var at = from
            for (comma in (from until to).filter { line[it] == ',' }) {
                add(Kind.EVENT, at, comma)
                add(Kind.PUNCT, comma, comma + 1)
                at = comma + 1
            }
            add(Kind.EVENT, at, to)
        }

        /** COMMAND -> ITEM -> … -> EVENT, or EVENT, EVENT, from START to the end of the line. */
        fun chain(start: Int) {
            val parts = line.substring(start).split("->")
            var at = start
            parts.forEachIndexed { i, part ->
                val item = jsTrim(part)
                val kind = when {
                    i == 0 -> Kind.COMMAND
                    item.startsWith('(') -> Kind.AGGREGATE
                    item.startsWith('[') -> Kind.EXTERNAL
                    else -> Kind.EVENT
                }
                if (kind == Kind.EVENT && i == parts.size - 1) events(at, at + part.length) else add(kind, at, at + part.length)
                at += part.length
                if (i < parts.size - 1) {
                    add(Kind.ARROW, at, at + 2)
                    at += 2
                }
            }
        }

        val start = line.indexOfFirst { !isJsWhitespace(it) }.let { if (it == -1) line.length else it }
        val rest = line.substring(start)
        fun word(w: String) = rest.startsWith("$w ")
        when {
            rest.startsWith('#') -> add(Kind.COMMENT, start, line.length)
            rest.startsWith('!') -> add(Kind.HOTSPOT, start, line.length)
            rest.startsWith('*') -> add(Kind.RULE, start, line.length)
            rest.startsWith('{') -> add(Kind.READ_MODEL, start, line.length)
            rest.startsWith("==") -> add(Kind.SECTION, start, line.length)
            word("then") -> {
                add(Kind.KEYWORD, start, start + 4)
                chain(start + 5)
            }
            word("when") -> {
                add(Kind.KEYWORD, start, start + 4)
                events(start + 5, line.length)
            }
            word("after") -> {
                add(Kind.KEYWORD, start, start + 5)
                val unless = line.indexOf(" unless ", start + 5)
                if (unless == -1) {
                    add(Kind.SCHEDULE, start + 6, line.length)
                } else {
                    add(Kind.SCHEDULE, start + 6, unless)
                    add(Kind.KEYWORD, unless + 1, unless + 7)
                    add(Kind.EVENT, unless + 8, line.length)
                }
            }
            word("every") -> {
                add(Kind.KEYWORD, start, start + 5)
                val colon = line.lastIndexOf(':')
                if (colon < start + 6) {
                    add(Kind.SCHEDULE, start + 6, line.length)
                } else {
                    add(Kind.SCHEDULE, start + 6, colon)
                    add(Kind.PUNCT, colon, colon + 1)
                    chain(colon + 1)
                }
            }
            else -> {
                val colon = line.indexOf(':', start)
                if (colon != -1) {
                    add(Kind.ACTOR, start, colon)
                    add(Kind.PUNCT, colon, colon + 1)
                    chain(colon + 1)
                } else if ("->" in rest) {
                    chain(start)
                } else if (rest.none { it in "()[]{}" }) {
                    add(Kind.EVENT, start, line.length)
                }
            }
        }
        return tokens
    }

    /** What JavaScript's trim() removes: its WhiteSpace and LineTerminator characters. */
    private fun isJsWhitespace(c: Char) =
        c in "\t\u000B\u000C  ﻿\n\r  " || Character.getType(c) == Character.SPACE_SEPARATOR.toInt()

    private fun jsTrim(s: String) = s.trim(::isJsWhitespace)
}
