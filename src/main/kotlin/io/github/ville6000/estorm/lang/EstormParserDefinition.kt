package io.github.ville6000.estorm.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 * A flat tree: every token right under the file. The language server knows
 * the board's structure; the IDE only needs tokens, for colours, comments
 * and brackets.
 */
class EstormParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?) = EstormLexer()

    override fun createParser(project: Project?) = PsiParser { root, builder ->
        val file = builder.mark()
        while (!builder.eof()) builder.advanceLexer()
        file.done(root)
        builder.treeBuilt
    }

    override fun getFileNodeType() = FILE

    override fun getCommentTokens() = EstormTokenTypes.COMMENTS

    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

    override fun createElement(node: ASTNode): PsiElement =
        throw UnsupportedOperationException("estorm has no composite elements: ${node.elementType}")

    override fun createFile(viewProvider: FileViewProvider): PsiFile = EstormFile(viewProvider)

    companion object {
        @JvmField
        val FILE = IFileElementType(EstormLanguage)
    }
}

class EstormFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, EstormLanguage) {
    override fun getFileType() = EstormFileType

    override fun toString() = "estorm file"
}
