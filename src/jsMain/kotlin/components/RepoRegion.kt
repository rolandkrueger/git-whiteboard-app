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
 */
class RepoRegion(
    private val repoName: String,
    private val regionLeft: Int,
    private val regionTop: Int,
    private val regionWidth: Int = 700,
    private val regionHeight: Int = 480
) : Renderable {

    private val ACTIVE_STROKE = "#2196F3"
    private val INACTIVE_STROKE = "#CCCCCC"
    private val STROKE_WIDTH_ACTIVE = 3
    private val STROKE_WIDTH_INACTIVE = 1

    private val borderRect = Rect(
        jso<RectConfig> {
            x = regionLeft
            y = regionTop
            width = regionWidth
            height = regionHeight
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
            y = regionTop + 8
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
     * Returns true if the given canvas-absolute coordinates are within this region.
     */
    fun containsPoint(x: Double, y: Double): Boolean {
        return x >= regionLeft && x <= regionLeft + regionWidth && y >= regionTop && y <= regionTop + regionHeight
    }
}
