package io.github.ville6000.estorm.preview

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.NamedColorUtil
import com.intellij.util.ui.UIUtil
import java.awt.Color

/**
 * The IDE's colours as the CSS variables of estorm's preview page, so the
 * page around the board follows the theme. The board follows the editor's
 * colour scheme: light or dark.
 */
object ThemeCss {
    fun css(): String {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val vars = linkedMapOf(
            "--bg" to scheme.defaultBackground,
            "--fg" to scheme.defaultForeground,
            "--button-bg" to UIUtil.getPanelBackground(),
            "--button-fg" to UIUtil.getLabelForeground(),
            "--button-hover" to JBUI.CurrentTheme.ActionButton.hoverBackground(),
            "--border" to JBColor.border(),
            "--focus" to JBUI.CurrentTheme.Focus.focusColor(),
            "--error-bg" to JBUI.CurrentTheme.Validator.errorBackgroundColor(),
            "--error-fg" to NamedColorUtil.getErrorForeground(),
            "--error-border" to JBUI.CurrentTheme.Validator.errorBorderColor(),
        ).mapValues { (_, color) -> hex(color) }
        val font = JBFont.label().family.replace("\"", "")
        return ":root {\n" +
            vars.entries.joinToString("") { (name, value) -> "  $name: $value;\n" } +
            "  --font: \"$font\", system-ui, sans-serif;\n}\n"
    }

    /** The editor's background, for the browser to show before the page draws. */
    fun background(): String = hex(EditorColorsManager.getInstance().globalScheme.defaultBackground)

    /** The theme for svg(): 'dark' when the editor's background is. */
    fun boardTheme(): String =
        if (ColorUtil.isDark(EditorColorsManager.getInstance().globalScheme.defaultBackground)) "dark" else "light"

    private fun hex(color: Color) = "#%02x%02x%02x".format(color.red, color.green, color.blue)
}
