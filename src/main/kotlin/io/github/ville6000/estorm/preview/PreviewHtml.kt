package io.github.ville6000.estorm.preview

/**
 * The preview's page: estorm's preview page (preview.css, preview.js) with
 * this plugin's host.js, all inlined so the page needs no files and makes no
 * network requests. See "Embedding the preview" in estorm's EDITORS.md.
 */
object PreviewHtml {
    /** The page; POST(expression) is JavaScript that sends the string EXPRESSION evaluates to the IDE. */
    fun page(themeCss: String, post: (String) -> String): String = """
        <!doctype html>
        <html lang="en">
        <head>
        <meta charset="utf-8">
        <meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline'; img-src data:; script-src 'unsafe-inline'">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style>${style(resource("preview.css"))}</style>
        <style id="$THEME_ID">${style(themeCss)}</style>
        </head>
        <body>
        <script>
        // To the IDE as "ready" or "reveal:<line>" (EstormPreviewEditor.receive).
        window.estormHost = {
          post(msg) {
            const message = msg.type === 'reveal' ? 'reveal:' + msg.line : msg.type;
            ${post("message")}
          },
        };
        </script>
        <script>${script(resource("host.js"))}</script>
        <script>${script(resource("preview.js"))}</script>
        </body>
        </html>
    """.trimIndent()

    /** JavaScript that shows TEXT with svg()'s THEME ('light' or 'dark'). */
    fun show(text: String, theme: String) = "estormShow(${jsString(text)}, ${jsString(theme)});"

    /** JavaScript that replaces the theme's CSS variables. */
    fun setTheme(themeCss: String) = "document.getElementById('$THEME_ID').textContent = ${jsString(themeCss)};"

    /** S as a JavaScript string literal. */
    fun jsString(s: String): String = buildString(s.length + 2) {
        append('"')
        for (c in s) {
            when (c) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                '<' -> append("\\u003c")
                else -> if (c < ' ' || c == ' ' || c == ' ') append("\\u%04x".format(c.code)) else append(c)
            }
        }
        append('"')
    }

    private const val THEME_ID = "estorm-theme"

    private fun resource(name: String): String =
        PreviewHtml::class.java.getResourceAsStream("/preview/$name")?.use { String(it.readAllBytes(), Charsets.UTF_8) }
            ?: error("/preview/$name is missing from the plugin: build it with `npm run build`")

    /** CODE, safe inside <script>: only an end tag ends the element. */
    private fun script(code: String) = code.replace("</script", "<\\/script", ignoreCase = true)

    /** CSS, safe inside <style>. */
    private fun style(css: String) = css.replace("</style", "<\\/style", ignoreCase = true)
}
