package components

import fabricjs.FabricCanvas
import fabricjs.FabricLine
import fabricjs.Point

class Line(private val start: Point, private val end: Point) : Renderable {
    override fun render(canvas: FabricCanvas) {
        val line = FabricLine(arrayOf(start.x, start.y, end.x, end.y))
        with(line) {
            fill = "black"
            stroke = "black"
            strokeWidth = 3
            selectable = false
            evented = false
        }
        canvas.add(line)
        canvas.sendToBack(line)
    }
}