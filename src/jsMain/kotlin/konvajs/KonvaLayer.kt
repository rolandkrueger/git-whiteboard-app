@file:JsModule("konva/lib/Layer")
@file:JsNonModule

package konvajs

external class Layer(config: LayerConfig = definedExternally) : Container<LayerConfig> {
    fun batchDraw(): Layer
    fun draw(): Layer
}
