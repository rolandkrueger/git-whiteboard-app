package components

import config.GitGraphConfiguration
import graph.Commit
import konvajs.Layer
import konvajs.Rect
import konvajs.RectConfig
import konvajs.Text
import konvajs.TextConfig

abstract class CommitLabel protected constructor(
    private val text: String,
    private val color: String,
    var position: Point,
    private val labelOpacity: Double = 1.0,
    private val textColor: String = "#000"
) : Renderable {

    private var labelWidth: Int = calcLabelWidth(text)

    private var rectangle: Rect = createRect(position, labelWidth, color, labelOpacity)
    private var textObject: Text = createText(text, position, textColor)
    private var line = Line(Point(0, 0), Point(0, 0))
    private var lineRendered = false

    override fun render(layer: Layer) {
        layer.add(rectangle.asDynamic())
        layer.add(textObject.asDynamic())
    }

    private fun setPosition(position: Point) {
        this.position = position
        rectangle.x(position.x.toDouble())
        rectangle.y(position.y.toDouble())
        textObject.x((position.x + 5).toDouble())
        textObject.y((position.y + 2).toDouble())
    }

    var isActive: Boolean = false
        set(value) {
            val displayText = if (value) "*${text}*" else text
            textObject.fontStyle(if (value) "bold" else "normal")
            textObject.text(displayText)
            labelWidth = calcLabelWidth(displayText)
            rectangle.width(labelWidth.toDouble())
            field = value
        }

    fun attachToCommit(commit: Commit, layer: Layer) {
        setPosition(
            commit.commitCircle.getRightDockPoint()
                    + GitGraphConfiguration.labelOffset
                    + Point(0, (commit.branches.size - 1) * GitGraphConfiguration.labelYOffset)
        )
        setLinePosition(layer, commit.commitCircle.getRightDockPoint(), getLeftDockPoint())
    }

    fun attachToLabel(commitLabel: CommitLabel, layer: Layer) {
        setPosition(commitLabel.position + Point(commitLabel.labelWidth + 25, 0))
        setLinePosition(layer, commitLabel.getRightDockPoint(), getLeftDockPoint())
    }

    private fun setLinePosition(layer: Layer, start: Point, end: Point) {
        if (lineRendered) {
            line.removeFrom(layer)
        }
        line = Line(start, end)
        line.render(layer)
        lineRendered = true
    }

    fun onDoubleClick(handler: () -> Unit) {
        rectangle.on("dblclick") { handler() }
        textObject.on("dblclick") { handler() }
    }

    fun getLeftDockPoint(): Point = Point(position.x, position.y + GitGraphConfiguration.labelHeight / 2)
    fun getRightDockPoint(): Point = Point(position.x + labelWidth, position.y + GitGraphConfiguration.labelHeight / 2)

    override fun removeFrom(layer: Layer) {
        if (lineRendered) {
            line.removeFrom(layer)
            lineRendered = false
        }
        rectangle.destroy()
        textObject.destroy()
    }

    companion object {
        fun calcLabelWidth(text: String) = text.length * 10 + 10

        fun createRect(position: Point, width: Int, color: String, opacity: Double): Rect {
            return Rect(
                jso<RectConfig> {
                    x = position.x
                    y = position.y
                    this.width = width
                    height = GitGraphConfiguration.labelHeight
                    fill = color
                    stroke = "black"
                    strokeWidth = 1
                    this.opacity = opacity
                    cornerRadius = 5
                    listening = true
                }
            )
        }

        fun createText(text: String, position: Point, textColor: String): Text {
            return Text(
                jso<TextConfig> {
                    x = position.x + 5
                    y = position.y + 2
                    this.text = text
                    fontSize = 16
                    fontFamily = "Courier New"
                    fill = textColor
                    listening = true
                }
            )
        }
    }
}

class HeadLabel(position: Point) : CommitLabel("HEAD", "#7CA1EC", position, textColor = "#fff")

class TagLabel(tagName: String, position: Point) : CommitLabel(tagName, "#FFE068", position)

class BranchLabel(branchName: String, position: Point) : CommitLabel(branchName, "#6ECC84", position)
