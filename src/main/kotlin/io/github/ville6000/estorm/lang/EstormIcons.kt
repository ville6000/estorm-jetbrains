package io.github.ville6000.estorm.lang

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object EstormIcons {
    /** A sticky: the icon of .estorm files and of the language server in the status bar. */
    @JvmField
    val FILE: Icon = IconLoader.getIcon("/icons/estorm.svg", EstormIcons::class.java)
}
