import components.*
import config.GitGraphConfiguration
import fabricjs.FabricCanvas
import fabricjs.Point
import fabricjs.plus
import graph.GitGraph
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import react.*
import react.dom.canvas
import react.dom.div
import react.dom.input
import kotlin.browser.document

val scope = MainScope()

val App = functionalComponent<RProps> { _ ->
    val (currentGraph, setGraph) = useState(GitGraph())
    val (currentCanvas, setCanvas) = useState(FabricCanvas(""))

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
            resetGraph(setGraph, canvas)
            renderGraph(currentGraph, canvas)
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
                                renderGraph(currentGraph, currentCanvas)
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
                                            renderGraph(currentGraph, currentCanvas)
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
                                            renderGraph(currentGraph, currentCanvas)
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
                                            renderGraph(currentGraph, currentCanvas)
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
                                            renderGraph(currentGraph, currentCanvas)
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
                                resetGraph(setGraph, currentCanvas)
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

private fun renderGraph(graph: GitGraph, canvas: FabricCanvas) {
    canvas.clear()

    graph.calculateLostCommits()
    graph.commits.forEach { commit ->
        val parent = commit.parent
        if (parent != null) {
            if (commit.swimlane == parent.swimlane) {
                Line(
                    parent.commitCircle.getUpperDockPoint(),
                    commit.commitCircle.getLowerDockPoint()
                ).render(canvas)
            } else {
                Line(
                    parent.commitCircle.getRightDockPoint(),
                    commit.commitCircle.getLowerDockPoint()
                ).render(canvas)
            }
            val mergedCommit = commit.mergedCommit
            if (mergedCommit != null) {
                Line(mergedCommit.commitCircle.getUpperDockPoint(), commit.commitCircle.getLowerDockPoint()).render(canvas)
            }
        }
        commit.commitCircle.render(canvas)
        commit.commitCircle.onDoubleClick {
            graph.checkout(commit.id)
            renderGraph(graph, canvas)
        }

        val labelOffset = Point(20, -1 * CommitCircle.RADIUS + CommitLabel.LABEL_HEIGHT / 2)

        graph.commits.forEach {
            it.branches.forEachIndexed { index, branch ->
                if (branch != graph.head) {
                    val yOffset = index * (CommitLabel.LABEL_HEIGHT + 5)
                    val branchCommitCircle = it.commitCircle
                    val labelText = if (branch == graph.head.targetBranch) "*${branch.id}*" else branch.id
                    val label = BranchLabel(
                        labelText,
                        branchCommitCircle.getRightDockPoint() + labelOffset + Point(0, yOffset),
                        branch == graph.head.targetBranch
                    )
                    label.render(canvas)
                    label.onDoubleClick {
                        graph.checkout(branch.id)
                        renderGraph(graph, canvas)
                    }
                    Line(branchCommitCircle.getRightDockPoint(), label.getLeftDockPoint()).render(canvas)

                    val headBranch = graph.head.targetBranch
                    if (headBranch != null && headBranch == branch) {
                        val headLabel =
                            HeadLabel(label.getRightDockPoint() + Point(15, CommitLabel.LABEL_HEIGHT / -2))
                        headLabel.render(canvas)
                        Line(label.getRightDockPoint(), headLabel.getLeftDockPoint()).render(canvas)
                    }
                }
            }
        }

        val headBranch = graph.head.targetBranch
        if (headBranch == null) {
            val headCommit = graph.head.commit
            val yOffset = if (headCommit.branches.size > 0) {
                (headCommit.branches.size - 1) * (CommitLabel.LABEL_HEIGHT + 5)
            } else {
                0
            }

            val headLabel = HeadLabel(headCommit.commitCircle.getRightDockPoint() + labelOffset + Point(0, yOffset))
            headLabel.render(canvas)
            Line(headCommit.commitCircle.getRightDockPoint(), headLabel.getLeftDockPoint()).render(canvas)
        }

        graph.tags.forEach { tag ->
            val tagLabel = TagLabel(
                tag.id,
                tag.commit.commitCircle.getRightDockPoint() + labelOffset
                        + Point(0, tag.commit.branches.size * (CommitLabel.LABEL_HEIGHT + 5))
            )
            tagLabel.render(canvas)
            tagLabel.onDoubleClick {
                graph.checkout(tag.commit.id)
                renderGraph(graph, canvas)
            }
            Line(tag.commit.commitCircle.getRightDockPoint(), tagLabel.getLeftDockPoint()).render(canvas)
        }
    }
}

private fun resetGraph(setGraph: RSetState<GitGraph>, canvas: FabricCanvas) {
    val gitGraph = GitGraph()
    setGraph(gitGraph)
    renderGraph(gitGraph, canvas)
}
