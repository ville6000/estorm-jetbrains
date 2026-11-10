package io.github.ville6000.estorm.lang

import com.intellij.openapi.fileTypes.LanguageFileType

object EstormFileType : LanguageFileType(EstormLanguage) {
    override fun getName() = "estorm"

    override fun getDescription() = "estorm board"

    override fun getDefaultExtension() = "estorm"

    override fun getIcon() = EstormIcons.FILE
}
