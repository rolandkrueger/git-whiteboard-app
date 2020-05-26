package components

import fabricjs.FabricCanvas
import fabricjs.FabricLine
import fabricjs.Point

class Line(start: Point, end: Point) : Renderable {
    private val line: FabricLine = FabricLine(arrayOf(start.x, start.y, end.x, end.y))

    override fun render(canvas: FabricCanvas) {
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

    fun removeFrom(canvas: FabricCanvas) {
        canvas.remove(line)
    }
}