package io.github.ville6000.estorm.lang

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

/** Matches, and closes as you type, the brackets of (aggregates), [externals] and {read models}. */
class EstormBraceMatcher : PairedBraceMatcher {
    override fun getPairs() = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?) = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int) = openingBraceOffset

    private companion object {
        val PAIRS = arrayOf(
            BracePair(EstormTokenTypes.AGGREGATE_OPEN, EstormTokenTypes.AGGREGATE_CLOSE, false),
            BracePair(EstormTokenTypes.EXTERNAL_OPEN, EstormTokenTypes.EXTERNAL_CLOSE, false),
            BracePair(EstormTokenTypes.READ_MODEL_OPEN, EstormTokenTypes.READ_MODEL_CLOSE, false),
        )
    }
}
