@file:JsModule("konva/lib/shapes/Circle")
@file:JsNonModule

package konvajs

external class Circle(config: CircleConfig = definedExternally) : Shape<CircleConfig> {
    fun radius(): Number
    fun radius(v: Number): Circle
}
