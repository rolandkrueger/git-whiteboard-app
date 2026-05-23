package components

import konvajs.Layer
import konvajs.Line as KonvaLine
import konvajs.LineConfig

class Line(start: Point, end: Point) : Renderable {
    private val line: KonvaLine = KonvaLine(
        jso<LineConfig> {
            points = arrayOf(start.x, start.y, end.x, end.y)
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
