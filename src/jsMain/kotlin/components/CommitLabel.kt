package components

import fabricjs.*

abstract class CommitLabel protected constructor(
    private val text: String,
    private val color: String,
    private val position: fabric.Point,
    private val isActive: Boolean = false,
    private val labelOpacity: Double = 1.0,
    private val textColor: String = "#fff"
) :
    Renderable {

    private var labelWidth: Int = calcLabelWidth(text)

    private lateinit var rectangle: FabricRect
    private lateinit var fabricTextObject: FabricText

    override fun render(canvas: FabricCanvas) {
        rectangle = FabricRect()
        with(rectangle) {
            left = position.x
            top = position.y
            fill = color
            width = labelWidth
            height = LABEL_HEIGHT
            selectable = false
            hoverCursor = "pointer"
            strokeWidth = 2
            opacity = labelOpacity
            rx = 5
            ry = 5
            strokeWidth = 1
            stroke = "black"
        }

        fabricTextObject = FabricText(text)
        with(fabricTextObject) {
            left = position.x + 5
            top = position.y + 2
            fontSize = 16
            fontFamily = "Courier New"
            fill = textColor
            selectable = false
            hoverCursor = "pointer"
        }

        if (isActive) {
            fabricTextObject.fontWeight = "bold"
            fabricTextObject.fill = "#000"
        }

        canvas.add(rectangle)
        canvas.add(fabricTextObject)

    }

    fun onDoubleClick(handler: () -> Unit) {
        rectangle.on("mousedblclick") {
            handler()
        }
        fabricTextObject.on("mousedblclick") {
            handler()
        }
    }

    fun getLeftDockPoint(): Point = Point(position.x, position.y + LABEL_HEIGHT / 2)
    fun getRightDockPoint(): Point = Point(position.x + labelWidth, position.y + LABEL_HEIGHT / 2)

    companion object {
        const val LABEL_HEIGHT = 24

        fun calcLabelWidth(text: String) = text.length * 10 + 10
    }
}

class HeadLabel(position: Point) : CommitLabel("HEAD", "#4C95EF", position)

class TagLabel(tagName: String, position: Point) : CommitLabel(tagName, "#BCBC25", position)

class BranchLabel(branchName: String, position: Point, isActive: Boolean) :
    CommitLabel(branchName, "#07BF3C", position, isActive)

class IdLabel(id: String, position: Point) :
    CommitLabel(id, "#FFF", position, false, 0.8, "#000")