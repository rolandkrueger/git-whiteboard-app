@file:JsModule("konva/lib/shapes/Line")
@file:JsNonModule

package konvajs

external class Line(config: LineConfig = definedExternally) : Shape<LineConfig> {
    fun points(): Array<Number>
    fun points(v: Array<Number>): Line
    fun closed(): Boolean
    fun closed(v: Boolean): Line
    fun tension(): Number
    fun tension(v: Number): Line
}
