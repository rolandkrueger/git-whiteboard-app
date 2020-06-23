package components

import config.GitGraphConfiguration
import fabricjs.FabricCanvas
import fabricjs.FabricCircle
import fabricjs.Point
import fabricjs.fabric

class CommitCircle(id: String, private val centerPosition: fabric.Point) :
    Renderable {

    var isLostInReflog: Boolean = false
        set(value) {
            if (value) {
                setColor("#CCC", "#CCC")
            } else {
                setColor("#423462", "#A081EF")
            }
            field = value
        }

    private var circle: FabricCircle = FabricCircle()

    private var idLabel: CommitIdLabel = CommitIdLabel(
        id, Point(
            centerPosition.x - CommitLabel.calcLabelWidth(id) / 2,
            centerPosition.y - GitGraphConfiguration.labelHeight / 2
        )
    )

    init {
        with(circle) {
            left = centerPosition.x
            top = centerPosition.y
            strokeWidth = 2
            radius = GitGraphConfiguration.commitRadius
            if (isLostInReflog) {
                fill = "#CCC"
                stroke = "#CCC"
            } else {
                fill = "#A081EF"
                stroke = "#423462"
            }
            hasControls = false
            hasBorders = false
            selectable = false
            hoverCursor = "pointer"
        }
    }

    override fun render(canvas: FabricCanvas) {
        canvas.add(circle)
        idLabel.render(canvas)
    }

    fun onDoubleClick(handler: () -> Unit) {
        circle.on("mousedblclick") {
            handler()
        }
        idLabel.onDoubleClick(handler)
    }

    fun setColor(stroke: String, fill: String) {
        circle.set("stroke", stroke)
        circle.set("fill", fill)
    }

    override fun removeFrom(canvas: FabricCanvas) {
        canvas.remove(circle)
        idLabel.removeFrom(canvas)
    }

    fun getUpperDockPoint() = centerPosition.subtract(fabric.Point(0, GitGraphConfiguration.commitRadius))
    fun getLowerDockPoint() = centerPosition.add(fabric.Point(0, GitGraphConfiguration.commitRadius))
    fun getRightDockPoint() = centerPosition.add(fabric.Point(GitGraphConfiguration.commitRadius, 0))
}