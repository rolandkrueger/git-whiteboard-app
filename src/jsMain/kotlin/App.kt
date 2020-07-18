import fabricjs.FabricCanvas
import graph.AbstractBranch
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
import ui.UiControl
import ui.UiControl.Companion.activateTab
import ui.UiControl.Companion.doWhenButtonClicked
import ui.UiControl.Companion.doWhenCheckboxClicked
import ui.UiControl.Companion.doWhenLinkClicked
import ui.UiControl.Companion.getSelectedOption
import ui.UiControl.Companion.getUserInput
import ui.UiControl.Companion.hideElements
import ui.UiControl.Companion.isCheckboxChecked
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
                    if (zoom < 0.2) zoom = 0.2
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

            doWhenButtonClicked("runGCButton") {
                gitGraph.runGarbageCollection()
            }

            doWhenButtonClicked("clearGraphButton") {
                ConfirmationDialog.showConfirmationDialog(
                    "Are you sure?",
                    "Do you really want to reset your graph and start over?"
                ) {
                    canvas.clear()
                    val newGraph = GitGraph(canvas)
                    newGraph.initGraph()
                    gitGraph = newGraph
                }
            }

            doWhenButtonClicked("addCommitButton") {
                gitGraph.addCommit()
            }

            doWhenButtonClicked("amendCommitButton") {
                gitGraph.amendCommit()
            }

            doWhenButtonClicked("cherryPickButton") {
                val commitId = getUserInput("cherryPickInput")
                if (!gitGraph.doesCommitExist(commitId)) {
                    ConfirmationDialog.showMessageDialog(
                        "Commit does not exist",
                        "Unable to cherry-pick '$commitId': commit does not exist."
                    )
                } else {
                    gitGraph.addCommit(commitIdSuffix = "($commitId)", commitColor = gitGraph.findCommitFor(commitId)?.commitColor)
                }
            }

            doWhenButtonClicked("checkoutCommitButton") {
                gitGraph.checkout(getUserInput("checkoutCommitInput"), isCheckboxChecked("showLostCommitsCheckbox"))
            }

            doWhenButtonClicked("addBranchButton") {
                val branchName = getUserInput("addBranchInput").replace(" ", "_")
                if (!gitGraph.isBranchNameValid(branchName)) {
                    ConfirmationDialog.showMessageDialog(
                        "Branch name '$branchName' is invalid",
                        "The name of the branch starts with a letter that is already used by another branch or " +
                                "by the HEAD reference as first letter. Namespaces (feature/*, bugfix/*) are not considered. "
                    )
                } else {
                    gitGraph.addBranch(branchName)
                    updateBranchSelects(gitGraph.getBranches())
                }
            }

            doWhenButtonClicked("checkoutBranchButton") {
                gitGraph.checkout(
                    getSelectedOption("checkoutBranchInput"),
                    isCheckboxChecked("showLostCommitsCheckbox")
                )
            }

            doWhenButtonClicked("deleteBranchButton") {
                val branchName = getSelectedOption("deleteBranchInput")
                if (gitGraph.isBranchCheckedOut(branchName)) {
                    // deleting checked out branches is not allowed by Git
                    ConfirmationDialog.showMessageDialog(
                        "Cannot delete checked out branch",
                        "Cannot delete branch which is currently checked out."
                    )
                } else {
                    gitGraph.deleteBranch(branchName)
                    updateBranchSelects(gitGraph.getBranches())
                }
            }

            doWhenButtonClicked("resetBranchButton") {
                gitGraph.resetBranch(getUserInput("resetBranchInput"))
            }

            doWhenButtonClicked("addTagButton") {
                val tagName = getUserInput("addTagInput").replace(" ", "_")
                if (gitGraph.doesTagExist(tagName)) {
                    ConfirmationDialog.showMessageDialog(
                        "Tag already exists",
                        "Cannot create tag $tagName: tag already exists."
                    )
                } else {
                    gitGraph.addTag(tagName)
                    updateTagSelects(gitGraph.getTags())
                }
            }

            doWhenButtonClicked("deleteTagButton") {
                gitGraph.deleteTag(getSelectedOption("deleteTagInput"))
                updateTagSelects(gitGraph.getTags())
            }

            doWhenButtonClicked("mergeBranchButton") {
                if (!gitGraph.merge(
                        isCheckboxChecked("noFFCheckbox"),
                        getSelectedOption("mergeBranchInput")
                    )
                ) {
                    ConfirmationDialog.showMessageDialog("Already up-to-date.", "Branch does not need to be merged.")
                }
            }

            doWhenButtonClicked("rebaseBranchButton") {
                if (!gitGraph.rebase(getSelectedOption("rebaseBranchInput"))) {
                    ConfirmationDialog.showMessageDialog("Already up-to-date.", "Branch cannot be rebased.")
                }
            }

            doWhenCheckboxClicked("showLostCommitsCheckbox", gitGraph::showLostCommits)
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

fun updateBranchSelects(branches: List<AbstractBranch>) {
    val branchNames = branches
        .map { it.id }
        .filter { it != "HEAD" }

    UiControl.setSelectOptions("checkoutBranchInput", branchNames)
    UiControl.setSelectOptions("deleteBranchInput", branchNames)
    UiControl.setSelectOptions("mergeBranchInput", branchNames)
    UiControl.setSelectOptions("rebaseBranchInput", branchNames)
}

fun updateTagSelects(tags: List<AbstractBranch>) {
    val tagNames = tags.map { it.id }
    UiControl.setSelectOptions("deleteTagInput", tagNames)

}
