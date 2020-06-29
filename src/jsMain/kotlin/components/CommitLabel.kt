package components

import config.GitGraphConfiguration
import fabricjs.*
import graph.Commit

abstract class CommitLabel protected constructor(
    private val text: String,
    private val color: String,
    var position: fabric.Point,
    private val labelOpacity: Double = 1.0,
    private val textColor: String = "#000"
) :
    Renderable {

    private var labelWidth: Int = calcLabelWidth(text)

    private var rectangle: FabricRect = FabricRect()
    private var fabricTextObject: FabricText
    private var line = Line(Point(0, 0), Point(0, 0))

    init {
        with(rectangle) {
            fill = color
            width = labelWidth
            height = GitGraphConfiguration.labelHeight
            selectable = false
            hoverCursor = "pointer"
            strokeWidth = 2
            opacity = labelOpacity
            rx = 5
            ry = 5
            strokeWidth = 1
            stroke = "black"
            left = position.x
            top = position.y
        }

        fabricTextObject = FabricText(text)
        with(fabricTextObject) {
            fontSize = 16
            fontFamily = "Courier New"
            fill = textColor
            selectable = false
            hoverCursor = "pointer"
            left = position.x + 5
            top = position.y + 2
        }
    }

    override fun render(canvas: FabricCanvas) {
        canvas.add(rectangle)
        canvas.add(fabricTextObject)
    }

    private fun setPosition(position: Point, canvas: FabricCanvas) {
        this.position = position
        canvas.remove(rectangle)
        canvas.remove(fabricTextObject)

        rectangle.left = position.x
        rectangle.top = position.y
        fabricTextObject.left = position.x + 5
        fabricTextObject.top = position.y + 2

        canvas.add(rectangle)
        canvas.add(fabricTextObject)
    }

    var isActive: Boolean = false
        set(value) {
            if (value) {
                fabricTextObject.set("fontWeight", "bold")
                fabricTextObject.set("text", "*${text}*")
                labelWidth = calcLabelWidth("*${text}*")
                rectangle.width = labelWidth
            } else {
                fabricTextObject.set("fontWeight", "normal")
                fabricTextObject.set("text", text)
                labelWidth = calcLabelWidth(text)
                rectangle.width = labelWidth
            }
            fabricTextObject.dirty = true
            rectangle.dirty = true
            field = value
        }

    fun attachToCommit(commit: Commit, canvas: FabricCanvas) {
        setPosition(
            commit.commitCircle.getRightDockPoint()
                    + GitGraphConfiguration.labelOffset
                    + Point(0, (commit.branches.size - 1) * GitGraphConfiguration.labelYOffset), canvas
        )
        setLinePosition(canvas, commit.commitCircle.getRightDockPoint(), getLeftDockPoint())
    }

    fun attachToLabel(commitLabel: CommitLabel, canvas: FabricCanvas) {
        setPosition(commitLabel.position + Point(commitLabel.labelWidth + 25, 0), canvas)
        setLinePosition(canvas, commitLabel.getRightDockPoint(), getLeftDockPoint())
    }

    private fun setLinePosition(canvas: FabricCanvas, start: Point, end: Point) {
        line.removeFrom(canvas)
        line = Line(start, end)
        line.render(canvas)
    }

    fun onDoubleClick(handler: () -> Unit) {
        rectangle.on("mousedblclick") {
            handler()
        }
        fabricTextObject.on("mousedblclick") {
            handler()
        }
    }

    fun getLeftDockPoint(): Point = Point(position.x, position.y + GitGraphConfiguration.labelHeight / 2)
    fun getRightDockPoint(): Point = Point(position.x + labelWidth, position.y + GitGraphConfiguration.labelHeight / 2)

    override fun removeFrom(canvas: FabricCanvas) {
        line.removeFrom(canvas)
        canvas.remove(rectangle)
        canvas.remove(fabricTextObject)
        // FIXME: removing the label from the canvas does not properly work. Work around by setting the label's size to zero and moving it away
        setPosition(Point(9999, 9999), canvas)
        rectangle.width = 0
        rectangle.height = 0
        fabricTextObject.set("text", "")
        rectangle.dirty = true
        fabricTextObject.dirty = true
        canvas.setZoom(canvas.getZoom())
    }

    companion object {
        fun calcLabelWidth(text: String) = text.length * 10 + 10
    }
}

class HeadLabel(position: Point) : CommitLabel("HEAD", "#7CA1EC", position, textColor = "#fff")

class TagLabel(tagName: String, position: Point) : CommitLabel(tagName, "#FFE068", position)

class BranchLabel(branchName: String, position: Point) : CommitLabel(branchName, "#6ECC84", position)