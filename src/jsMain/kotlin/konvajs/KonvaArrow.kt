@file:JsModule("konva/lib/shapes/Arrow")
@file:JsNonModule

package konvajs

external class Arrow(config: ArrowConfig = definedExternally) : Shape<ArrowConfig> {
    fun points(): Array<Number>
    fun points(v: Array<Number>): Arrow
}
