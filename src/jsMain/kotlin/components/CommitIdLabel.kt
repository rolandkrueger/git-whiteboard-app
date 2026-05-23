package components

import config.GitGraphConfiguration
import konvajs.Layer
import konvajs.Rect
import konvajs.RectConfig
import konvajs.Text
import konvajs.TextConfig

class CommitIdLabel(id: String, position: Point) : Renderable {

    private var rectangle: Rect = Rect(
        jso<RectConfig> {
            x = position.x
            y = position.y
            width = CommitLabel.calcLabelWidth(id)
            height = GitGraphConfiguration.labelHeight
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
            x = position.x + 5
            y = position.y + 2
            text = id
            fontSize = 16
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
}
