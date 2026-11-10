package io.github.ville6000.estorm.lang

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import io.github.ville6000.estorm.lang.EstormTokenTypes as T

class EstormSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer() = EstormLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
        pack(KEYS[tokenType])

    private companion object {
        val KEYS: Map<IElementType, TextAttributesKey> = mapOf(
            T.COMMENT to EstormColors.COMMENT,
            T.KEYWORD to EstormColors.KEYWORD,
            T.ARROW to EstormColors.OPERATOR,
            T.PUNCT to EstormColors.OPERATOR,
            T.SECTION to EstormColors.SECTION,
            T.ACTOR to EstormColors.ACTOR,
            T.COMMAND to EstormColors.COMMAND,
            T.AGGREGATE to EstormColors.AGGREGATE,
            T.AGGREGATE_OPEN to EstormColors.AGGREGATE,
            T.AGGREGATE_CLOSE to EstormColors.AGGREGATE,
            T.EXTERNAL to EstormColors.EXTERNAL,
            T.EXTERNAL_OPEN to EstormColors.EXTERNAL,
            T.EXTERNAL_CLOSE to EstormColors.EXTERNAL,
            T.EVENT to EstormColors.EVENT,
            T.READ_MODEL to EstormColors.READ_MODEL,
            T.READ_MODEL_OPEN to EstormColors.READ_MODEL,
            T.READ_MODEL_CLOSE to EstormColors.READ_MODEL,
            T.SCHEDULE to EstormColors.SCHEDULE,
            T.HOTSPOT to EstormColors.HOTSPOT,
            T.RULE to EstormColors.RULE,
        )
    }
}

class EstormSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter =
        EstormSyntaxHighlighter()
}
