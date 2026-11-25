package components

import config.GitGraphConfiguration
import konvajs.Layer
import konvajs.Rect
import konvajs.RectConfig
import konvajs.Text
import konvajs.TextConfig

/**
 * The commit ID drawn inside the commit circle. Short IDs (up to 3 characters) keep the classic 16 px look;
 * longer ones (e.g. `fl123**`) shrink the font, but not below [MIN_FONT_SIZE], so the box stays inside the circle.
 */
class CommitIdLabel(id: String, center: Point) : Renderable {

    private val textSize: Int = fontSizeFor(id)
    private val shrunk = id.length > CLASSIC_MAX_LENGTH
    private val boxWidth: Int =
        if (shrunk) kotlin.math.ceil(id.length * CHAR_WIDTH_RATIO * textSize).toInt() + 4 else CommitLabel.calcLabelWidth(id)
    private val boxHeight: Int = if (shrunk) textSize + 6 else GitGraphConfiguration.labelHeight
    private val boxX = center.x - boxWidth / 2
    private val boxY = center.y - boxHeight / 2

    private var rectangle: Rect = Rect(
        jso<RectConfig> {
            x = boxX
            y = boxY
            width = boxWidth
            height = boxHeight
            fill = "#FFF"
            stroke = "black"
            strokeWidth = 1
            opacity = 0.8
            cornerRadius = 5
            listening = true
        }
    )
    private var textObject: Text = Text(
        jso<TextConfig> {
            x = boxX + if (shrunk) 2 else 5
            y = boxY + if (shrunk) (boxHeight - textSize) / 2 else 2
            text = id
            fontSize = textSize
            fontFamily = "Courier New"
            fill = "#000"
        }
    )

    fun onDoubleClick(handler: () -> Unit) {
        rectangle.on("dblclick") { handler() }
        textObject.on("dblclick") { handler() }
    }

    override fun render(layer: Layer) {
        layer.add(rectangle.asDynamic())
        layer.add(textObject.asDynamic())
    }

    override fun removeFrom(layer: Layer) {
        rectangle.destroy()
        textObject.destroy()
    }

    companion object {
        const val MAX_FONT_SIZE = 16
        const val MIN_FONT_SIZE = 10
        private const val CLASSIC_MAX_LENGTH = 3
        private const val CHAR_WIDTH_RATIO = 0.6 // Courier New glyph advance / font size
        private const val MAX_TEXT_WIDTH = 40.0 // keeps the box inside the 50 px commit circle

        fun fontSizeFor(id: String): Int {
            if (id.length <= CLASSIC_MAX_LENGTH) return MAX_FONT_SIZE
            val fitting = kotlin.math.floor(MAX_TEXT_WIDTH / (id.length * CHAR_WIDTH_RATIO)).toInt()
            return fitting.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        }
    }
}
