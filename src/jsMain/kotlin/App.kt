import config.GitGraphConfiguration
import fabricjs.FabricCanvas
import graph.AbstractBranch
import graph.GitGraph
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
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

fun startApp() {
    val canvasElement = document.createElement("canvas") as HTMLCanvasElement
    canvasElement.id = "gitKannWas"
    canvasElement.setAttribute("class", "fullscreenCanvas")
    canvasElement.width = window.innerWidth
    canvasElement.height = window.innerHeight
    document.getElementById("root")?.appendChild(canvasElement)

    val canvas = FabricCanvas("gitKannWas")

    var isDragging = false
    var lastPosX = 0
    var lastPosY = 0
    var dragStartX = 0
    var dragStartY = 0

    canvas.on("mouse:down") {
        val event = it.e as MouseEvent
        isDragging = true
        canvas.selection = false
        lastPosX = event.clientX
        lastPosY = event.clientY
        dragStartX = event.clientX
        dragStartY = event.clientY
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

    // Repo "origin" at default position; repo "alice" offset to the right
    val repoSeparation = 800
    val originConfig = GitGraphConfiguration(xOffset = 0, yOffset = 0)
    val aliceConfig = GitGraphConfiguration(xOffset = repoSeparation, yOffset = 0)

    val originGraph = GitGraph(canvas, repoName = "origin", config = originConfig)
    val aliceGraph = GitGraph(canvas, repoName = "alice", config = aliceConfig)

    originGraph.initGraph()
    aliceGraph.initGraph()

    // origin is active by default
    var activeGraph = originGraph
    originGraph.isActive = true

    // Update the active-repo indicator in the control panel header
    fun updateActiveRepoLabel() {
        document.getElementById("activeRepoLabel")?.textContent = "Active: ${activeGraph.repoName}"
    }
    updateActiveRepoLabel()

    canvas.on("mouse:up") {
        isDragging = false
        canvas.selection = true
        canvas.setZoom(canvas.getZoom())

        // Distinguish a click from a drag: only handle repo selection if the mouse barely moved
        val event = it.e as MouseEvent
        val dx = event.clientX - dragStartX
        val dy = event.clientY - dragStartY
        val movedDistance = dx * dx + dy * dy
        if (movedDistance < 25) { // less than ~5px movement = click, not drag
            // Use absolutePointer from the Fabric event to get canvas coordinates
            val pointer = it.absolutePointer
            if (pointer != null) {
                val px = pointer.x.toDouble()
                val py = pointer.y.toDouble()
                val clickedGraph = when {
                    aliceGraph.containsPoint(px, py) -> aliceGraph
                    originGraph.containsPoint(px, py) -> originGraph
                    else -> null
                }
                if (clickedGraph != null && clickedGraph != activeGraph) {
                    activeGraph.isActive = false
                    activeGraph = clickedGraph
                    activeGraph.isActive = true
                    updateActiveRepoLabel()
                    updateBranchSelects(activeGraph.getBranches())
                    updateTagSelects(activeGraph.getTags())
                }
            }
        }
    }

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
        activeGraph.runGarbageCollection()
    }

    doWhenButtonClicked("clearGraphButton") {
        ConfirmationDialog.showConfirmationDialog(
            "Are you sure?",
            "Do you really want to reset the active repo graph and start over?"
        ) {
            activeGraph.clearGraph(canvas)
            updateBranchSelects(activeGraph.getBranches())
            updateTagSelects(activeGraph.getTags())
        }
    }

    doWhenButtonClicked("addCommitButton") {
        activeGraph.addCommit()
    }

    doWhenButtonClicked("amendCommitButton") {
        activeGraph.amendCommit()
    }

    doWhenButtonClicked("cherryPickButton") {
        val commitId = getUserInput("cherryPickInput")
        if (!activeGraph.doesCommitExist(commitId)) {
            ConfirmationDialog.showMessageDialog(
                "Commit does not exist",
                "Unable to cherry-pick '$commitId': commit does not exist."
            )
        } else {
            activeGraph.addCommit(commitIdSuffix = "($commitId)", commitColor = activeGraph.findCommitFor(commitId)?.commitColor)
        }
    }

    doWhenButtonClicked("checkoutCommitButton") {
        activeGraph.checkout(getUserInput("checkoutCommitInput"), isCheckboxChecked("showLostCommitsCheckbox"))
    }

    doWhenButtonClicked("addBranchButton") {
        val branchName = getUserInput("addBranchInput").replace(" ", "_")
        if (!activeGraph.isBranchNameValid(branchName)) {
            ConfirmationDialog.showMessageDialog(
                "Branch name '$branchName' is invalid",
                "The name of the branch starts with a letter that is already used by another branch or " +
                        "by the HEAD reference as first letter. Namespaces (feature/*, bugfix/*) are not considered. "
            )
        } else {
            activeGraph.addBranch(branchName)
            updateBranchSelects(activeGraph.getBranches())
        }
    }

    doWhenButtonClicked("checkoutBranchButton") {
        activeGraph.checkout(
            getSelectedOption("checkoutBranchInput"),
            isCheckboxChecked("showLostCommitsCheckbox")
        )
    }

    doWhenButtonClicked("deleteBranchButton") {
        val branchName = getSelectedOption("deleteBranchInput")
        if (activeGraph.isBranchCheckedOut(branchName)) {
            ConfirmationDialog.showMessageDialog(
                "Cannot delete checked out branch",
                "Cannot delete branch which is currently checked out."
            )
        } else {
            activeGraph.deleteBranch(branchName)
            updateBranchSelects(activeGraph.getBranches())
        }
    }

    doWhenButtonClicked("resetBranchButton") {
        activeGraph.resetBranch(getUserInput("resetBranchInput"))
    }

    doWhenButtonClicked("addTagButton") {
        val tagName = getUserInput("addTagInput").replace(" ", "_")
        if (activeGraph.doesTagExist(tagName)) {
            ConfirmationDialog.showMessageDialog(
                "Tag already exists",
                "Cannot create tag $tagName: tag already exists."
            )
        } else {
            activeGraph.addTag(tagName)
            updateTagSelects(activeGraph.getTags())
        }
    }

    doWhenButtonClicked("checkoutTagButton") {
        activeGraph.checkout(
            getSelectedOption("checkoutTagInput"),
            isCheckboxChecked("showLostCommitsCheckbox")
        )
    }

    doWhenButtonClicked("deleteTagButton") {
        activeGraph.deleteTag(getSelectedOption("deleteTagInput"))
        updateTagSelects(activeGraph.getTags())
    }

    doWhenButtonClicked("mergeBranchButton") {
        if (!activeGraph.merge(
                isCheckboxChecked("noFFCheckbox"),
                getSelectedOption("mergeBranchInput")
            )
        ) {
            ConfirmationDialog.showMessageDialog("Already up-to-date.", "Branch does not need to be merged.")
        }
    }

    doWhenButtonClicked("rebaseBranchButton") {
        if (!activeGraph.rebase(getSelectedOption("rebaseBranchInput"))) {
            ConfirmationDialog.showMessageDialog("Already up-to-date.", "Branch cannot be rebased.")
        }
    }

    doWhenCheckboxClicked("showLostCommitsCheckbox") { activeGraph.showLostCommits(it) }

    // Initialise branch/tag selects for the active (origin) repo
    updateBranchSelects(activeGraph.getBranches())
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
    UiControl.setSelectOptions("checkoutTagInput", tagNames)
}
