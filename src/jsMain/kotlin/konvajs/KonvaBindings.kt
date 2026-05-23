package konvajs

// ---- Config interfaces (no JS module, pure Kotlin) ----

external interface NodeConfig {
    var x: Number?
        get() = definedExternally
        set(value) = definedExternally
    var y: Number?
        get() = definedExternally
        set(value) = definedExternally
    var width: Number?
        get() = definedExternally
        set(value) = definedExternally
    var height: Number?
        get() = definedExternally
        set(value) = definedExternally
    var visible: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var listening: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var id: String?
        get() = definedExternally
        set(value) = definedExternally
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var opacity: Number?
        get() = definedExternally
        set(value) = definedExternally
    var draggable: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var offsetX: Number?
        get() = definedExternally
        set(value) = definedExternally
    var offsetY: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ShapeConfig : NodeConfig {
    var fill: String?
        get() = definedExternally
        set(value) = definedExternally
    var stroke: String?
        get() = definedExternally
        set(value) = definedExternally
    var strokeWidth: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ContainerConfig : NodeConfig

external interface StageConfig : ContainerConfig {
    var container: dynamic
        get() = definedExternally
        set(value) = definedExternally
}

external interface LayerConfig : ContainerConfig {
    var clearBeforeDraw: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var hitGraphEnabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var imageSmoothingEnabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface GroupConfig : ContainerConfig

external interface CircleConfig : ShapeConfig {
    var radius: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface RectConfig : ShapeConfig {
    var cornerRadius: dynamic
        get() = definedExternally
        set(value) = definedExternally
}

external interface LineConfig : ShapeConfig {
    var points: Array<Number>?
        get() = definedExternally
        set(value) = definedExternally
    var closed: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var bezier: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var tension: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface TextConfig : ShapeConfig {
    var text: String?
        get() = definedExternally
        set(value) = definedExternally
    var fontFamily: String?
        get() = definedExternally
        set(value) = definedExternally
    var fontSize: Number?
        get() = definedExternally
        set(value) = definedExternally
    var fontStyle: String?
        get() = definedExternally
        set(value) = definedExternally
    var fontVariant: String?
        get() = definedExternally
        set(value) = definedExternally
    var textDecoration: String?
        get() = definedExternally
        set(value) = definedExternally
    var align: String?
        get() = definedExternally
        set(value) = definedExternally
    var verticalAlign: String?
        get() = definedExternally
        set(value) = definedExternally
    var padding: Number?
        get() = definedExternally
        set(value) = definedExternally
    var lineHeight: Number?
        get() = definedExternally
        set(value) = definedExternally
    var wrap: String?
        get() = definedExternally
        set(value) = definedExternally
    var ellipsis: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

// ---- Event types ----

external interface Vector2d {
    var x: Number
    var y: Number
}

external interface KonvaEventObject<EventType> {
    var target: Node<NodeConfig>
    var evt: EventType
    var pointerId: Number
    var type: String
    var cancelBubble: Boolean
}

// ---- Node base (from konva/lib/Node) ----

abstract external class Node<Config : NodeConfig>(config: Config = definedExternally) {
    fun on(evtStr: String, handler: (KonvaEventObject<dynamic>) -> Unit): Node<Config>
    fun off(evtStr: String = definedExternally): Node<Config>
    fun fire(eventType: String, evt: dynamic = definedExternally, bubble: Boolean = definedExternally): Node<Config>
    fun destroy(): Node<Config>
    fun show(): Node<Config>
    fun hide(): Node<Config>
    fun visible(): Boolean
    fun visible(v: Boolean): Node<Config>
    fun opacity(): Number
    fun opacity(v: Number): Node<Config>
    fun x(): Number
    fun x(v: Number): Node<Config>
    fun y(): Number
    fun y(v: Number): Node<Config>
    fun width(): Number
    fun width(v: Number): Node<Config>
    fun height(): Number
    fun height(v: Number): Node<Config>
    fun offsetX(): Number
    fun offsetX(v: Number): Node<Config>
    fun offsetY(): Number
    fun offsetY(v: Number): Node<Config>
    fun draggable(): Boolean
    fun draggable(v: Boolean): Node<Config>
    fun zIndex(): Number
    fun zIndex(v: Number): Node<Config>
    fun moveToBottom(): Boolean
    fun moveToTop(): Boolean
    fun getStage(): Stage?
    fun getLayer(): Layer?
    fun getParent(): Container<ContainerConfig>?
    fun setAttrs(config: dynamic)
    fun getAttr(attr: String): dynamic
    fun setAttr(attr: String, value: dynamic)
    fun getType(): String
    fun getClassName(): String
}

// ---- Container ----

abstract external class Container<Config : ContainerConfig>(config: Config = definedExternally) : Node<Config> {
    fun add(vararg children: dynamic): Container<Config>
    fun removeChildren(): Container<Config>
    fun destroyChildren(): Container<Config>
    fun getChildren(): dynamic
    fun find(selector: String): dynamic
    fun findOne(selector: String): Node<NodeConfig>?
}

// ---- Shape base ----

abstract external class Shape<Config : ShapeConfig>(config: Config = definedExternally) : Node<Config> {
    fun fill(): String
    fun fill(v: String): Shape<Config>
    fun stroke(): String
    fun stroke(v: String): Shape<Config>
    fun strokeWidth(): Number
    fun strokeWidth(v: Number): Shape<Config>
}

