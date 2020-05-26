package components

import config.GitGraphConfiguration
import fabricjs.FabricCanvas
import fabricjs.FabricRect
import fabricjs.FabricText
import fabricjs.fabric

class CommitIdLabel(id: String, position: fabric.Point) : Renderable {

    private var rectangle: FabricRect = FabricRect()
    private var fabricTextObject: FabricText

    init {
        with(rectangle) {
            fill = "#FFF"
            width = CommitLabel.calcLabelWidth(id)
            height = GitGraphConfiguration.labelHeight
            selectable = false
            hoverCursor = "pointer"
            strokeWidth = 2
            opacity = 0.8
            rx = 5
            ry = 5
            strokeWidth = 1
            stroke = "black"
            left = position.x
            top = position.y
        }

        fabricTextObject = FabricText(id)
        with(fabricTextObject) {
            fontSize = 16
            fontFamily = "Courier New"
            fill = "#000"
            selectable = false
            hoverCursor = "pointer"
            left = position.x + 5
            top = position.y + 2
        }
    }

    fun onDoubleClick(handler: () -> Unit) {
        rectangle.on("mousedblclick") {
            handler()
        }
        fabricTextObject.on("mousedblclick") {
            handler()
        }
    }

    override fun render(canvas: FabricCanvas) {
        canvas.add(rectangle)
        canvas.add(fabricTextObject)
    }
}