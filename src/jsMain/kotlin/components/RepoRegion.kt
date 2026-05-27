package components

import konvajs.Layer
import konvajs.Rect
import konvajs.RectConfig
import konvajs.Text
import konvajs.TextConfig

/**
 * A visual region drawn behind a GitGraph to identify a repo on the shared canvas.
 * Shows a colored border when the repo is active; neutral border when inactive.
 * Also displays the repo name as a label.
 *
 * The region can grow to enclose new graph content via [updateBounds].
 */
class RepoRegion(
    repoName: String,
    private val regionLeft: Int,
    initialTop: Int,
    initialWidth: Int = 700,
    initialHeight: Int = 480
) : Renderable {

    private val ACTIVE_STROKE = "#2196F3"
    private val INACTIVE_STROKE = "#CCCCCC"
    private val STROKE_WIDTH_ACTIVE = 3
    private val STROKE_WIDTH_INACTIVE = 1

    /** Current top edge of the region (can decrease as the graph grows upward). */
    private var currentTop: Int = initialTop

    /** Current width of the region (can increase as the graph grows rightward). */
    private var currentWidth: Int = initialWidth

    /** Current height of the region (updated whenever top or right edge changes). */
    private var currentHeight: Int = initialHeight

    private val borderRect = Rect(
        jso<RectConfig> {
            x = regionLeft
            y = initialTop
            width = initialWidth
            height = initialHeight
            fill = "transparent"
            stroke = INACTIVE_STROKE
            strokeWidth = STROKE_WIDTH_INACTIVE
            cornerRadius = 8
            listening = false
        }
    )

    private val nameLabel = Text(
        jso<TextConfig> {
            x = regionLeft + 12
            y = initialTop + 8
            text = repoName
            fontSize = 18
            fontFamily = "Courier New"
            fill = "#888888"
            listening = false
        }
    )

    override fun render(layer: Layer) {
        layer.add(borderRect.asDynamic())
        borderRect.moveToBottom()
        layer.add(nameLabel.asDynamic())
        nameLabel.moveToBottom()
    }

    override fun removeFrom(layer: Layer) {
        borderRect.destroy()
        nameLabel.destroy()
    }

    fun setName(name: String) {
        nameLabel.text(name)
    }

    fun setActive(active: Boolean) {
        if (active) {
            borderRect.stroke(ACTIVE_STROKE)
            borderRect.strokeWidth(STROKE_WIDTH_ACTIVE.toDouble())
            nameLabel.fill("#2196F3")
        } else {
            borderRect.stroke(INACTIVE_STROKE)
            borderRect.strokeWidth(STROKE_WIDTH_INACTIVE.toDouble())
            nameLabel.fill("#888888")
        }
    }

    /**
     * Updates the region bounds to the given values, growing the border rect.
     * Only expands — never shrinks below current size.
     * The left edge and bottom edge are fixed anchors; the region grows upward and rightward.
     *
     * @param newTop the new top-edge y coordinate (may be smaller than current to grow upward)
     * @param newWidth the new width (may be larger than current to grow rightward)
     * @param newHeight the new height to match the new top while keeping the bottom anchor fixed
     */
    fun updateBounds(newTop: Int, newWidth: Int, newHeight: Int) {
        if (newTop < currentTop) {
            currentTop = newTop
            borderRect.y(newTop.toDouble())
            nameLabel.y((newTop + 8).toDouble())
        }
        if (newWidth > currentWidth) {
            currentWidth = newWidth
            borderRect.width(newWidth.toDouble())
        }
        if (newHeight > currentHeight) {
            currentHeight = newHeight
            borderRect.height(newHeight.toDouble())
        }
    }

    /**
     * Returns true if the given canvas-absolute coordinates are within this region.
     * Uses the current (dynamically updated) bounds.
     */
    fun containsPoint(x: Double, y: Double): Boolean {
        return x >= regionLeft && x <= regionLeft + currentWidth &&
                y >= currentTop && y <= currentTop + currentHeight
    }
}
