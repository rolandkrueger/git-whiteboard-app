package components

import konvajs.Arrow
import konvajs.ArrowConfig
import konvajs.Layer

/**
 * Thin blue Bezier arrow from a label's dock point ([start]) to the commit (or previous label) it points at ([end]).
 * Konva draws the arrowhead at the last point, so the head sits at [end], the commit / previous-label side.
 * Both control points sit at the y of their own end point, so the curve leaves and enters horizontally
 * and the arrowhead (oriented along control point 2 -> end) points horizontally into the target.
 */
class LabelConnector(start: Point, end: Point) : Renderable {
    private val arrow: Arrow

    init {
        val midX = (start.x + end.x) / 2
        arrow = Arrow(
            jso<ArrowConfig> {
                points = arrayOf(start.x, start.y, midX, start.y, midX, end.y, end.x, end.y)
                bezier = true
                fill = CONNECTOR_COLOR
                stroke = CONNECTOR_COLOR
                strokeWidth = 1.5
                pointerLength = 8
                pointerWidth = 7
                listening = false
            }
        )
    }

    override fun render(layer: Layer) {
        layer.add(arrow.asDynamic())
        arrow.moveToBottom()
    }

    override fun removeFrom(layer: Layer) {
        arrow.destroy()
    }

    companion object {
        const val CONNECTOR_COLOR = "#2196F3"
    }
}
