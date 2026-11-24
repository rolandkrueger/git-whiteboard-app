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

    private val BADGE_COLOR = "#B7791F"
    private val REMOTE_TINT = "rgba(255, 193, 7, 0.10)"
    private val CHAR_WIDTH_NAME = 11
    private val CHAR_WIDTH_BADGE = 8

    private var currentName: String = repoName
    private var isRemote = false
    private var originName: String? = null

    // Header badges: a "REMOTE" pill on the remote and "origin -> <name>" on a clone. Never listening.
    private val remoteBadgeRect = Rect(
        jso<RectConfig> {
            y = initialTop + 8
            width = 0
            height = 22
            fill = BADGE_COLOR
            cornerRadius = 11
            listening = false
            visible = false
        }
    )
    private val remoteBadgeText = Text(
        jso<TextConfig> {
            y = initialTop + 12
            text = "REMOTE"
            fontSize = 13
            fontStyle = "bold"
            fontFamily = "Courier New"
            fill = "#FFFFFF"
            listening = false
            visible = false
        }
    )
    private val originBadge = Text(
        jso<TextConfig> {
            y = initialTop + 11
            text = ""
            fontSize = 14
            fontFamily = "Courier New"
            fill = BADGE_COLOR
            listening = false
            visible = false
        }
    )

    override fun render(layer: Layer) {
        layer.add(borderRect.asDynamic())
        layer.add(nameLabel.asDynamic())
        layer.add(remoteBadgeRect.asDynamic())
        layer.add(remoteBadgeText.asDynamic())
        layer.add(originBadge.asDynamic())
        // Reverse order so the border ends up at the very bottom
        originBadge.moveToBottom()
        remoteBadgeText.moveToBottom()
        remoteBadgeRect.moveToBottom()
        nameLabel.moveToBottom()
        borderRect.moveToBottom()
        layoutHeader()
    }

    override fun removeFrom(layer: Layer) {
        borderRect.destroy()
        nameLabel.destroy()
        remoteBadgeRect.destroy()
        remoteBadgeText.destroy()
        originBadge.destroy()
    }

    fun setName(name: String) {
        currentName = name
        nameLabel.text(name)
        layoutHeader()
    }

    /**
     * Marks this region as a remote repo (dashed border, light tint, REMOTE badge) and/or shows
     * `origin -> <originName>` when this repo is a clone. Plain repos look unchanged.
     */
    fun setRemoteLinks(remote: Boolean, originName: String?) {
        isRemote = remote
        this.originName = originName
        if (remote) {
            borderRect.dash(arrayOf<Number>(12, 8))
            borderRect.fill(REMOTE_TINT)
        } else {
            borderRect.dash(arrayOf<Number>())
            borderRect.fill("transparent")
        }
        layoutHeader()
    }

    // Places badges to the right of the name; character widths approximate the monospaced fonts.
    private fun layoutHeader() {
        var x = regionLeft + 12 + currentName.length * CHAR_WIDTH_NAME + 12
        remoteBadgeRect.visible(isRemote)
        remoteBadgeText.visible(isRemote)
        if (isRemote) {
            val w = "REMOTE".length * CHAR_WIDTH_BADGE + 16
            remoteBadgeRect.x(x.toDouble())
            remoteBadgeRect.width(w.toDouble())
            remoteBadgeText.x((x + 8).toDouble())
            x += w + 12
        }
        val origin = originName
        originBadge.visible(origin != null)
        if (origin != null) {
            originBadge.text("origin -> $origin")
            originBadge.x(x.toDouble())
        }
    }

    val left: Int get() = regionLeft
    val top: Int get() = currentTop
    val width: Int get() = currentWidth
    val height: Int get() = currentHeight

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

    /** Shows a red alert border (used briefly to flag a rejected push); [setActive] restores the normal look. */
    fun setAlert() {
        borderRect.stroke("#F14668")
        borderRect.strokeWidth(6.0)
        nameLabel.fill("#F14668")
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
            remoteBadgeRect.y((newTop + 8).toDouble())
            remoteBadgeText.y((newTop + 12).toDouble())
            originBadge.y((newTop + 11).toDouble())
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
