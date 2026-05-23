package components

import konvajs.Layer

interface Renderable {
    fun render(layer: Layer)
    fun removeFrom(layer: Layer)
}
