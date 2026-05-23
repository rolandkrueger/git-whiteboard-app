import config.GitGraphConfiguration
import graph.AbstractBranch
import graph.GitGraph
import konvajs.Layer
import konvajs.Stage
import konvajs.StageConfig
import components.jso
import kotlinx.browser.document
import kotlinx.browser.window
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
    // Create a container div for Konva and append it to the root element
    val containerDiv = document.createElement("div")
    containerDiv.id = "konvaContainer"
    containerDiv.setAttribute("class", "fullscreenCanvas")
    document.getElementById("root")?.appendChild(containerDiv)

    val stage = Stage(
        jso<StageConfig> {
            container = "konvaContainer"
            width = window.innerWidth
            height = window.innerHeight
        }
    )

    val originLayer = Layer()
    val aliceLayer = Layer()
    stage.add(originLayer.asDynamic())
    stage.add(aliceLayer.asDynamic())

    var isDragging = false
    var lastPosX = 0.0
    var lastPosY = 0.0
    var dragStartX = 0.0
    var dragStartY = 0.0

    stage.on("mousedown") {
        val event = it.evt as MouseEvent
        isDragging = true
        lastPosX = event.clientX.toDouble()
        lastPosY = event.clientY.toDouble()
        dragStartX = event.clientX.toDouble()
        dragStartY = event.clientY.toDouble()
    }

    stage.on("mousemove") {
        if (isDragging) {
            val event = it.evt as MouseEvent
            val dx = event.clientX.toDouble() - lastPosX
            val dy = event.clientY.toDouble() - lastPosY
            stage.x(stage.x().toDouble() + dx)
            stage.y(stage.y().toDouble() + dy)
            stage.batchDraw()
            lastPosX = event.clientX.toDouble()
            lastPosY = event.clientY.toDouble()
        }
    }

    // Wheel zoom: Ctrl+scroll
    stage.getContainer().addEventListener("wheel", { evt ->
        val event = evt as WheelEvent
        if (event.ctrlKey) {
            event.preventDefault()
            val delta = event.deltaY
            var scaleX = stage.scaleX().toDouble()
            scaleX += if (delta > 0) -0.06 else 0.06
            if (scaleX > 3.0) scaleX = 3.0
            if (scaleX < 0.2) scaleX = 0.2

            val pointer = stage.getPointerPosition()
            if (pointer != null) {
                val mousePointTo = jso<konvajs.Vector2d> {
                    x = (pointer.x.toDouble() - stage.x().toDouble()) / stage.scaleX().toDouble()
                    y = (pointer.y.toDouble() - stage.y().toDouble()) / stage.scaleY().toDouble()
                }
                stage.scaleX(scaleX)
                stage.scaleY(scaleX)
                stage.x(pointer.x.toDouble() - mousePointTo.x.toDouble() * scaleX)
                stage.y(pointer.y.toDouble() - mousePointTo.y.toDouble() * scaleX)
            } else {
                stage.scaleX(scaleX)
                stage.scaleY(scaleX)
            }
            stage.batchDraw()
        }
    })

    // Repo "origin" at default position; repo "alice" offset to the right
    val repoSeparation = 800
    val originConfig = GitGraphConfiguration(xOffset = 0, yOffset = 0)
    val aliceConfig = GitGraphConfiguration(xOffset = repoSeparation, yOffset = 0)

    val originGraph = GitGraph(originLayer, repoName = "origin", config = originConfig)
    val aliceGraph = GitGraph(aliceLayer, repoName = "alice", config = aliceConfig)

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

    stage.on("mouseup") {
        isDragging = false

        // Distinguish a click from a drag: only handle repo selection if the mouse barely moved
        val event = it.evt as MouseEvent
        val dx = event.clientX.toDouble() - dragStartX
        val dy = event.clientY.toDouble() - dragStartY
        val movedDistance = dx * dx + dy * dy
        if (movedDistance < 25) { // less than ~5px movement = click, not drag
            val pointer = stage.getPointerPosition()
            if (pointer != null) {
                // Convert stage-relative pointer to the unscaled world coordinate
                val px = (pointer.x.toDouble() - stage.x().toDouble()) / stage.scaleX().toDouble()
                val py = (pointer.y.toDouble() - stage.y().toDouble()) / stage.scaleY().toDouble()
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
            activeGraph.clearGraph()
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
