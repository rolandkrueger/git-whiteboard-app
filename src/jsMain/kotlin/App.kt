import fabricjs.FabricCanvas
import graph.GitGraph
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.html.id
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import react.RProps
import react.dom.canvas
import react.functionalComponent
import react.useEffect
import react.useState
import ui.ConfirmationDialog
import ui.UiControl.Companion.activateTab
import ui.UiControl.Companion.doWhenButtonClicked
import ui.UiControl.Companion.doWhenLinkClicked
import ui.UiControl.Companion.getUserInput
import ui.UiControl.Companion.hideElements
import ui.UiControl.Companion.showElements
import kotlin.browser.document
import kotlin.browser.window

val scope = MainScope()

val App = functionalComponent<RProps> { _ ->
    val (currentCanvas, setCanvas) = useState(FabricCanvas(""))
    val (currentGraph, setGraph) = useState(GitGraph(currentCanvas))

    useEffect(dependencies = listOf()) {
        scope.launch {
            val canvasElement = document.getElementById("gitKannWas") as HTMLCanvasElement
            canvasElement.width = window.innerWidth
            canvasElement.height = window.innerHeight

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
                canvas.setZoom(canvas.getZoom())
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
            var gitGraph = GitGraph(canvas)
            gitGraph.initGraph()
            setGraph(gitGraph)

            doWhenButtonClicked("expandPanelButton") {
                hideElements("collapsedControlPanel")
                showElements("expandedControlPanel")
            }
            doWhenButtonClicked("hidePanelButton") {
                hideElements("expandedControlPanel")
                showElements("collapsedControlPanel")
            }

            doWhenLinkClicked("generalTabControl") {
                activateTab("generalTab", "commitTab", "refsTab", "mergeTab", "aboutTab")
            }
            doWhenLinkClicked("commitTabControl") {
                activateTab("commitTab", "generalTab", "refsTab", "mergeTab", "aboutTab")
            }
            doWhenLinkClicked("refsTabControl") {
                activateTab("refsTab", "generalTab", "commitTab", "mergeTab", "aboutTab")
            }
            doWhenLinkClicked("mergeTabControl") {
                activateTab("mergeTab", "generalTab", "commitTab", "refsTab", "aboutTab")
            }
            doWhenLinkClicked("aboutTabControl") {
                activateTab("aboutTab", "generalTab", "commitTab", "mergeTab", "refsTab")
            }

            doWhenButtonClicked("clearGraphButton") {
                ConfirmationDialog.showConfirmationDialog("Are you sure?",
                    "Do you really want to reset your graph and start over?") {
                    canvas.clear()
                    val newGraph = GitGraph(canvas)
                    newGraph.initGraph()
                    gitGraph = newGraph
                }
            }

            doWhenButtonClicked("addCommitButton") {
                gitGraph.addCommit()
            }

            doWhenButtonClicked("checkoutCommitButton") {
                gitGraph.checkout(getUserInput("checkoutCommitInput"))
            }

            doWhenButtonClicked("addBranchButton") {
                gitGraph.addBranch(getUserInput("addBranchInput"))
            }

            doWhenButtonClicked("checkoutBranchButton") {
                gitGraph.checkout(getUserInput("checkoutBranchInput"))
            }

            doWhenButtonClicked("addTagButton") {
                gitGraph.addTag(getUserInput("addTagInput"))
            }

            doWhenButtonClicked("mergeBranchButton") {
                gitGraph.merge(getUserInput("mergeBranchInput"))
            }
        }
    }

    canvas("fullscreenCanvas") {
        attrs {
            id = "gitKannWas"
            width = ""
            height = ""
        }
    }
}