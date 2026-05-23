@file:JsModule("konva/lib/shapes/Text")
@file:JsNonModule

package konvajs

external class Text(config: TextConfig = definedExternally) : Shape<TextConfig> {
    fun text(): String
    fun text(v: String): Text
    fun fontSize(): Number
    fun fontSize(v: Number): Text
    fun fontFamily(): String
    fun fontFamily(v: String): Text
    fun fontStyle(): String
    fun fontStyle(v: String): Text
    fun align(): String
    fun align(v: String): Text
    fun getTextWidth(): Number
    fun getTextHeight(): Number
    fun setText(text: String): Text
}
