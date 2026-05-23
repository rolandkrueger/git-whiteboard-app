@file:JsModule("konva/lib/shapes/Rect")
@file:JsNonModule

package konvajs

external class Rect(config: RectConfig = definedExternally) : Shape<RectConfig> {
    fun cornerRadius(): dynamic
    fun cornerRadius(v: Number): Rect
    fun cornerRadius(v: Array<Number>): Rect
}
