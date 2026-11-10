package io.github.ville6000.estorm.lang

import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage

/** Settings | Editor | Color Scheme | estorm. */
class EstormColorSettingsPage : ColorSettingsPage {
    override fun getDisplayName() = "estorm"

    override fun getIcon() = EstormIcons.FILE

    override fun getHighlighter() = EstormSyntaxHighlighter()

    override fun getAttributeDescriptors() =
        EstormColors.ALL.map { (name, key) -> AttributesDescriptor(name, key) }.toTypedArray()

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getAdditionalHighlightingTagToDescriptorMap() = null

    override fun getDemoText() = DEMO

    private companion object {
        val DEMO = """
            # Web shop: from checkout to shipping

            == Sales ==
            ! Do we support guest checkout?
            {Shopping cart}
            Customer: Place order -> (Order) -> OrderPlaced
              after 30 minutes unless PaymentCaptured
                then Cancel unpaid order -> (Order) -> OrderCancelled

            == Payments ==
            when OrderPlaced
              then Charge card -> [Payment provider] -> PaymentCaptured, PaymentDeclined
              * A card is charged once per order

            every night: Settle payments -> [Payment provider] -> PaymentsSettled
        """.trimIndent()
    }
}
