package io.github.ville6000.estorm.lang

import com.intellij.lang.Commenter

/** Comment with Line Comment (Ctrl/Cmd+/): estorm has `#` line comments only. */
class EstormCommenter : Commenter {
    override fun getLineCommentPrefix() = "# "

    override fun getBlockCommentPrefix(): String? = null

    override fun getBlockCommentSuffix(): String? = null

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
}
