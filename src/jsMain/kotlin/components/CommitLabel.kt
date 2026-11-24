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
    private val textColor: String = "#000",
    private val dashed: Boolean = false
) : Renderable {

    private var labelWidth: Int = calcLabelWidth(text)

    private var rectangle: Rect = createRect(position, labelWidth, color, labelOpacity, dashed)
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

    /** Greys the label out and makes it semi-transparent (or restores the normal look). */
    fun setMuted(muted: Boolean) {
        rectangle.fill(if (muted) MUTED_FILL else color)
        rectangle.opacity(if (muted) MUTED_OPACITY else labelOpacity)
        textObject.opacity(if (muted) MUTED_OPACITY else 1.0)
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
        private const val MUTED_FILL = "#9E9E9E"
        private const val MUTED_OPACITY = 0.5

        fun calcLabelWidth(text: String) = text.length * 10 + 10

        fun createRect(position: Point, width: Int, color: String, opacity: Double, dashed: Boolean = false): Rect {
            return Rect(
                jso<RectConfig> {
                    x = position.x
                    y = position.y
                    this.width = width
                    height = GitGraphConfiguration.labelHeight
                    if (dashed) {
                        // Unfilled pill with a dashed border in the branch colour (remote-tracking label)
                        fill = "rgba(255,255,255,0.01)"
                        stroke = color
                        dash = arrayOf(6, 4)
                    } else {
                        fill = color
                        stroke = "black"
                    }
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

/** Remote-tracking label: outlined, dashed, grey-ish text. [color] is the branch colour used for the border. */
class RemoteBranchLabel(text: String, color: String, position: Point) :
    CommitLabel(text, color.ifEmpty { "#888888" }, position, textColor = "#777", dashed = true)
