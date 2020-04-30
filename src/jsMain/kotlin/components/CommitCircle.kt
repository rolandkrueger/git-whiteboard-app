package components

import fabricjs.FabricCanvas
import fabricjs.FabricCircle
import fabricjs.Point
import fabricjs.fabric

class CommitCircle(val id: String, private val centerPosition: fabric.Point) :
    Renderable {

    var isLostInReflog: Boolean = false
        set(value) {
            if (value) {
                circle.fill = "#CCC"
                circle.stroke = "#CCC"
            } else {
                circle.fill = "#A081EF"
                circle.stroke = "#423462"
            }
            field = value
        }

    private var circle: FabricCircle = FabricCircle()

    private var idLabel: IdLabel = IdLabel(
        id, Point(
            centerPosition.x - CommitLabel.calcLabelWidth(id) / 2,
            centerPosition.y - CommitLabel.LABEL_HEIGHT / 2
        )
    )

    init {
        with(circle) {
            left = centerPosition.x
            top = centerPosition.y
            strokeWidth = 2
            radius = RADIUS
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

    fun getUpperDockPoint() = centerPosition.subtract(fabric.Point(0, RADIUS))
    fun getLowerDockPoint() = centerPosition.add(fabric.Point(0, RADIUS))
    fun getLeftDockPoint() = centerPosition.subtract(fabric.Point(RADIUS, 0))
    fun getRightDockPoint() = centerPosition.add(fabric.Point(RADIUS, 0))

    companion object {
        const val RADIUS = 25
    }
}