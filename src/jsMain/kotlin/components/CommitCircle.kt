package components

import config.GitGraphConfiguration
import konvajs.Circle
import konvajs.CircleConfig
import konvajs.Layer

class CommitCircle(id: String, private val centerPosition: Point, private val commitColor: String) :
    Renderable {

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
    }

    fun onDoubleClick(handler: () -> Unit) {
        circle.on("dblclick") { handler() }
        idLabel.onDoubleClick(handler)
    }

    fun setColor(stroke: String, fill: String) {
        circle.stroke(stroke)
        circle.fill(fill)
    }

    override fun removeFrom(layer: Layer) {
        circle.destroy()
        idLabel.removeFrom(layer)
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
