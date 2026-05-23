@file:JsModule("konva/lib/Stage")
@file:JsNonModule

package konvajs

external class Stage(config: StageConfig) : Container<StageConfig> {
    fun getPointerPosition(): Vector2d?
    fun setPointersPositions(evt: dynamic)
    fun getContainer(): org.w3c.dom.HTMLElement
    fun scaleX(): Number
    fun scaleX(v: Number): Stage
    fun scaleY(): Number
    fun scaleY(v: Number): Stage
    fun position(): Vector2d
    fun position(v: Vector2d): Stage
    fun batchDraw(): Stage
    var content: dynamic
}
