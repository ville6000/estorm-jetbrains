package io.github.ville6000.estorm.lang

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Default
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

/**
 * A colour per sticky kind, in Settings | Editor | Color Scheme | estorm.
 * Each falls back to the standard colour the VS Code extension maps it to,
 * so any scheme colours them. Hotspots and rules have none, so they default
 * to the board's red and amber (colorSchemes/).
 */
object EstormColors {
    @JvmField val COMMENT = createTextAttributesKey("ESTORM_COMMENT", Default.LINE_COMMENT)
    @JvmField val KEYWORD = createTextAttributesKey("ESTORM_KEYWORD", Default.KEYWORD)
    @JvmField val OPERATOR = createTextAttributesKey("ESTORM_OPERATOR", Default.OPERATION_SIGN)
    @JvmField val SECTION = createTextAttributesKey("ESTORM_SECTION", Default.LABEL)
    @JvmField val ACTOR = createTextAttributesKey("ESTORM_ACTOR", Default.LOCAL_VARIABLE)
    @JvmField val COMMAND = createTextAttributesKey("ESTORM_COMMAND", Default.FUNCTION_DECLARATION)
    @JvmField val AGGREGATE = createTextAttributesKey("ESTORM_AGGREGATE", Default.CLASS_NAME)
    @JvmField val EXTERNAL = createTextAttributesKey("ESTORM_EXTERNAL", Default.INTERFACE_NAME)
    @JvmField val EVENT = createTextAttributesKey("ESTORM_EVENT", Default.STRING)
    @JvmField val READ_MODEL = createTextAttributesKey("ESTORM_READ_MODEL", Default.INSTANCE_FIELD)
    @JvmField val SCHEDULE = createTextAttributesKey("ESTORM_SCHEDULE", Default.NUMBER)
    @JvmField val HOTSPOT = createTextAttributesKey("ESTORM_HOTSPOT")
    @JvmField val RULE = createTextAttributesKey("ESTORM_RULE")

    /** Name in the settings, key; in the order a flow reads, as in the board's legend. */
    val ALL: List<Pair<String, TextAttributesKey>> = listOf(
        "Section" to SECTION,
        "Actor" to ACTOR,
        "Command" to COMMAND,
        "Aggregate" to AGGREGATE,
        "External system" to EXTERNAL,
        "Event" to EVENT,
        "Read model" to READ_MODEL,
        "Schedule" to SCHEDULE,
        "Hotspot" to HOTSPOT,
        "Rule" to RULE,
        "Keyword" to KEYWORD,
        "Arrow and punctuation" to OPERATOR,
        "Comment" to COMMENT,
    )
}
