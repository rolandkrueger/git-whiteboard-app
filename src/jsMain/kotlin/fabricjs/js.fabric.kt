@file:JsModule("fabric")
@file:JsNonModule

package fabricjs

import org.w3c.dom.events.Event

external class fabric {

    open class Object : IObjectOptions, IObservable<Any> {
        override var cornerStyle: String
            get() = definedExternally
            set(value) = definedExternally

        override fun on(eventName: String, handler: (e: IEvent) -> Unit): Any
        override fun trigger(eventName: String, options: Any): Any
        override fun off(eventName: String, handler: (e: IEvent) -> Unit): Any
        override fun off(eventName: Any, handler: (e: IEvent) -> Unit): Any
        override fun off(): Any
        fun set(key: String, value: String)
    }

    class Canvas(id: String) : Object, ICanvasOptions {
        fun add(element: dynamic)
        fun clear(): Canvas
        fun requestRenderAll(): Canvas
        fun sendToBack(obj: Any): Canvas
        fun bringToFront(obj: Any): Canvas
        fun sendBackwards(obj: Any, intersecting: Boolean = definedExternally): Canvas
        fun bringForward(obj: Any, intersecting: Boolean = definedExternally): Canvas
        fun getZoom(): Double
        fun setZoom(value: Double): Canvas
    }

    interface IObservable<T> {
        fun on(eventName: String, handler: (e: IEvent) -> Unit): T
        fun trigger(eventName: String, options: Any = definedExternally): T
        fun off(eventName: String = definedExternally, handler: (e: IEvent) -> Unit = definedExternally): T
        fun off(eventName: Any = definedExternally, handler: (e: IEvent) -> Unit = definedExternally): T
        fun off(): T
    }

    interface IEvent {
        var e: Event
        var target: Any?
            get() = definedExternally
            set(value) = definedExternally
        var subTargets: Array<Any>?
            get() = definedExternally
            set(value) = definedExternally
        var button: Number?
            get() = definedExternally
            set(value) = definedExternally
        var isClick: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var pointer: Point?
            get() = definedExternally
            set(value) = definedExternally
        var absolutePointer: Point?
            get() = definedExternally
            set(value) = definedExternally
    }

    class Rect : Object, IEllipseOptions
    class Circle : Object, ICircleOptions
    class Line(points: Array<Number> = definedExternally) : Object, ILineOptions

    class Triangle : Object

    class Text(text: String) : Object, TextOptions {
        override var fontStyle: String
            get() = definedExternally
            set(value) = definedExternally
    }

    interface IObjectOptions {
        var hasControls: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var hasBorders: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var selectable: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var hoverCursor: String?
            get() = definedExternally
            set(value) = definedExternally
        var type: String?
            get() = definedExternally
            set(value) = definedExternally
        var originX: String?
            get() = definedExternally
            set(value) = definedExternally
        var originY: String?
            get() = definedExternally
            set(value) = definedExternally
        var top: Number?
            get() = definedExternally
            set(value) = definedExternally
        var left: Number?
            get() = definedExternally
            set(value) = definedExternally
        var width: Number?
            get() = definedExternally
            set(value) = definedExternally
        var height: Number?
            get() = definedExternally
            set(value) = definedExternally
        var scaleX: Number?
            get() = definedExternally
            set(value) = definedExternally
        var scaleY: Number?
            get() = definedExternally
            set(value) = definedExternally
        var flipX: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var flipY: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var opacity: Number?
            get() = definedExternally
            set(value) = definedExternally
        var angle: Number?
            get() = definedExternally
            set(value) = definedExternally
        var skewX: Number?
            get() = definedExternally
            set(value) = definedExternally
        var skewY: Number?
            get() = definedExternally
            set(value) = definedExternally
        var cornerSize: Number?
            get() = definedExternally
            set(value) = definedExternally
        var transparentCorners: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var moveCursor: String?
            get() = definedExternally
            set(value) = definedExternally
        var padding: Number?
            get() = definedExternally
            set(value) = definedExternally
        var borderColor: String?
            get() = definedExternally
            set(value) = definedExternally
        var borderDashArray: Array<Number>?
            get() = definedExternally
            set(value) = definedExternally
        var cornerColor: String?
            get() = definedExternally
            set(value) = definedExternally
        var cornerStrokeColor: String?
            get() = definedExternally
            set(value) = definedExternally
        var cornerStyle: String /* "rect" | "circle" */
        var cornerDashArray: Array<Number>?
            get() = definedExternally
            set(value) = definedExternally
        var centeredScaling: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var centeredRotation: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var fill: dynamic /* String | Pattern | Gradient */
            get() = definedExternally
            set(value) = definedExternally
        var fillRule: String?
            get() = definedExternally
            set(value) = definedExternally
        var globalCompositeOperation: String?
            get() = definedExternally
            set(value) = definedExternally
        var selectionBackgroundColor: String?
            get() = definedExternally
            set(value) = definedExternally
        var stroke: String?
            get() = definedExternally
            set(value) = definedExternally
        var strokeWidth: Number?
            get() = definedExternally
            set(value) = definedExternally
        var strokeDashArray: Array<Number>?
            get() = definedExternally
            set(value) = definedExternally
        var strokeDashOffset: Number?
            get() = definedExternally
            set(value) = definedExternally
        var strokeLineCap: String?
            get() = definedExternally
            set(value) = definedExternally
        var strokeLineJoin: String?
            get() = definedExternally
            set(value) = definedExternally
        var strokeMiterLimit: Number?
            get() = definedExternally
            set(value) = definedExternally
        var shadow: dynamic /* Shadow | String */
            get() = definedExternally
            set(value) = definedExternally
        var borderOpacityWhenMoving: Number?
            get() = definedExternally
            set(value) = definedExternally
        var borderScaleFactor: Number?
            get() = definedExternally
            set(value) = definedExternally
        var transformMatrix: Array<Any>?
            get() = definedExternally
            set(value) = definedExternally
        var minScaleLimit: Number?
            get() = definedExternally
            set(value) = definedExternally
        var evented: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var visible: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var hasRotatingPoint: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var rotatingPointOffset: Number?
            get() = definedExternally
            set(value) = definedExternally
        var perPixelTargetFind: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var includeDefaultValues: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var lockMovementX: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var lockMovementY: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var lockRotation: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var lockScalingX: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var lockScalingY: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var lockUniScaling: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var lockSkewingX: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var lockSkewingY: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var lockScalingFlip: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var excludeFromExport: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var objectCaching: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var statefullCache: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var noScaleCache: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var strokeUniform: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var dirty: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var paintFirst: String?
            get() = definedExternally
            set(value) = definedExternally
        var stateProperties: Array<String>?
            get() = definedExternally
            set(value) = definedExternally
        var cacheProperties: Array<String>?
            get() = definedExternally
            set(value) = definedExternally
        var clipPath: Any?
            get() = definedExternally
            set(value) = definedExternally
        var inverted: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var absolutePositioned: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var name: String?
            get() = definedExternally
            set(value) = definedExternally
        var data: Any?
            get() = definedExternally
            set(value) = definedExternally
        var matrixCache: Any?
            get() = definedExternally
            set(value) = definedExternally
        var ownMatrixCache: Any?
            get() = definedExternally
            set(value) = definedExternally
        var snapAngle: Number?
            get() = definedExternally
            set(value) = definedExternally
        var snapThreshold: Number?
            get() = definedExternally
            set(value) = definedExternally
        var canvas: Canvas?
            get() = definedExternally
            set(value) = definedExternally
    }

    interface IEllipseOptions : IObjectOptions {
        var rx: Int?
            get() = definedExternally
            set(value) = definedExternally
        var ry: Int?
            get() = definedExternally
            set(value) = definedExternally
    }

    interface ICircleOptions : IObjectOptions {
        var radius: Number?
            get() = definedExternally
            set(value) = definedExternally
        var startAngle: Number?
            get() = definedExternally
            set(value) = definedExternally
        var endAngle: Number?
            get() = definedExternally
            set(value) = definedExternally
    }

    interface ILineOptions : IObjectOptions {
        var x1: Number?
            get() = definedExternally
            set(value) = definedExternally
        var x2: Number?
            get() = definedExternally
            set(value) = definedExternally
        var y1: Number?
            get() = definedExternally
            set(value) = definedExternally
        var y2: Number?
            get() = definedExternally
            set(value) = definedExternally
    }

    interface TextOptions : IObjectOptions {
        override var type: String?
            get() = definedExternally
            set(value) = definedExternally
        var fontSize: Number?
            get() = definedExternally
            set(value) = definedExternally
        var fontWeight: dynamic /* String | Number */
            get() = definedExternally
            set(value) = definedExternally
        var fontFamily: String?
            get() = definedExternally
            set(value) = definedExternally
        var underline: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var overline: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var linethrough: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var textAlign: String?
            get() = definedExternally
            set(value) = definedExternally
        var fontStyle: String /* '' | 'normal' | 'italic' | 'oblique' */
        var lineHeight: Number?
            get() = definedExternally
            set(value) = definedExternally
        var textBackgroundColor: String?
            get() = definedExternally
            set(value) = definedExternally
        override var stroke: String?
            get() = definedExternally
            set(value) = definedExternally
        override var shadow: dynamic /* Shadow | String */
            get() = definedExternally
            set(value) = definedExternally
        var charSpacing: Number?
            get() = definedExternally
            set(value) = definedExternally
        var styles: Any?
            get() = definedExternally
            set(value) = definedExternally
        var deltaY: Number?
            get() = definedExternally
            set(value) = definedExternally
        var text: String?
            get() = definedExternally
            set(value) = definedExternally
        override var cacheProperties: Array<String>?
            get() = definedExternally
            set(value) = definedExternally
        override var stateProperties: Array<String>?
            get() = definedExternally
            set(value) = definedExternally
    }

    open class Point(x: Int, y: Int) {
        open var x: Int
        open var y: Int
        open var type: String
        open fun add(that: Point): Point
        open fun addEquals(that: Point): Point
        open fun scalarAdd(scalar: Number): Point
        open fun scalarAddEquals(scalar: Number): Point
        open fun subtract(that: Point): Point
        open fun subtractEquals(that: Point): Point
        open fun scalarSubtract(scalar: Number): Point
        open fun scalarSubtractEquals(scalar: Number): Point
        open fun multiply(scalar: Number): Point
        open fun multiplyEquals(scalar: Number): Point
        open fun divide(scalar: Number): Point
        open fun divideEquals(scalar: Number): Point
        open fun eq(that: Point): Point
        open fun lt(that: Point): Point
        open fun lte(that: Point): Point
        open fun gt(that: Point): Point
        open fun gte(that: Point): Point
        open fun lerp(that: Point, t: Number): Point
        open fun distanceFrom(that: Point): Number
        open fun midPointFrom(that: Point): Point
        open fun min(that: Point): Point
        open fun max(that: Point): Point
        override fun toString(): String
        open fun setXY(x: Number, y: Number): Point
        open fun setX(x: Number): Point
        open fun setY(y: Number): Point
        open fun setFromPoint(that: Point): Point
        open fun swap(that: Point): Point
        open fun clone(): Point
    }

    interface IStaticCanvasOptions {
        var backgroundColor: dynamic /* String | Pattern */
            get() = definedExternally
            set(value) = definedExternally
        var backgroundImage: dynamic /* Image | String */
            get() = definedExternally
            set(value) = definedExternally
        var overlayColor: dynamic /* String | Pattern */
            get() = definedExternally
            set(value) = definedExternally
        var stateful: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var renderOnAddRemove: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var controlsAboveOverlay: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var allowTouchScrolling: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var imageSmoothingEnabled: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var viewportTransform: Array<Int>?
            get() = definedExternally
            set(value) = definedExternally
        var backgroundVpt: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var overlayVpt: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var enableRetinaScaling: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var skipOffscreen: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var svgViewportTransformation: Boolean?
            get() = definedExternally
            set(value) = definedExternally
    }

    interface ICanvasOptions : IStaticCanvasOptions {
        var uniScaleTransform: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var uniScaleKey: String?
            get() = definedExternally
            set(value) = definedExternally
        var centeredKey: String?
            get() = definedExternally
            set(value) = definedExternally
        var altActionKey: String?
            get() = definedExternally
            set(value) = definedExternally
        var interactive: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var selection: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var selectionKey: dynamic /* String | Array<String> */
            get() = definedExternally
            set(value) = definedExternally
        var altSelectionKey: String?
            get() = definedExternally
            set(value) = definedExternally
        var selectionColor: String?
            get() = definedExternally
            set(value) = definedExternally
        var selectionDashArray: Array<Number>?
            get() = definedExternally
            set(value) = definedExternally
        var selectionBorderColor: String?
            get() = definedExternally
            set(value) = definedExternally
        var selectionLineWidth: Number?
            get() = definedExternally
            set(value) = definedExternally
        var selectionFullyContained: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var defaultCursor: String?
            get() = definedExternally
            set(value) = definedExternally
        var freeDrawingCursor: String?
            get() = definedExternally
            set(value) = definedExternally
        var rotationCursor: String?
            get() = definedExternally
            set(value) = definedExternally
        var notAllowedCursor: String?
            get() = definedExternally
            set(value) = definedExternally
        var containerClass: String?
            get() = definedExternally
            set(value) = definedExternally
        var targetFindTolerance: Number?
            get() = definedExternally
            set(value) = definedExternally
        var skipTargetFind: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var isDrawingMode: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var preserveObjectStacking: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var stopContextMenu: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var fireRightClick: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var fireMiddleClick: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var targets: Array<Any>?
            get() = definedExternally
            set(value) = definedExternally
    }

}
