package components

import fabricjs.FabricCanvas

interface Renderable {
    fun render(canvas: FabricCanvas)
    fun removeFrom(canvas: FabricCanvas)
}