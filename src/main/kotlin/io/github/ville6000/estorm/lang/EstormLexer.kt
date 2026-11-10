package io.github.ville6000.estorm.lang

import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

/**
 * Lexes estorm a line at a time with EstormTokenizer, filling what it leaves
 * out with WHITE_SPACE and TEXT so that every character is in a token, and
 * splitting the brackets off aggregates, externals and read models.
 *
 * A line's tokens depend on how the line starts (`then`, `when`, `Actor:`),
 * so the state is 0 only for the first token of a line: the editor restarts
 * the lexer only at such tokens, that is, at line starts.
 */
class EstormLexer : LexerBase() {
    private class Piece(val type: IElementType, val start: Int, val end: Int)

    private var buffer: CharSequence = ""
    private var bufferEnd = 0

    /** The current line's tokens, its newline included. */
    private val pieces = ArrayList<Piece>()
    private var index = 0

    /** Where the next line starts. */
    private var nextLine = 0

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        bufferEnd = endOffset
        lexLine(startOffset)
    }

    override fun getState() = if (index == 0) 0 else 1

    override fun getTokenType(): IElementType? = pieces.getOrNull(index)?.type

    override fun getTokenStart() = pieces.getOrNull(index)?.start ?: bufferEnd

    override fun getTokenEnd() = pieces.getOrNull(index)?.end ?: bufferEnd

    override fun advance() {
        index++
        if (index >= pieces.size && nextLine < bufferEnd) lexLine(nextLine)
    }

    override fun getBufferSequence() = buffer

    override fun getBufferEnd() = bufferEnd

    private fun lexLine(start: Int) {
        pieces.clear()
        index = 0
        if (start >= bufferEnd) {
            nextLine = bufferEnd
            return
        }
        var lineEnd = start
        while (lineEnd < bufferEnd && buffer[lineEnd] != '\n') lineEnd++
        nextLine = if (lineEnd < bufferEnd) lineEnd + 1 else bufferEnd
        val textEnd = if (lineEnd > start && buffer[lineEnd - 1] == '\r') lineEnd - 1 else lineEnd

        var at = start
        for (token in EstormTokenizer.tokenize(buffer.subSequence(start, textEnd).toString())) {
            val from = start + token.from
            val to = start + token.to
            if (from < at) continue
            gap(at, from)
            sticky(token.kind, from, to)
            at = to
        }
        gap(at, nextLine)
    }

    /** A token of KIND over [from, to), its brackets as their own tokens. */
    private fun sticky(kind: EstormTokenizer.Kind, from: Int, to: Int) {
        val type = EstormTokenTypes.of(kind)
        val brackets = EstormTokenTypes.brackets(kind)
        if (brackets == null) {
            pieces += Piece(type, from, to)
            return
        }
        var start = from
        var end = to
        val opens = buffer[start] == brackets.open
        if (opens) start++
        val closes = end > start && buffer[end - 1] == brackets.close
        if (closes) end--
        if (opens) pieces += Piece(brackets.openType, from, from + 1)
        if (end > start) pieces += Piece(type, start, end)
        if (closes) pieces += Piece(brackets.closeType, end, to)
    }

    /** [from, to), outside any token: runs of WHITE_SPACE and TEXT. */
    private fun gap(from: Int, to: Int) {
        var at = from
        while (at < to) {
            val space = isSpace(buffer[at])
            var end = at + 1
            while (end < to && isSpace(buffer[end]) == space) end++
            pieces += Piece(if (space) TokenType.WHITE_SPACE else EstormTokenTypes.TEXT, at, end)
            at = end
        }
    }

    private fun isSpace(c: Char) = c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\u000C'
}
