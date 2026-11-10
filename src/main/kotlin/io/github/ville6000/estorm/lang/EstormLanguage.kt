package io.github.ville6000.estorm.lang

import com.intellij.lang.Language

object EstormLanguage : Language("estorm") {
    private fun readResolve(): Any = EstormLanguage

    override fun getDisplayName() = "estorm"
}
