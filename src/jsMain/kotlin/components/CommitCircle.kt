package components

import config.GitGraphConfiguration
import konvajs.Circle
import konvajs.CircleConfig
import konvajs.Layer
import konvajs.Text
import konvajs.TextConfig

class CommitCircle(
    id: String,
    private val centerPosition: Point,
    private val commitColor: String,
    conflicting: Boolean = false
) : Renderable {

    var isLostInReflog: Boolean = false
        set(value) {
            if (value) {
                setColor("#CCC", "#CCC")
            } else {
                setColor("#423462", commitColor)
            }
            field = value
        }

    private var circle: Circle = createCircle()

    private var idLabel: CommitIdLabel = CommitIdLabel(
        id, Point(
            centerPosition.x - CommitLabel.calcLabelWidth(id) / 2,
            centerPosition.y - GitGraphConfiguration.labelHeight / 2
        )
    )

    /** Warning badge for a conflicting change (ADR 0006); null for unmarked commits. */
    private val badge: Circle? = if (conflicting) createBadge() else null
    private val badgeMark: Text? = if (conflicting) createBadgeMark() else null

    private fun badgeCenter() = Point(
        centerPosition.x + GitGraphConfiguration.commitRadius * 4 / 5,
        centerPosition.y - GitGraphConfiguration.commitRadius * 4 / 5
    )

    private fun createBadge(): Circle {
        val c = badgeCenter()
        return Circle(
            jso<CircleConfig> {
                x = c.x
                y = c.y
                radius = 10
                fill = "#E8590C"
                stroke = "#FFF"
                strokeWidth = 2
                listening = false
            }
        )
    }

    private fun createBadgeMark(): Text {
        val c = badgeCenter()
        return Text(
            jso<TextConfig> {
                x = c.x - 10
                y = c.y - 8
                width = 20
                align = "center"
                text = "!"
                fontSize = 16
                fontStyle = "bold"
                fontFamily = "Arial"
                fill = "#FFF"
                listening = false
            }
        )
    }

    private fun createCircle(): Circle {
        val r = GitGraphConfiguration.commitRadius
        return Circle(
            jso<CircleConfig> {
                // Konva circles are centered at x,y by default; use offsetX/offsetY to center
                x = centerPosition.x + r
                y = centerPosition.y + r
                offsetX = r
                offsetY = r
                radius = r
                fill = if (isLostInReflog) "#CCC" else commitColor
                stroke = if (isLostInReflog) "#CCC" else "#423462"
                strokeWidth = 2
                listening = true
            }
        )
    }

    override fun render(layer: Layer) {
        layer.add(circle.asDynamic())
        idLabel.render(layer)
        badge?.let { layer.add(it.asDynamic()) }
        badgeMark?.let { layer.add(it.asDynamic()) }
    }

    fun onDoubleClick(handler: () -> Unit) {
        circle.on("dblclick") { handler() }
        idLabel.onDoubleClick(handler)
    }

    fun setColor(stroke: String, fill: String) {
        circle.stroke(stroke)
        circle.fill(fill)
        // Fade the conflict badge together with a lost commit.
        badge?.fill(if (stroke == "#CCC") "#CCC" else "#E8590C")
    }

    override fun removeFrom(layer: Layer) {
        circle.destroy()
        idLabel.removeFrom(layer)
        badge?.destroy()
        badgeMark?.destroy()
    }

    fun getUpperDockPoint() = Point(centerPosition.x, centerPosition.y - GitGraphConfiguration.commitRadius)
    fun getLowerDockPoint() = Point(centerPosition.x, centerPosition.y + GitGraphConfiguration.commitRadius)
    fun getRightDockPoint() = Point(centerPosition.x + GitGraphConfiguration.commitRadius, centerPosition.y)
    fun getLeftDockPoint() = Point(centerPosition.x - GitGraphConfiguration.commitRadius, centerPosition.y)
}

// Helper to create a JS object from a lambda without depending on kotlin-stdlib-js directly
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> jso(block: T.() -> Unit): T {
    val result = js("{}") as T
    result.block()
    return result
}
