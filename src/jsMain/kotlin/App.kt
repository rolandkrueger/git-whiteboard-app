import fabricjs.FabricCanvas
import graph.GitGraph
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.canvas
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import react.RProps
import react.dom.canvas
import react.dom.div
import react.dom.input
import react.functionalComponent
import react.useEffect
import react.useState
import kotlin.browser.document

val scope = MainScope()

val App = functionalComponent<RProps> { _ ->
    val (currentCanvas, setCanvas) = useState(FabricCanvas(""))
    val (currentGraph, setGraph) = useState(GitGraph(currentCanvas))

    useEffect(dependencies = listOf()) {
        scope.launch {
            val canvasElement = document.getElementById("gitKannWas") as HTMLCanvasElement
            val canvasBox = document.getElementById("canvasBox")
            if (canvasBox != null) {
                canvasElement.width = canvasBox.clientWidth
                canvasElement.height = if (document.body?.clientHeight != null) {
                    document.body?.clientHeight!!
                } else {
                    1000
                }
            }

            val canvas = FabricCanvas("gitKannWas")

            var isDragging = false
            var lastPosX = 0
            var lastPosY = 0

            canvas.on("mouse:down") {
                val event = it.e as MouseEvent
                isDragging = true
                canvas.selection = false
                lastPosX = event.clientX
                lastPosY = event.clientY
            }
            canvas.on("mouse:move") {
                if (isDragging) {
                    val event = it.e as MouseEvent
                    val viewportTransform = canvas.viewportTransform
                    if (viewportTransform != null) {
                        viewportTransform.set(4, viewportTransform[4] + event.clientX - lastPosX)
                        viewportTransform.set(5, viewportTransform[5] + event.clientY - lastPosY)
                    }
                    canvas.requestRenderAll()
                    lastPosX = event.clientX
                    lastPosY = event.clientY
                }
            }
            canvas.on("mouse:up") {
                isDragging = false
                canvas.selection = true
            }

            canvas.on("mouse:wheel") {
                val event = it.e as WheelEvent
                if (event.ctrlKey) {
                    val delta = event.deltaY
                    var zoom = canvas.getZoom()
                    zoom += if (delta > 0) {
                        -0.06
                    } else {
                        0.06
                    }
                    if (zoom > 3.0) zoom = 3.0
                    if (zoom < 0.5) zoom = 0.5
                    canvas.setZoom(zoom)
                    event.preventDefault()
                    event.stopPropagation()
                }
            }

            setCanvas(canvas)
            val gitGraph = GitGraph(canvas)
            gitGraph.initGraph()
            setGraph(gitGraph)
        }
    }

    div("columns") {
        div("column") {
            div("has-background-light") {
                div("field") {
                    input(InputType.button, classes = "input") {
                        attrs {
                            value = "Add commit"
                            onClickFunction = {
                                currentGraph.addCommit()
                            }
                        }
                    }
                }

                div("field") {
                    div("columns") {
                        div("column is-two-thirds") {
                            input(InputType.text, classes = "input") {
                                attrs {
                                    id = "branchNameInput"
                                    placeholder = "Branch name"
                                }
                            }
                        }
                        div("column") {
                            input(InputType.button, classes = "input") {
                                attrs {
                                    value = "Add branch"
                                    onClickFunction = {
                                        val input: HTMLInputElement? =
                                            document.getElementById("branchNameInput") as HTMLInputElement?
                                        if (input?.value != null && input.value.isNotEmpty()) {
                                            currentGraph.addBranch(input.value)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                div("field") {
                    div("columns") {
                        div("column is-two-thirds") {
                            input(InputType.text, classes = "input") {
                                attrs {
                                    id = "checkoutBranchNameInput"
                                    placeholder = "Branch name or commit ID to check out"
                                }
                            }
                        }
                        div("column") {
                            input(InputType.button, classes = "input") {
                                attrs {
                                    value = "Check out"
                                    onClickFunction = {
                                        val input: HTMLInputElement? =
                                            document.getElementById("checkoutBranchNameInput") as HTMLInputElement?
                                        if (input?.value != null && input.value.isNotEmpty()) {
                                            currentGraph.checkout(input.value)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                div("field") {
                    div("columns") {
                        div("column is-two-thirds") {
                            input(InputType.text, classes = "input") {
                                attrs {
                                    id = "mergeBranchNameInput"
                                    placeholder = "Branch name to merge into current branch"
                                }
                            }
                        }
                        div("column") {
                            input(InputType.button, classes = "input") {
                                attrs {
                                    value = "Merge"
                                    onClickFunction = {
                                        val input: HTMLInputElement? =
                                            document.getElementById("mergeBranchNameInput") as HTMLInputElement?
                                        if (input?.value != null && input.value.isNotEmpty()) {
                                            currentGraph.merge(input.value)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                div("field") {
                    div("columns") {
                        div("column is-two-thirds") {
                            input(InputType.text, classes = "input") {
                                attrs {
                                    id = "tagNameInput"
                                    placeholder = "Tag name"
                                }
                            }
                        }
                        div("column") {
                            input(InputType.button, classes = "input") {
                                attrs {
                                    value = "Create tag"
                                    onClickFunction = {
                                        val input: HTMLInputElement? =
                                            document.getElementById("tagNameInput") as HTMLInputElement?
                                        if (input?.value != null && input.value.isNotEmpty()) {
                                            currentGraph.addTag(input.value)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                div("field") {
                    input(InputType.button, classes = "input") {
                        attrs {
                            value = "Reset history"
                            onClickFunction = {
                                currentCanvas.clear()
                                val newGraph = GitGraph(currentCanvas)
                                newGraph.initGraph()
                                setGraph(newGraph)
                            }
                        }
                    }
                }
            }
        }

        div("column is-three-quarters") {
            div("box") {
                attrs.id = "canvasBox"
                canvas("image") {
                    attrs {
                        id = "gitKannWas"
                    }
                }
            }
        }
    }
}