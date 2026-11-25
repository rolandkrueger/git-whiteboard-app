package components

import konvajs.Layer
import konvajs.Line as KonvaLine
import konvajs.LineConfig

/** Black commit edge: straight inside a lane, smooth Bezier bend where it changes lanes (see [commitEdgePoints]). */
class CommitEdge(start: Point, end: Point, kind: EdgeKind, commitDistance: Int) : Renderable {
    private val line: KonvaLine = KonvaLine(
        jso<LineConfig> {
            val pts = commitEdgePoints(start, end, kind, commitDistance)
            points = pts.map { it as Number }.toTypedArray()
            bezier = pts.size > 4
            fill = "black"
            stroke = "black"
            strokeWidth = 3
            listening = false
        }
    )

    override fun render(layer: Layer) {
        layer.add(line.asDynamic())
        line.moveToBottom()
    }

    override fun removeFrom(layer: Layer) {
        line.destroy()
    }
}
