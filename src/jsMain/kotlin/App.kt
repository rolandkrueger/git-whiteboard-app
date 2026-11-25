import config.GitGraphConfiguration
import graph.AbstractBranch
import graph.CommitIdAllocator
import graph.GitGraph
import graph.RemoteBranch
import konvajs.Layer
import konvajs.Line
import konvajs.LineConfig
import konvajs.Text
import konvajs.TextConfig
import konvajs.Stage
import konvajs.StageConfig
import components.jso
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import scenario.MultiRepoSnapshot
import scenario.ScenarioSerializer
import scenario.compressToBase64Url
import scenario.decompressFromBase64Url
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

    val repoSeparation = 800

    // Connector layer sits below all repo layers; its shapes never listen, so clicks and panning pass through.
    val connectorLayer = Layer()
    stage.add(connectorLayer.asDynamic())

    // Dynamic list of graphs — starts with one "my-repo" graph
    val graphs = mutableListOf<GitGraph>()
    // Single app-wide commit ID source shared by all repos (ADR 0004)
    val commitIdAllocator = CommitIdAllocator()

    var isDragging = false
    var lastPosX = 0.0
    var lastPosY = 0.0
    var dragStartX = 0.0
    var dragStartY = 0.0

    // Use native DOM events on the container for pan — Konva synthetic events are unreliable
    // when shapes with listening:true intercept the pointer, preventing bubbling to the stage.
    val container = stage.getContainer()

    container.addEventListener("mousedown", { evt ->
        val event = evt as MouseEvent
        isDragging = true
        lastPosX = event.clientX.toDouble()
        lastPosY = event.clientY.toDouble()
        dragStartX = event.clientX.toDouble()
        dragStartY = event.clientY.toDouble()
    })

    container.addEventListener("mousemove", { evt ->
        if (isDragging) {
            val event = evt as MouseEvent
            val dx = event.clientX.toDouble() - lastPosX
            val dy = event.clientY.toDouble() - lastPosY
            stage.x(stage.x().toDouble() + dx)
            stage.y(stage.y().toDouble() + dy)
            stage.batchDraw()
            lastPosX = event.clientX.toDouble()
            lastPosY = event.clientY.toDouble()
        }
    })

    // Clear drag state when pointer leaves the canvas — prevents sticky pan
    container.addEventListener("mouseleave", {
        isDragging = false
    })

    // Wheel zoom: Ctrl+scroll
    container.addEventListener("wheel", { evt ->
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

    // Update the active-repo indicator in the control panel header
    fun updateActiveRepoLabel(activeGraph: GitGraph) {
        document.getElementById("activeRepoLabel")?.textContent = "Active: ${activeGraph.repoName}"
        (document.getElementById("repoNameInput") as? HTMLInputElement)?.value = activeGraph.repoName
        document.getElementById("repoNameError")?.classList?.add("hidden")
    }

    // Update the "Add Repository" button disabled state based on graph count
    fun updateAddRepoButtonState() {
        val btn = document.getElementById("addRepoButton") as? HTMLButtonElement
        btn?.disabled = graphs.size >= 3
        val cloneBtn = document.getElementById("cloneRepoButton") as? HTMLButtonElement
        cloneBtn?.disabled = graphs.size >= 3
        cloneBtn?.title = if (graphs.size >= 3) "Maximum of 3 repositories reached" else "Clone the active repository"
        document.getElementById("cloneRepoHint")?.classList?.toggle("hidden", graphs.size < 3)
    }

    var nextRepoId = 1

    // Holds the currently active graph
    lateinit var activeGraph: GitGraph

    // A repo waiting on a conflict refuses every other graph operation (ADR 0006).
    fun refuseWhilePending(g: GitGraph): Boolean {
        if (g.pending == null) return false
        ConfirmationDialog.showMessageDialog("Conflict pending", graph.RemoteOps.RESOLVE_FIRST)
        return true
    }
    GitGraph.onCheckoutRefused = { ConfirmationDialog.showMessageDialog("Cannot check out", it) }
    GitGraph.onPendingRefused = { ConfirmationDialog.showMessageDialog("Conflict pending", graph.RemoteOps.RESOLVE_FIRST) }
    fun doWhenGuardedButtonClicked(buttonId: String, handler: () -> Unit) {
        doWhenButtonClicked(buttonId) { if (!refuseWhilePending(activeGraph)) handler() }
    }
    // Forward reference: resolveRegionOverlaps is defined after refreshRemoteLinks, which schedules it.
    var resolveRegionOverlapsRef: () -> Unit = {}
    var layoutResolutionPending = false

    // Create a new GitGraph with its own Layer at the given xOffset and add it to the stage/list
    fun createGraph(repoName: String, xOffset: Int, yOffset: Int = 0): GitGraph {
        val layer = Layer()
        stage.add(layer.asDynamic())
        val config = GitGraphConfiguration(xOffset = xOffset, yOffset = yOffset)
        return GitGraph(layer, commitIdAllocator, repoName = repoName, config = config).also { it.repoId = nextRepoId++ }
    }

    // Applies the remote cues (region styling, header badges, origin lines) from the remoteRepoId links.
    fun refreshRemoteLinks() {
        connectorLayer.destroyChildren()
        for (g in graphs) {
            g.onRegionBoundsChanged = {
                refreshRemoteLinks()
                // The callback fires mid-operation; rebuild graphs only after the operation has finished.
                if (!layoutResolutionPending) {
                    layoutResolutionPending = true
                    window.setTimeout({
                        layoutResolutionPending = false
                        resolveRegionOverlapsRef()
                    }, 0)
                }
            }
            val origin = g.remoteRepoId?.let { rid -> graphs.find { it.repoId == rid } }
            g.setRemoteLinks(graphs.any { it.remoteRepoId == g.repoId }, origin?.repoName)
            if (origin == null) continue
            val (ol, ot, ow, oh) = origin.regionBounds
            val (cl, ct, cw, ch) = g.regionBounds
            val pts: Array<Number> = if (ct >= ot + oh) {
                arrayOf(ol + ow / 2, ot + oh, cl + cw / 2, ct)
            } else if (cl >= ol + ow) {
                arrayOf(ol + ow, ot + oh / 2, cl, ct + ch / 2)
            } else if (cl + cw <= ol) {
                arrayOf(ol, ot + oh / 2, cl + cw, ct + ch / 2)
            } else {
                arrayOf(ol + ow / 2, ot, cl + cw / 2, ct + ch)
            }
            connectorLayer.add(Line(jso<LineConfig> {
                points = pts
                stroke = "#B7791F"
                strokeWidth = 3
                listening = false
            }).asDynamic())
            connectorLayer.add(Text(jso<TextConfig> {
                x = (pts[0].toInt() + pts[2].toInt()) / 2 + 10
                y = (pts[1].toInt() + pts[3].toInt()) / 2 - 38
                text = "origin"
                fontSize = 32
                fontStyle = "bold"
                fontFamily = "Courier New"
                fill = "#B7791F"
                listening = false
            }).asDynamic())
        }
        connectorLayer.batchDraw()
    }

    // Rebuilds a graph at new offsets (snapshot + restore into a fresh layer); keeps identity, links and view state.
    fun rebuildGraph(old: GitGraph, xOffset: Int, yOffset: Int): GitGraph {
        val index = graphs.indexOf(old)
        val moved = createGraph(old.repoName, xOffset, yOffset)
        moved.restoreFromSnapshot(old.getSnapshot().copy(xOffset = xOffset, yOffset = yOffset))
        moved.repoId = old.repoId
        moved.remoteRepoId = old.remoteRepoId
        moved.wasCleared = old.wasCleared
        moved.showLostCommits(old.lostCommitsShown)
        // The snapshot does not carry a pending conflict; a growth re-layout must not drop it.
        old.pending?.let { moved.restorePending(it) }
        val wasActive = activeGraph === old
        old.destroy()
        graphs[index] = moved
        if (wasActive) {
            activeGraph = moved
            moved.isActive = true
        }
        return moved
    }

    // Pushes regions apart (ADR 0001): of a colliding pair only the lower / right region moves, down or right.
    fun resolveRegionOverlaps() {
        val vGap = GitGraphConfiguration.cloneVerticalGap
        val hGap = GitGraphConfiguration.regionHorizontalClearance
        // Mutable rects [left, top, right, bottom] per graph, plus accumulated shifts [dx, dy].
        val rects = graphs.map { g -> g.regionBounds.let { (l, t, w, h) -> intArrayOf(l, t, l + w, t + h) } }
        val shifts = graphs.map { intArrayOf(0, 0) }
        var changed = true
        var guard = 0
        while (changed && guard++ < 20) {
            changed = false
            for (i in graphs.indices) for (j in graphs.indices) {
                if (i == j) continue
                val a = rects[i]
                val b = rects[j]
                val bLower = b[3] > a[3] || (b[3] == a[3] && b[0] > a[0])
                if (!bLower) continue
                val horizontalOverlap = a[0] - hGap < b[2] && b[0] < a[2] + hGap
                if (!horizontalOverlap) continue
                if (b[3] > a[3]) {
                    val dy = a[3] + vGap - b[1]
                    if (dy > 0) {
                        b[1] += dy; b[3] += dy; shifts[j][1] += dy; changed = true
                    }
                } else if (b[1] < a[3] && a[1] < b[3]) {
                    val dx = a[2] + hGap - b[0]
                    if (dx > 0) {
                        b[0] += dx; b[2] += dx; shifts[j][0] += dx; changed = true
                    }
                }
            }
        }
        var anyMoved = false
        for (i in graphs.indices.toList()) {
            val (dx, dy) = shifts[i].let { it[0] to it[1] }
            if (dx == 0 && dy == 0) continue
            val g = graphs[i]
            rebuildGraph(g, g.xOffset + dx, g.yOffset + dy)
            anyMoved = true
        }
        if (anyMoved) refreshRemoteLinks()
    }

    resolveRegionOverlapsRef = { resolveRegionOverlaps() }

    // ---- "Add Repository" dialog logic ----
    fun showAddRepoDialog(onNewGraph: (GitGraph) -> Unit) {
        val dialog = document.getElementById("addRepoDialog") ?: return
        val nameInput = document.getElementById("addRepoNameInput") as? HTMLInputElement ?: return
        val confirmBtn = document.getElementById("addRepoConfirmButton") as? HTMLButtonElement ?: return
        val errorLabel = document.getElementById("addRepoNameError") ?: return

        nameInput.value = ""
        confirmBtn.disabled = true
        errorLabel.textContent = ""
        errorLabel.classList.add("hidden")

        dialog.classList.add("is-active")
        nameInput.focus()

        fun validateName(): Boolean {
            val name = nameInput.value.trim()
            return when {
                name.isEmpty() -> false
                graphs.any { it.repoName == name } -> false
                else -> true
            }
        }

        fun showError(msg: String) {
            errorLabel.textContent = msg
            errorLabel.classList.remove("hidden")
        }

        fun hideError() {
            errorLabel.textContent = ""
            errorLabel.classList.add("hidden")
        }

        nameInput.oninput = { _: Event ->
            val name = nameInput.value.trim()
            when {
                name.isEmpty() -> {
                    hideError()
                    confirmBtn.disabled = true
                }
                graphs.any { it.repoName == name } -> {
                    showError("A repository named '$name' already exists.")
                    confirmBtn.disabled = true
                }
                else -> {
                    hideError()
                    confirmBtn.disabled = false
                }
            }
        }

        fun closeDialog() {
            dialog.classList.remove("is-active")
            nameInput.oninput = null
        }

        val cancelBtn = document.getElementById("addRepoCancelButton") as? HTMLButtonElement
        cancelBtn?.onclick = { closeDialog() }

        confirmBtn.onclick = {
            if (validateName()) {
                val name = nameInput.value.trim()
                val xOffset = graphs.last().xOffset + repoSeparation
                val newGraph = createGraph(name, xOffset)
                newGraph.initGraph()
                graphs.add(newGraph)
                updateAddRepoButtonState()
                onNewGraph(newGraph)
                closeDialog()
            }
        }
    }

    // Refresh the Remote tab from the active repo; textContent is only touched when the text changes.
    fun updateRemoteTab(g: GitGraph) {
        fun setText(id: String, text: String) {
            val el = document.getElementById(id) ?: return
            if (el.textContent != text) el.textContent = text
        }
        val remote = g.remoteRepoId?.let { rid -> graphs.find { it.repoId == rid } }
        val status = g.upstreamStatus()
        setText("remoteStatus", when {
            remote == null -> "no remote"
            !graph.RemoteOps.isAvailable(remote) -> "origin: ${remote.repoName} (not available)"
            else -> "origin: ${remote.repoName}"
        })
        setText("upstreamStatus", when {
            status.detached -> "detached HEAD - no upstream"
            status.upstream != null -> "${status.branchName} -> ${status.upstream}"
            else -> "no upstream"
        })
        (document.getElementById("pushButton") as? org.w3c.dom.HTMLButtonElement)?.let {
            val canPush = remote != null && !status.detached && status.branchName != null
            if (it.disabled == canPush) it.disabled = !canPush
        }
        (document.getElementById("fetchButton") as? org.w3c.dom.HTMLButtonElement)?.let {
            val canFetch = remote != null
            if (it.disabled == canFetch) it.disabled = !canFetch
        }
        (document.getElementById("pullButton") as? org.w3c.dom.HTMLButtonElement)?.let {
            val canPull = remote != null && !status.detached && status.branchName != null && status.upstream != null
            if (it.disabled == canPull) it.disabled = !canPull
            (document.getElementById("pullRebaseCheckbox") as? org.w3c.dom.HTMLInputElement)?.let { cb ->
                if (cb.disabled == canPull) cb.disabled = !canPull
            }
        }
        setText("aheadBehindStatus", if (status.upstream != null) "ahead ${status.ahead}, behind ${status.behind}" else "-")
    }

    // ---- Startup: restore from URL fragment if present, otherwise init blank graph ----

    // Always create the initial "my-repo" graph synchronously so activeGraph is defined.
    // This graph is either initialized normally or replaced by the hash-restore callback.
    val initialLayer = Layer()
    stage.add(initialLayer.asDynamic())
    val initialGraph = GitGraph(initialLayer, commitIdAllocator, repoName = "my-repo",
        config = GitGraphConfiguration(xOffset = 0, yOffset = 0))
    activeGraph = initialGraph

    // Graph changes come from many places (buttons, canvas double-click, restore); a cheap poll keeps the tab current.
    // Continue/Abort are shown only while the active repo has a pending conflict.
    fun updatePendingControls(g: GitGraph) {
        val pending = g.pending
        document.getElementById("pendingControls")?.classList?.toggle("hidden", pending == null)
        val msg = document.getElementById("pendingMessage")
        val text = pending?.message ?: ""
        if (msg != null && msg.textContent != text) msg.textContent = text
    }

    window.setInterval({ updateRemoteTab(activeGraph); updatePendingControls(activeGraph) }, 300)

    fun showRemoteResult(result: graph.RemoteOps.Result) {
        val msg = document.getElementById("remoteMessage")
        if (msg != null) {
            msg.textContent = result.message
            msg.classList.remove("is-danger", "is-success", "is-info")
            msg.classList.add(when (result.kind) {
                graph.RemoteOps.Kind.ERROR -> "is-danger"
                graph.RemoteOps.Kind.SUCCESS -> "is-success"
                graph.RemoteOps.Kind.INFO -> "is-info"
            })
            if (result.kind == graph.RemoteOps.Kind.ERROR) msg.classList.add("has-text-weight-bold")
            else msg.classList.remove("has-text-weight-bold")
        }
        updateRemoteTab(activeGraph)
        updatePendingControls(activeGraph)
        updateBranchSelects(activeGraph.getBranches(), activeGraph.getRemoteBranches())
    }

    document.getElementById("pushButton")?.addEventListener("click", {
        val remote = activeGraph.remoteRepoId?.let { rid -> graphs.find { it.repoId == rid } }
        showRemoteResult(graph.RemoteOps.push(activeGraph, remote))
    })

    document.getElementById("fetchButton")?.addEventListener("click", {
        val remote = activeGraph.remoteRepoId?.let { rid -> graphs.find { it.repoId == rid } }
        showRemoteResult(graph.RemoteOps.fetch(activeGraph, remote))
    })

    document.getElementById("pullButton")?.addEventListener("click", {
        val remote = activeGraph.remoteRepoId?.let { rid -> graphs.find { it.repoId == rid } }
        val rebase = (document.getElementById("pullRebaseCheckbox") as? org.w3c.dom.HTMLInputElement)?.checked == true
        showRemoteResult(graph.RemoteOps.pull(activeGraph, remote, rebase))
    })

    // Populate graphs with default state (called when no URL state is present, or as fallback)
    fun setupDefaultGraph() {
        // graphs may already contain stale entries from a failed restore; clear them
        graphs.clear()
        initialGraph.initGraph()
        graphs.add(initialGraph)
        activeGraph = initialGraph
        initialGraph.isActive = true
        updateActiveRepoLabel(activeGraph)
        updateBranchSelects(activeGraph.getBranches(), activeGraph.getRemoteBranches())
        updateTagSelects(activeGraph.getTags())
        updateAddRepoButtonState()
    }

    // Restores graphs from an encoded URL fragment; falls back to default on error
    fun restoreFromHash(encoded: String) {
        val promise = decompressFromBase64Url(encoded)
        promise.then({ json: String ->
            val snapshot: MultiRepoSnapshot? = ScenarioSerializer.decode(json)
            if (snapshot == null || snapshot.version > 2 || snapshot.repos.isEmpty()) {
                console.warn("URL state could not be decoded; falling back to blank canvas.")
                setupDefaultGraph()
                return@then
            }
            // Restore however many repos are in the snapshot (1, 2, or 3).
            // The initial graph layer is reused for the first repo to avoid an orphan layer.
            graphs.clear()
            commitIdAllocator.seed(snapshot.nextCommitId)
            for ((index, repoSnapshot) in snapshot.repos.withIndex()) {
                val graph = if (index == 0) {
                    // Reuse the pre-created layer for the first repo
                    GitGraph(initialLayer, commitIdAllocator, repoName = repoSnapshot.repoName,
                        config = GitGraphConfiguration(xOffset = repoSnapshot.xOffset, yOffset = repoSnapshot.yOffset))
                } else {
                    createGraph(repoSnapshot.repoName, xOffset = repoSnapshot.xOffset, yOffset = repoSnapshot.yOffset)
                }
                graph.repoId = index
                graph.restoreFromSnapshot(repoSnapshot)
                graphs.add(graph)
            }
            for ((index, repoSnapshot) in snapshot.repos.withIndex()) {
                graphs[index].remoteRepoId = repoSnapshot.remoteRepoIndex?.let { graphs.getOrNull(it)?.repoId }
            }
            nextRepoId = graphs.size
            activeGraph = graphs.first()
            activeGraph.isActive = true
            updateActiveRepoLabel(activeGraph)
            updateBranchSelects(activeGraph.getBranches(), activeGraph.getRemoteBranches())
            updateTagSelects(activeGraph.getTags())
            updateAddRepoButtonState()
            refreshRemoteLinks()
            resolveRegionOverlaps()
        }, { _: dynamic ->
            console.warn("URL state decompression failed; falling back to blank canvas.")
            setupDefaultGraph()
        })
    }

    val hash = window.location.hash
    val statePrefix = "#state="
    if (hash.startsWith(statePrefix)) {
        val encoded = hash.removePrefix(statePrefix)
        if (encoded.isNotEmpty()) {
            restoreFromHash(encoded)
        } else {
            setupDefaultGraph()
        }
    } else {
        setupDefaultGraph()
    }

    // Use native mouseup on window so release outside canvas still clears isDragging
    window.addEventListener("mouseup", { evt ->
        val wasDragging = isDragging
        isDragging = false

        if (!wasDragging) return@addEventListener

        // Distinguish a click from a drag: only handle repo selection if the mouse barely moved
        val event = evt as MouseEvent
        val dx = event.clientX.toDouble() - dragStartX
        val dy = event.clientY.toDouble() - dragStartY
        val movedDistance = dx * dx + dy * dy
        if (movedDistance < 25) { // less than ~5px movement = click, not drag
            // Compute canvas-relative position from native event, then apply stage transform
            val rect = container.getBoundingClientRect()
            val canvasX = event.clientX.toDouble() - rect.left
            val canvasY = event.clientY.toDouble() - rect.top
            val px = (canvasX - stage.x().toDouble()) / stage.scaleX().toDouble()
            val py = (canvasY - stage.y().toDouble()) / stage.scaleY().toDouble()
            val clickedGraph = graphs.firstOrNull { it.containsPoint(px, py) }
            if (clickedGraph != null && clickedGraph != activeGraph) {
                activeGraph.isActive = false
                activeGraph = clickedGraph
                activeGraph.isActive = true
                updateActiveRepoLabel(activeGraph)
                updateBranchSelects(activeGraph.getBranches(), activeGraph.getRemoteBranches())
                updateTagSelects(activeGraph.getTags())
            }
        }
    })

    // ---- Share button: encode current canvas state to URL fragment and clipboard ----
    doWhenButtonClicked("shareButton") {
        if (graphs.any { it.pending != null }) {
            ConfirmationDialog.showMessageDialog("Conflict pending", graph.RemoteOps.RESOLVE_FIRST)
            return@doWhenButtonClicked
        }
        val snapshot = MultiRepoSnapshot(
            version = 2,
            nextCommitId = commitIdAllocator.peekLast() + 1,
            repos = graphs.map { g ->
                g.getSnapshot().copy(remoteRepoIndex = g.remoteRepoId?.let { id -> graphs.indexOfFirst { it.repoId == id }.takeIf { it >= 0 } })
            }
        )
        val json = ScenarioSerializer.encode(snapshot)
        val compressPromise = compressToBase64Url(json)
        compressPromise.then({ encoded: String ->
            window.location.hash = "state=$encoded"
            val fullUrl = window.location.href
            window.navigator.clipboard.writeText(fullUrl).then({ _: dynamic ->
                console.log("Share URL copied to clipboard.")
            }, { err: dynamic ->
                console.warn("Could not copy to clipboard: $err")
            })
        }, { err: dynamic ->
            console.warn("Share: compression failed: $err")
        })
    }

    doWhenButtonClicked("addRepoButton") {
        showAddRepoDialog { newGraph ->
            activeGraph.isActive = false
            activeGraph = newGraph
            activeGraph.isActive = true
            updateActiveRepoLabel(activeGraph)
            updateBranchSelects(activeGraph.getBranches(), activeGraph.getRemoteBranches())
            updateTagSelects(activeGraph.getTags())
        }
    }

    doWhenGuardedButtonClicked("cloneRepoButton") {
        if (graphs.size >= 3) return@doWhenGuardedButtonClicked
        val source = activeGraph
        var cloneName = "${source.repoName}-clone"
        var suffix = 2
        while (graphs.any { it.repoName == cloneName }) {
            cloneName = "${source.repoName}-clone-$suffix"
            suffix++
        }
        // Clones go below their origin (a clone's region starts as tall as the origin's current one);  a second clone of the same origin forms a triangle with the first.
        val cloneYOffset = source.yOffset + source.currentRegionHeight + GitGraphConfiguration.cloneVerticalGap
        val siblings = graphs.filter { it.remoteRepoId == source.repoId }
        val cloneXOffset = if (siblings.isEmpty()) source.xOffset else source.xOffset + repoSeparation / 2
        siblings.firstOrNull()?.let { first ->
            rebuildGraph(first, source.xOffset - repoSeparation / 2, cloneYOffset)
        }
        val clone = createGraph(cloneName, cloneXOffset, cloneYOffset)
        clone.restoreFromSnapshot(source.buildCloneSnapshot(cloneName, cloneXOffset).copy(yOffset = cloneYOffset))
        clone.remoteRepoId = source.repoId
        graphs.add(clone)
        updateAddRepoButtonState()
        refreshRemoteLinks()
        activeGraph.isActive = false
        activeGraph = clone
        activeGraph.isActive = true
        updateActiveRepoLabel(activeGraph)
        updateBranchSelects(activeGraph.getBranches(), activeGraph.getRemoteBranches())
        updateTagSelects(activeGraph.getTags())
    }

    doWhenButtonClicked("renameRepoButton") {
        val input = document.getElementById("repoNameInput") as? HTMLInputElement ?: return@doWhenButtonClicked
        val errorLabel = document.getElementById("repoNameError") ?: return@doWhenButtonClicked
        val name = input.value.trim()
        when {
            name.isEmpty() -> {
                errorLabel.textContent = "Repository name must not be empty."
                errorLabel.classList.remove("hidden")
            }
            graphs.any { it != activeGraph && it.repoName == name } -> {
                errorLabel.textContent = "A repository named '$name' already exists."
                errorLabel.classList.remove("hidden")
            }
            else -> {
                activeGraph.renameRepository(name)
                refreshRemoteLinks()
                updateActiveRepoLabel(activeGraph)
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
        activateTab("generalTab", "commitTab", "refsTab", "mergeTab", "aboutTab", "remoteTab")
    }
    doWhenLinkClicked("commitTabControl") {
        activateTab("commitTab", "generalTab", "refsTab", "mergeTab", "aboutTab", "remoteTab")
    }
    doWhenLinkClicked("refsTabControl") {
        activateTab("refsTab", "generalTab", "commitTab", "mergeTab", "aboutTab", "remoteTab")
    }
    doWhenLinkClicked("mergeTabControl") {
        activateTab("mergeTab", "generalTab", "commitTab", "refsTab", "aboutTab", "remoteTab")
    }
    doWhenLinkClicked("remoteTabControl") {
        activateTab("remoteTab", "generalTab", "commitTab", "mergeTab", "refsTab", "aboutTab")
        updateRemoteTab(activeGraph)
    }
    doWhenLinkClicked("aboutTabControl") {
        activateTab("aboutTab", "generalTab", "commitTab", "mergeTab", "refsTab", "remoteTab")
    }

    doWhenGuardedButtonClicked("runGCButton") {
        activeGraph.runGarbageCollection()
    }

    doWhenGuardedButtonClicked("clearGraphButton") {
        ConfirmationDialog.showConfirmationDialog(
            "Are you sure?",
            "Do you really want to reset the active repo graph and start over?"
        ) {
            activeGraph.clearGraph()
            updateBranchSelects(activeGraph.getBranches(), activeGraph.getRemoteBranches())
            updateTagSelects(activeGraph.getTags())
        }
    }

    doWhenGuardedButtonClicked("addCommitButton") {
        activeGraph.addCommit(conflicting = isCheckboxChecked("conflictingChangeCheckbox"))
    }

    doWhenGuardedButtonClicked("amendCommitButton") {
        activeGraph.amendCommit(conflicting = isCheckboxChecked("conflictingChangeCheckbox"))
    }

    doWhenGuardedButtonClicked("toggleConflictMarkerButton") {
        val commitId = getUserInput("toggleConflictMarkerInput")
        if (!activeGraph.toggleConflictMarker(commitId)) {
            ConfirmationDialog.showMessageDialog(
                "Commit does not exist",
                "Unable to toggle the conflict marker of '$commitId': commit does not exist."
            )
        }
    }

    doWhenGuardedButtonClicked("cherryPickButton") {
        val commitId = getUserInput("cherryPickInput")
        if (!activeGraph.doesCommitExist(commitId)) {
            ConfirmationDialog.showMessageDialog(
                "Commit does not exist",
                "Unable to cherry-pick '$commitId': commit does not exist."
            )
        } else {
            activeGraph.addCommit(commitIdSuffix = "($commitId)", commitColor = activeGraph.findCommitFor(commitId)?.commitColor,
                conflicting = activeGraph.findCommitFor(commitId)?.conflicting ?: false)
        }
    }

    doWhenGuardedButtonClicked("checkoutCommitButton") {
        activeGraph.checkout(getUserInput("checkoutCommitInput"), isCheckboxChecked("showLostCommitsCheckbox"))
        updateBranchSelects(activeGraph.getBranches(), activeGraph.getRemoteBranches())
    }

    doWhenGuardedButtonClicked("addBranchButton") {
        val branchName = getUserInput("addBranchInput").replace(" ", "_")
        if (!activeGraph.isBranchNameValid(branchName)) {
            ConfirmationDialog.showMessageDialog(
                "Branch name '$branchName' is invalid",
                "The name of the branch starts with a letter that is already used by another branch or " +
                        "by the HEAD reference as first letter. Namespaces (feature/*, bugfix/*) are not considered. "
            )
        } else {
            activeGraph.addBranch(branchName)
            updateBranchSelects(activeGraph.getBranches(), activeGraph.getRemoteBranches())
        }
    }

    doWhenGuardedButtonClicked("checkoutBranchButton") {
        activeGraph.checkout(
            getSelectedOption("checkoutBranchInput"),
            isCheckboxChecked("showLostCommitsCheckbox")
        )
        updateBranchSelects(activeGraph.getBranches(), activeGraph.getRemoteBranches())
    }

    doWhenGuardedButtonClicked("deleteBranchButton") {
        val branchName = getSelectedOption("deleteBranchInput")
        if (activeGraph.isBranchCheckedOut(branchName)) {
            ConfirmationDialog.showMessageDialog(
                "Cannot delete checked out branch",
                "Cannot delete branch which is currently checked out."
            )
        } else {
            activeGraph.deleteBranch(branchName)
            updateBranchSelects(activeGraph.getBranches(), activeGraph.getRemoteBranches())
        }
    }

    doWhenGuardedButtonClicked("resetBranchButton") {
        activeGraph.resetBranch(getUserInput("resetBranchInput"))
    }

    doWhenGuardedButtonClicked("addTagButton") {
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

    doWhenGuardedButtonClicked("checkoutTagButton") {
        activeGraph.checkout(
            getSelectedOption("checkoutTagInput"),
            isCheckboxChecked("showLostCommitsCheckbox")
        )
    }

    doWhenGuardedButtonClicked("deleteTagButton") {
        activeGraph.deleteTag(getSelectedOption("deleteTagInput"))
        updateTagSelects(activeGraph.getTags())
    }

    doWhenGuardedButtonClicked("mergeBranchButton") {
        if (!activeGraph.merge(
                isCheckboxChecked("noFFCheckbox"),
                getSelectedOption("mergeBranchInput")
            )
        ) {
            ConfirmationDialog.showMessageDialog("Already up-to-date.", "Branch does not need to be merged.")
        }
        updatePendingControls(activeGraph)
    }

    doWhenButtonClicked("pendingContinueButton") {
        activeGraph.continuePending()
        updatePendingControls(activeGraph)
    }

    doWhenButtonClicked("pendingAbortButton") {
        activeGraph.abortPending()
        updatePendingControls(activeGraph)
    }

    doWhenGuardedButtonClicked("rebaseBranchButton") {
        if (!activeGraph.rebase(getSelectedOption("rebaseBranchInput"))) {
            ConfirmationDialog.showMessageDialog("Already up-to-date.", "Branch cannot be rebased.")
        }
    }

    doWhenCheckboxClicked("showLostCommitsCheckbox") { activeGraph.showLostCommits(it) }
}

fun updateBranchSelects(branches: List<AbstractBranch>, remoteBranches: List<AbstractBranch> = emptyList()) {
    val branchNames = branches
        .map { it.id }
        .filter { it != "HEAD" }

    // Like `git checkout <TAB>`: local branches, remote-only names (DWIM), then origin/<name> (detached)
    val remoteOnly = remoteBranches
        .map { (it as RemoteBranch).name }
        .filter { it !in branchNames }
    UiControl.setSelectOptions("checkoutBranchInput", branchNames + remoteOnly + remoteBranches.map { it.id })
    UiControl.setSelectOptions("deleteBranchInput", branchNames)
    UiControl.setSelectOptions("mergeBranchInput", branchNames)
    UiControl.setSelectOptions("rebaseBranchInput", branchNames)
}

fun updateTagSelects(tags: List<AbstractBranch>) {
    val tagNames = tags.map { it.id }
    UiControl.setSelectOptions("deleteTagInput", tagNames)
    UiControl.setSelectOptions("checkoutTagInput", tagNames)
}
