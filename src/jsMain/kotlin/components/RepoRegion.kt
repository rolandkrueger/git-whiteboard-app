package components

import fabricjs.FabricCanvas
import fabricjs.FabricRect
import fabricjs.FabricText

/**
 * A visual region drawn behind a GitGraph to identify a repo on the shared canvas.
 * Shows a colored border when the repo is active; neutral border when inactive.
 * Also displays the repo name as a label.
 */
class RepoRegion(
    private val repoName: String,
    private val regionLeft: Int,
    private val regionTop: Int,
    private val regionWidth: Int = 700,
    private val regionHeight: Int = 480
) : Renderable {

    private val borderRect = FabricRect()
    private val nameLabel = FabricText(repoName)

    private val ACTIVE_STROKE = "#2196F3"
    private val INACTIVE_STROKE = "#CCCCCC"
    private val STROKE_WIDTH_ACTIVE = 3
    private val STROKE_WIDTH_INACTIVE = 1

    init {
        with(borderRect) {
            left = regionLeft
            top = regionTop
            width = regionWidth
            height = regionHeight
            fill = "transparent"
            stroke = INACTIVE_STROKE
            strokeWidth = STROKE_WIDTH_INACTIVE
            selectable = false
            evented = false
            rx = 8
            ry = 8
        }
        with(nameLabel) {
            left = regionLeft + 12
            top = regionTop + 8
            fontSize = 18
            fontFamily = "Courier New"
            fill = "#888888"
            selectable = false
            evented = false
        }
    }

    override fun render(canvas: FabricCanvas) {
        canvas.add(borderRect)
        canvas.sendToBack(borderRect)
        canvas.add(nameLabel)
        canvas.sendToBack(nameLabel)
    }

    override fun removeFrom(canvas: FabricCanvas) {
        canvas.remove(borderRect)
        canvas.remove(nameLabel)
    }

    fun setActive(active: Boolean) {
        if (active) {
            borderRect.set("stroke", ACTIVE_STROKE)
            borderRect.strokeWidth = STROKE_WIDTH_ACTIVE
            nameLabel.set("fill", "#2196F3")
        } else {
            borderRect.set("stroke", INACTIVE_STROKE)
            borderRect.strokeWidth = STROKE_WIDTH_INACTIVE
            nameLabel.set("fill", "#888888")
        }
        borderRect.dirty = true
        nameLabel.dirty = true
    }

    /**
     * Returns true if the given canvas-absolute coordinates are within this region.
     */
    fun containsPoint(x: Double, y: Double): Boolean {
        return x >= regionLeft && x <= regionLeft + regionWidth && y >= regionTop && y <= regionTop + regionHeight
    }
}
