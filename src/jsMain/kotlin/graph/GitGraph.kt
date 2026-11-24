package graph

import components.RepoRegion
import config.GitGraphConfiguration
import kotlinx.browser.window
import konvajs.Layer
import scenario.BranchSnapshot
import scenario.CommitSnapshot
import scenario.HeadSnapshot
import scenario.RemoteBranchSnapshot
import scenario.RepoSnapshot
import scenario.TagSnapshot

class GitGraph(
    private val layer: Layer,
    private val idAllocator: CommitIdAllocator,
    repoName: String = "my-repo",
    private val config: GitGraphConfiguration = GitGraphConfiguration()
) {
    private val checkoutHandler: (Commit) -> (() -> Unit)
    private val commits = mutableListOf<Commit>()
    private val branches = mutableListOf<AbstractBranch>()
    private val tags = mutableListOf<AbstractBranch>()
    private val remoteBranches = mutableListOf<RemoteBranch>()
    private var globalCommitNumber = 0
    private var globalSwimlaneCounter = 0
    private var isRemoteRepo = false
    private lateinit var headRef: Head
    private var head: Head
        get() = headRef
        set(value) {
            headRef = value
            value.isBare = isRemoteRepo
        }
    private var showLostCommits = true

    private val regionLeft = config.leftOffset - 60
    private val regionInitialTop = config.bottomOffset - 320
    private val regionInitialWidth = 700
    private val regionInitialHeight = 480
    /** Fixed bottom anchor: the region never shrinks below its initial bottom edge. */
    private val regionBottom = regionInitialTop + regionInitialHeight

    var repoName: String = repoName
        private set

    private val repoRegion = RepoRegion(this.repoName, regionLeft, regionInitialTop, regionInitialWidth, regionInitialHeight)

    /** The horizontal canvas offset for this repo's region, used to position new repos to the right. */
    val xOffset: Int get() = config.xOffset

    val yOffset: Int get() = config.yOffset

    /** Initial height of the repo region; a clone below this repo is offset by this plus the clone gap. */
    val initialRegionHeight: Int get() = regionInitialHeight

    /** Current region height: it grows upward from the fixed bottom edge as commits climb. */
    val currentRegionHeight: Int get() {
        val minCommitY = commits.minOfOrNull { config.bottomOffset - it.linePosition * config.commitDistance }
            ?: return regionInitialHeight
        val neededTop = minCommitY - config.commitRadius - 60
        return maxOf(regionBottom - minOf(neededTop, regionInitialTop), regionInitialHeight)
    }

    /** Removes this repo's Konva layer (used when the graph is rebuilt at a new position). */
    fun destroy() {
        layer.destroy()
    }

    /** Stable identity of this repo for remote links (ADR 0005); unlike repoName it never changes on rename. */
    var repoId: Int = 0

    /** Repo id of this repo's "origin" remote, or null when it was not created by a clone. */
    var remoteRepoId: Int? = null

    /** True once this repo was wiped by clearGraph(); clones treat such a remote as not available (ADR 0005). Not persisted. */
    var wasCleared: Boolean = false

    var isActive: Boolean = false
        set(value) {
            field = value
            repoRegion.setActive(value)
            layer.batchDraw()
        }

    init {
        head = Head(Commit("", 0, 0, config = config))
        checkoutHandler = { commit -> { checkout(commit.id, showLostCommits) } }
    }

    /** Returns true if the given canvas-absolute coordinates fall within this repo's region. */
    fun containsPoint(x: Double, y: Double): Boolean = repoRegion.containsPoint(x, y)

    /** Region rectangle (left, top, width, height) in canvas coordinates; used to draw remote connection lines. */
    val regionBounds: List<Int> get() = listOf(repoRegion.left, repoRegion.top, repoRegion.width, repoRegion.height)

    /** Called after the region bounds were recalculated, so connection lines can follow region growth. */
    var onRegionBoundsChanged: (() -> Unit)? = null

    /** Updates the remote cues of this repo's region: remote styling/badge and `origin -> <name>` on a clone. */
    fun setRemoteLinks(isRemote: Boolean, originName: String?) {
        repoRegion.setRemoteLinks(isRemote, originName)
        isRemoteRepo = isRemote
        head.isBare = isRemote
        layer.batchDraw()
    }

    fun renameRepository(newName: String) {
        repoName = newName
        repoRegion.setName(newName)
        layer.batchDraw()
    }

    /**
     * Recalculates the region bounding box from actual commit positions and updates the
     * RepoRegion. The region can only grow — it never shrinks below its initial size.
     * Must be called before every layer.batchDraw() in graph-mutating operations.
     */
    private fun updateRegionBounds() {
        if (commits.isEmpty()) return

        val PADDING_RIGHT = 80
        val PADDING_TOP = 60

        // Compute maximum x (rightward growth as swimlanes increase)
        val maxCommitX = commits.maxOf { config.leftOffset + it.swimlane * config.swimlaneDistance }
        val neededRight = maxCommitX + config.commitRadius + PADDING_RIGHT
        val newWidth = maxOf(neededRight - regionLeft, regionInitialWidth)

        // Compute minimum y (upward growth as linePositions increase)
        val minCommitY = commits.minOf { config.bottomOffset - it.linePosition * config.commitDistance }
        val neededTop = minCommitY - config.commitRadius - PADDING_TOP
        val newTop = minOf(neededTop, regionInitialTop)
        val newHeight = maxOf(regionBottom - newTop, regionInitialHeight)

        repoRegion.updateBounds(newTop, newWidth, newHeight)
        onRegionBoundsChanged?.invoke()
    }

    private var initialized = false

    fun initGraph() {
        if (globalCommitNumber > 0) {
            throw IllegalStateException("Git graph may only be initialized once.")
        }

        console.log("Initialize a new Git graph for repo: $repoName")
        if (!initialized) {
            repoRegion.render(layer)
            initialized = true
        }
        val branchColor = BranchColors.nextColor()
        val initialCommit = Commit("m${idAllocator.next()}", globalCommitNumber++, 0, commitColor = branchColor, config = config)
        initialCommit.commitCircle.onDoubleClick(checkoutHandler(initialCommit))
        commits.add(initialCommit)
        head = Head(initialCommit)
        branches.add(head)

        initialCommit.render(layer)
        addBranch("main", 2, branchColor)
        head.render(layer)
        updateRegionBounds()
        layer.batchDraw()
    }

    fun addCommit(
        mergedParentCommit: Commit? = null,
        newCommitId: String? = null,
        commitIdSuffix: String = "",
        commitColor: String? = null
    ) {
        addCommit(
            mergedParentCommit,
            newCommitId,
            commitIdSuffix,
            commitColor,
            head.commit,
            head.targetBranch?.swimlane
                ?: if (head.isDetached && head.swimlane == -1) globalSwimlaneCounter++ else head.swimlane,
            globalCommitNumber++
        )
    }

    private fun addCommit(
        mergedParentCommit: Commit? = null,
        newCommitId: String? = null,
        commitIdSuffix: String = "",
        commitColor: String? = null,
        parentCommit: Commit?,
        swimlane: Int,
        linePosition: Int
    ) {
        fun moveHeadTo(commit: Commit) {
            head.commit.removeBranch(head)
            head.commit = commit
            commit.addBranch(head)
        }

        var newSwimlane = swimlane

        if (isHeadOnOccupiedSwimlane()) {
            shiftCommitsToTheRight(head.commit.swimlane + 1)
            newSwimlane++
            currentBranch().swimlane = newSwimlane
        }

        val oldHeadCommit = head.commit
        if (head.isDetached && head.swimlane == -1) {
            head.swimlane = newSwimlane
        }
        val currentBranch = currentBranch()
        val id = (newCommitId ?: calcCommitId(currentBranch)) + commitIdSuffix
        val newCommitColor =
            commitColor ?: if (head.isDetached) head.commitColor else head.targetBranch?.commitColor ?: ""
        val commit = Commit(
            id, linePosition, newSwimlane, parentCommit,
            newCommitColor, config
        )
        parentCommit?.childCommit = commit
        mergedParentCommit?.childCommit = commit
        commit.mergedCommit = mergedParentCommit
        commit.render(layer)
        commit.commitCircle.onDoubleClick(checkoutHandler(commit))
        commits.add(commit)
        if (head.isDetached) {
            moveHeadTo(commit)
            head.attachToCommit(head.commit, layer)
        } else {
            moveBranch(currentBranch, head.commit, commit)
            head.attachToBranch(currentBranch, layer)
            currentBranch.attachToCommit(head.commit, layer)
        }

        oldHeadCommit.repositionBranches(layer)
        calculateLostCommits()
        updateRegionBounds()
        layer.batchDraw()
    }

    private fun isHeadOnOccupiedSwimlane(): Boolean {
        val currentSwimlane = head.targetBranch?.swimlane ?: head.swimlane
        val currentLinePosition = head.commit.linePosition
        commits.filter { it.swimlane == currentSwimlane && it.linePosition > currentLinePosition }
            .ifEmpty {
                branches
                    .filterNot { it == head.targetBranch || it == head }
                    .filter {
                        it.swimlane == currentSwimlane && head.commit != it.commit
                    }
                    .ifEmpty {
                        return false
                    }
            }

        return true
    }

    fun amendCommit() {
        val amendedCommitSwimlane = head.commit.swimlane + 1
        shiftCommitsToTheRight(amendedCommitSwimlane)
        addCommit(
            newCommitId = "${head.commit.id}*",
            commitColor = head.commit.commitColor,
            parentCommit = head.commit.parent,
            swimlane = amendedCommitSwimlane,
            linePosition = head.commit.linePosition
        )
        calculateLostCommits()
        layer.batchDraw()
    }

    private fun shiftCommitsToTheRight(swimlaneStart: Int) {
        commits.forEach {
            if (it.swimlane >= swimlaneStart) {
                it.shiftRight(layer)
            }
            if (globalSwimlaneCounter <= it.swimlane) {
                globalSwimlaneCounter = it.swimlane + 1
            }
        }
        branches.forEach {
            if (it.swimlane >= swimlaneStart) {
                it.shiftToNextSwimlane()
            }
        }
        commits.forEach {
            if (it.isLostInReflog) {
                it.rerender(layer)
            }
        }
    }

    fun doesCommitExist(id: String) = commits.any { it.id == id }
    fun findCommitFor(id: String) = commits.find { it.id == id }

    /** Upstream state of the current branch, as shown in the Remote tab. */
    class UpstreamStatus(
        val detached: Boolean,
        val branchName: String?,
        val upstream: String?,
        val ahead: Int,
        val behind: Int
    )

    /** Describes the current branch's upstream and its ahead/behind counts (commit-id set difference). */
    fun upstreamStatus(): UpstreamStatus {
        if (head.isDetached) return UpstreamStatus(true, null, null, 0, 0)
        val branch = head.targetBranch as? Branch ?: return UpstreamStatus(false, null, null, 0, 0)
        val remote = branch.upstream?.let { up -> remoteBranches.find { it.id == up } }
            ?: return UpstreamStatus(false, branch.id, null, 0, 0)
        val local = ancestorIds(branch.commit)
        val upstreamIds = ancestorIds(remote.commit)
        return UpstreamStatus(false, branch.id, remote.id, (local - upstreamIds).size, (upstreamIds - local).size)
    }

    /** IDs of [start] and everything reachable through parent and merged-parent links. */
    fun ancestorIds(start: Commit): Set<String> {
        val ids = mutableSetOf<String>()
        val stack = mutableListOf(start)
        while (stack.isNotEmpty()) {
            val c = stack.removeAt(stack.size - 1)
            if (!ids.add(c.id)) continue
            c.parent?.let { stack.add(it) }
            c.mergedCommit?.let { stack.add(it) }
        }
        return ids
    }

    /** What a receiver needs to recreate a commit: identity, colour and parent links (ADR 0004/0005). */
    class CommitDescription(val id: String, val commitColor: String, val parentId: String?, val mergedParentId: String?)

    /**
     * Describes the commits reachable from [tip] that the receiver lacks ([receiverHas] is false), parents first.
     * Traversal stops at commits the receiver already has.
     */
    fun describeMissingCommits(tip: Commit, receiverHas: (String) -> Boolean): List<CommitDescription> {
        val result = mutableListOf<CommitDescription>()
        val seen = mutableSetOf<String>()
        // iterative post-order DFS: (commit, parentsExpanded)
        val stack = mutableListOf(tip to false)
        while (stack.isNotEmpty()) {
            val (c, expanded) = stack.removeAt(stack.size - 1)
            if (expanded) {
                result.add(CommitDescription(c.id, c.commitColor, c.parent?.id, c.mergedCommit?.id))
                continue
            }
            if (receiverHas(c.id) || !seen.add(c.id)) continue
            stack.add(c to true)
            c.mergedCommit?.let { stack.add(it to false) }
            c.parent?.let { stack.add(it to false) }
        }
        return result
    }

    /**
     * Generic commit-receiving primitive for push and fetch (ADR 0005 decision 5). Creates the commits of
     * [descriptions] (parents first) that do not exist here yet, keeping ID, colour and parents; the layout is
     * decided here: new linePositions and one lane. With [preferredSwimlane] the lane of the receiving ref is used,
     * shifting to the right when it holds commits that are not ancestors of the new ones; null allocates a new lane.
     * Moves no refs. Returns the created commits.
     */
    fun receiveCommits(descriptions: List<CommitDescription>, preferredSwimlane: Int?): List<Commit> {
        val missing = descriptions.filter { !doesCommitExist(it.id) }
        if (missing.isEmpty()) return emptyList()
        var lane = preferredSwimlane
        if (lane == null) {
            lane = globalSwimlaneCounter++
        } else {
            val base = missing.first().parentId?.let { findCommitFor(it) }
            val occupied = commits.any { it.swimlane == lane && it != base && (base == null || !it.isAncestorOf(base)) }
            if (occupied) {
                shiftCommitsToTheRight(lane + 1)
                lane++
            }
        }
        if (globalSwimlaneCounter <= lane) globalSwimlaneCounter = lane + 1
        val created = mutableListOf<Commit>()
        for (d in missing) {
            val parent = d.parentId?.let { findCommitFor(it) }
            val merged = d.mergedParentId?.let { findCommitFor(it) }
            val commit = Commit(d.id, globalCommitNumber++, lane, parent, d.commitColor, config)
            parent?.childCommit = commit
            merged?.childCommit = commit
            commit.mergedCommit = merged
            commit.render(layer)
            commit.commitCircle.onDoubleClick(checkoutHandler(commit))
            commits.add(commit)
            created.add(commit)
        }
        calculateLostCommits()
        updateRegionBounds()
        layer.batchDraw()
        return created
    }

    fun branchTip(name: String): Commit? = (findBranch(name) as? Branch)?.commit

    fun branchUpstream(name: String): String? = (findBranch(name) as? Branch)?.upstream

    fun setBranchUpstream(name: String, upstream: String?) {
        (findBranch(name) as? Branch)?.upstream = upstream
    }

    /** Moves an existing local branch (and HEAD, if it is checked out) onto [commit]; a plain ref move. */
    fun moveBranchTo(name: String, commit: Commit) {
        val branch = findBranch(name) ?: return
        val old = branch.commit
        old.removeBranch(branch)
        if (branch.isCheckedOut()) old.removeBranch(head)
        branch.swimlane = commit.swimlane
        branch.commit = commit
        branch.attachToCommit(commit, layer)
        old.repositionBranches(layer)
        calculateLostCommits()
        layer.batchDraw()
    }

    /** Creates a local branch label at [commit] without checking it out (used by push into this repo). */
    fun createBranchAt(name: String, commit: Commit, color: String) {
        val branch = Branch(name, commit.swimlane, commit = commit, commitColor = color)
        branches.add(branch)
        branches.sortBy { it.id }
        branch.onDoubleClick { checkout(name, showLostCommits) }
        branch.render(layer)
        branch.attachToCommit(commit, layer)
        calculateLostCommits()
        layer.batchDraw()
    }

    fun branchColor(name: String): String = findBranch(name)?.commitColor ?: ""

    /** Flags this repo's region red for ~1.5 s (rejected push). */
    fun flashAlert() {
        repoRegion.setAlert()
        layer.batchDraw()
        window.setTimeout({
            repoRegion.setActive(isActive)
            layer.batchDraw()
        }, 1500)
    }

    /**
     * Calculates the current branch based on the HEAD pointer. If the HEAD is detached, return the HEAD as a result.
     * Otherwise return the currently checked out branch.
     */
    private fun currentBranch(): AbstractBranch {
        return if (head.isDetached) {
            head
        } else {
            head.targetBranch!!
        }
    }

    private fun moveBranch(branch: AbstractBranch, from: Commit, to: Commit) {
        from.removeBranch(branch)
        to.addBranch(branch)
        branch.commit = to
        branch.getLabel().attachToCommit(to, layer)
        if (!head.isDetached) {
            head.attachToBranch(branch, layer)
            head.commit = to
        }
    }

    fun addBranch(id: String): Branch {
        return addBranch(id, 1, BranchColors.nextColor())
    }

    private fun addBranch(id: String, counter: Int, commitColor: String): Branch {
        console.log("Adding branch $id")
        val branch =
            Branch(id, globalSwimlaneCounter++, commit = head.commit, counter = counter, commitColor = commitColor)
        branches.add(branch)
        branches.sortBy { it.id }
        head.commit.removeBranch(head)
        branch.onDoubleClick { checkout(id, showLostCommits) }
        branch.attachToCommit(head.commit, layer)
        branch.render(layer)
        checkout(id, showLostCommits)
        return branch
    }

    fun getBranches(): List<AbstractBranch> = branches

    fun addTag(id: String) {
        val tag = Tag(id, head.commit)

        tag.render(layer)
        tag.onDoubleClick {
            checkout(tag.commit.id, showLostCommits)
        }
        tags.add(tag)
        tags.sortBy { it.id }
    }

    fun getTags(): List<AbstractBranch> = tags

    fun getRemoteBranches(): List<RemoteBranch> = remoteBranches

    /** Creates or moves the remote-tracking label `origin/<name>` onto [commit]. */
    fun setRemoteBranch(name: String, commit: Commit) {
        val existing = remoteBranches.find { it.name == name }
        if (existing != null) {
            existing.commit.removeBranch(existing)
            val oldCommit = existing.commit
            existing.commit = commit
            existing.attachToCommit(commit, layer)
            oldCommit.repositionBranches(layer)
        } else {
            val remote = RemoteBranch(name, commit.swimlane, commit, commit.commitColor)
            remoteBranches.add(remote)
            remoteBranches.sortBy { it.name }
            remote.render(layer)
            remote.attachToCommit(commit, layer)
        }
        calculateLostCommits()
        layer.batchDraw()
    }

    /** Removes the remote-tracking label `origin/<name>`; commits stay. */
    fun removeRemoteBranch(name: String) {
        val existing = remoteBranches.find { it.name == name } ?: return
        existing.commit.removeBranch(existing)
        existing.removeFrom(layer)
        remoteBranches.remove(existing)
        existing.commit.repositionBranches(layer)
        calculateLostCommits()
        layer.batchDraw()
    }

    fun checkout(id: String, doShowLostCommits: Boolean) {
        console.log("Checking out $id")
        val oldHeadCommit = head.commit

        val targetBranch = findBranch(id)
        if (targetBranch != null) {
            // checking out a branch
            head.targetBranch?.headRemoved()
            head.targetBranch = targetBranch
            targetBranch.checkedOut(head)
            head.commit.removeBranch(head)
            head.attachToBranch(targetBranch, layer)
        } else {
            // try to check out a tag or commit directly
            val targetCommit = findTag(id)?.commit ?: commits.find { it.id == id }
            if (targetCommit != null) {
                // a commit id was given to check out: create a detached HEAD
                if (!head.isDetached) {
                    head.targetBranch?.headRemoved()
                    head.targetBranch = null
                }
                head.swimlane = -1 // calculate new swimlane as soon as a new commit is added
                moveBranch(head, head.commit, targetCommit)
                head.attachToCommit(targetCommit, layer)
            }
        }
        oldHeadCommit.repositionBranches(layer)
        calculateLostCommits()
        showLostCommits(doShowLostCommits)
        layer.batchDraw()
    }

    fun merge(noFF: Boolean, targetBranchId: String): Boolean {
        // Source may also be a tracking ref (origin/<name>), used by pull; those are not listed in dropdowns.
        val targetBranch = findBranch(targetBranchId) ?: remoteBranches.find { it.id == targetBranchId }
            ?: throw IllegalStateException("target branch is null")
        if (targetBranch == currentBranch() || targetBranch.commit.isAncestorOf(currentBranch().commit)) {
            return false
        }
        return if (noFF || !currentBranch().commit.isAncestorOf(targetBranch.commit)) {
            val mergeCommitId = "${currentBranch().commit.id}${targetBranch.commit.id}"
            addCommit(targetBranch.commit, mergeCommitId)
            true
        } else {
            // fast-forward current branch to target branch's commit
            moveBranch(currentBranch(), currentBranch().commit, targetBranch.commit)
            true
        }
    }

    fun rebase(targetBranchName: String): Boolean {
        // Target may also be a tracking ref (origin/<name>), used by pull --rebase; not listed in dropdowns.
        val targetBranch: AbstractBranch? = findBranch(targetBranchName) ?: remoteBranches.find { it.id == targetBranchName }
        return if (targetBranch != null) {
            val commonBaseCommit = calculateCommonBaseCommit(targetBranch)
            if (commonBaseCommit == targetBranch.commit) {
                return false
            }
            if (commonBaseCommit == currentBranch().commit) {
                console.log("Rebase ${currentBranch().id} onto ${targetBranch.id}: fast-forward ${currentBranch().id}")
                moveBranch(currentBranch(), currentBranch().commit, targetBranch.commit)
                return true
            }
            console.log("Rebase ${currentBranch().id} onto ${targetBranch.id}")
            val newSwimlaneForRebasedBranch = targetBranch.swimlane
            shiftCommitsToTheRight(targetBranch.swimlane)
            currentBranch().swimlane = newSwimlaneForRebasedBranch

            val commitsToBeRebased = ArrayList<Commit>()
            var commit: Commit? = currentBranch().commit
            do {
                if (commit != null) {
                    commitsToBeRebased.add(commit)
                    commit = commit.parent
                }
            } while (commit != commonBaseCommit)

            moveBranch(currentBranch(), currentBranch().commit, targetBranch.commit)
            commitsToBeRebased.reverse()
            commitsToBeRebased.forEach {
                addCommit(
                    newCommitId = "${it.id}*",
                    commitColor = it.commitColor,
                    parentCommit = head.commit,
                    swimlane = newSwimlaneForRebasedBranch,
                    linePosition = globalCommitNumber++
                )
            }
            commitsToBeRebased.first().rerender(layer)
            calculateLostCommits()
            layer.batchDraw()
            true
        } else {
            false
        }
    }

    private fun calculateCommonBaseCommit(targetBranch: AbstractBranch): Commit? {
        val targetBranchHistory = HashSet<Commit>()
        fun addParentToBranchHistory(commit: Commit) {
            if (commit.parent != null) {
                targetBranchHistory.add(commit.parent)
                addParentToBranchHistory(commit.parent)
            }
        }
        targetBranchHistory.add(targetBranch.commit)
        addParentToBranchHistory(targetBranch.commit)

        fun findCommonBaseCommitInCurrentBranchHistory(startCommit: Commit): Commit? {
            if (targetBranchHistory.contains(startCommit)) {
                return startCommit
            } else if (startCommit.parent != null) {
                return findCommonBaseCommitInCurrentBranchHistory(startCommit.parent)
            } else {
                return null
            }
        }

        return findCommonBaseCommitInCurrentBranchHistory(currentBranch().commit)
    }

    private fun calculateLostCommits() {
        fun traverseHistory(commit: Commit?, callback: (Commit) -> Unit) {
            if (commit == null) {
                return
            }
            callback(commit)
            if (commit.parent != null) {
                traverseHistory(commit.parent, callback)
            }
            if (commit.mergedCommit != null) {
                traverseHistory(commit.mergedCommit, callback)
            }
        }

        commits.forEach {
            it.commitCircle.isLostInReflog = true
        }
        val resetLostInReflog: (Commit) -> Unit = { it.isLostInReflog = false }

        branches.forEach {
            traverseHistory(it.commit, resetLostInReflog)
        }
        tags.forEach {
            traverseHistory(it.commit, resetLostInReflog)
        }
        remoteBranches.forEach {
            traverseHistory(it.commit, resetLostInReflog)
        }
        traverseHistory(head.commit, resetLostInReflog)
    }

    private fun calcCommitId(branch: AbstractBranch) = "${branch.id.substringAfter('/').first()}${idAllocator.next()}"
    override fun toString(): String = commits.reversed().joinToString("\n")

    fun runGarbageCollection() {
        commits.forEach {
            if (it.commitCircle.isLostInReflog) {
                if (it.parent?.childCommit == it) {
                    it.parent.childCommit = null
                }
                it.removeFrom(layer)
                console.log("Remove lost commit $it")
            }
        }
        commits.removeAll { it.commitCircle.isLostInReflog }
        realignCommits()
        layer.batchDraw()
    }

    private fun realignCommits() {
        commits.forEach {
            if (it.parent?.swimlane == it.childCommit?.swimlane) {
                it.swimlane = it.parent?.swimlane ?: it.childCommit?.swimlane ?: 0
                it.rerender(layer)
            }
        }
        currentBranch().attachToCommit(currentBranch().commit, layer)
    }

    fun doesTagExist(tagName: String) = tags.map { it.id }.contains(tagName)
    fun deleteTag(tagName: String) = deleteRef(tagName, tags)
    fun deleteBranch(branchName: String) = deleteRef(branchName, branches)

    fun isBranchCheckedOut(branchName: String): Boolean = findBranch(branchName)?.isCheckedOut() ?: false
    fun isBranchNameValid(branchName: String): Boolean {
        return branchName.first() != 'H' &&
                branches
                    .map { it.id.substringAfter('/') }
                    .none { it.startsWith(branchName.substringAfter('/').first()) }
    }

    private fun deleteRef(refName: String, refList: MutableList<AbstractBranch>) {
        val targetRef = refList.find { it.id == refName }
        if (targetRef != null) {
            refList.remove(targetRef)
            refList.sortBy { it.id }
            targetRef.commit.removeBranch(targetRef)
            targetRef.commit.repositionBranches(layer)
            targetRef.removeFrom(layer)
        }
        calculateLostCommits()
    }

    private fun findBranch(branchName: String): AbstractBranch? = branches.find { it.id == branchName }

    private fun findTag(tagName: String): AbstractBranch? = tags.find { it.id == tagName }

    /** Whether lost commits are currently shown; carried over when the graph is rebuilt at a new position. */
    val lostCommitsShown: Boolean get() = showLostCommits

    fun showLostCommits(doShowLostCommits: Boolean) {
        showLostCommits = doShowLostCommits
        commits.forEach {
            if (it.isLostInReflog) {
                it.show(doShowLostCommits, layer)
            }
        }
        layer.batchDraw()
    }

    fun resetBranch(targetCommitId: String) {
        val targetCommit = findCommitFor(targetCommitId)
        if (targetCommit != null) {
            moveBranch(currentBranch(), currentBranch().commit, targetCommit)
            calculateLostCommits()
            layer.batchDraw()
        }
    }

    /**
     * Clears all commits, branches, and tags from this repo and re-initializes
     * to the initial state. The region border is preserved.
     */
    fun clearGraph() {
        wasCleared = true
        commits.forEach { it.removeFrom(layer) }
        commits.clear()
        branches.forEach { it.removeFrom(layer) }
        branches.clear()
        tags.forEach { it.removeFrom(layer) }
        tags.clear()
        remoteBranches.forEach { it.removeFrom(layer) }
        remoteBranches.clear()
        globalCommitNumber = 0
        globalSwimlaneCounter = 0
        head = Head(Commit("", 0, 0, config = config))
        initGraph()
        layer.batchDraw()
    }

    /**
     * Builds the snapshot a clone of this repo starts from (ADR 0005): all non-lost commits 1:1,
     * tags, an origin/<name> label per local branch and one local branch (the current one, or
     * main/first branch when HEAD is detached) checked out and tracking its origin counterpart.
     */
    fun buildCloneSnapshot(cloneName: String, cloneXOffset: Int): RepoSnapshot {
        val source = getSnapshot()
        val keptCommits = source.commits.filter { !it.isLostInReflog }
        val keptIds = keptCommits.map { it.id }.toSet()
        val localBranches = source.branches.filter { it.targetCommitId in keptIds }
        val currentId = source.head.targetBranchId
        val localBranch = localBranches.firstOrNull { it.id == currentId }
            ?: localBranches.firstOrNull { it.id == "main" }
            ?: localBranches.firstOrNull()
        return RepoSnapshot(
            repoName = cloneName,
            xOffset = cloneXOffset,
            yOffset = source.yOffset,
            globalCommitNumber = source.globalCommitNumber,
            globalSwimlaneCounter = source.globalSwimlaneCounter,
            commits = keptCommits,
            branches = listOfNotNull(localBranch?.copy(upstream = "origin/${localBranch.id}")),
            tags = source.tags.filter { it.targetCommitId in keptIds },
            head = HeadSnapshot(targetBranchId = localBranch?.id, detachedCommitId = null),
            remoteBranches = localBranches.map { RemoteBranchSnapshot(it.id, it.targetCommitId) }
        )
    }

    /** Returns a snapshot of the current state for URL encoding. */
    fun getSnapshot(): RepoSnapshot {
        val commitSnapshots = commits.map { commit ->
            CommitSnapshot(
                id = commit.id,
                linePosition = commit.linePosition,
                swimlane = commit.swimlane,
                parentId = commit.parent?.id,
                mergedParentId = commit.mergedCommit?.id,
                commitColor = commit.commitColor,
                isLostInReflog = commit.isLostInReflog
            )
        }
        val branchSnapshots = branches
            .filter { it.id != "HEAD" }
            .map { branch ->
                BranchSnapshot(
                    id = branch.id,
                    swimlane = branch.swimlane,
                    targetCommitId = branch.commit.id,
                    commitColor = branch.commitColor,
                    upstream = (branch as? Branch)?.upstream
                )
            }
        val tagSnapshots = tags.map { tag ->
            TagSnapshot(id = tag.id, targetCommitId = tag.commit.id)
        }
        val remoteBranchSnapshots = remoteBranches.map { RemoteBranchSnapshot(it.name, it.commit.id) }
        val headSnapshot = HeadSnapshot(
            targetBranchId = if (head.isDetached) null else head.targetBranch?.id,
            detachedCommitId = if (head.isDetached) head.commit.id else null
        )
        return RepoSnapshot(
            repoName = repoName,
            xOffset = config.xOffset,
            yOffset = config.yOffset,
            globalCommitNumber = globalCommitNumber,
            globalSwimlaneCounter = globalSwimlaneCounter,
            commits = commitSnapshots,
            branches = branchSnapshots,
            tags = tagSnapshots,
            head = headSnapshot,
            remoteBranches = remoteBranchSnapshots
        )
    }

    /**
     * Restores the graph state from a snapshot. Must be called instead of initGraph().
     * Directly populates commits, branches, tags and head, then re-renders everything.
     */
    fun restoreFromSnapshot(snapshot: RepoSnapshot) {
        // Render the repo region border (same as initGraph() does)
        if (!initialized) {
            repoRegion.render(layer)
            initialized = true
        }

        // Restore counters (the shared commit ID allocator is re-seeded by the caller)
        globalCommitNumber = snapshot.globalCommitNumber
        globalSwimlaneCounter = snapshot.globalSwimlaneCounter

        // Rebuild commit objects in order (snapshot stores parents before children)
        val commitMap = mutableMapOf<String, Commit>()
        for (cs in snapshot.commits) {
            val parent = if (cs.parentId != null) commitMap[cs.parentId] else null
            val commit = Commit(
                id = cs.id,
                linePosition = cs.linePosition,
                swimlane = cs.swimlane,
                parent = parent,
                commitColor = cs.commitColor,
                config = config
            )
            commit.isLostInReflog = cs.isLostInReflog
            // Wire childCommit on parent
            if (parent != null && parent.childCommit == null) {
                parent.childCommit = commit
            }
            commitMap[cs.id] = commit
            commits.add(commit)
        }
        // Second pass: wire mergedCommit references and parent lines
        for (cs in snapshot.commits) {
            if (cs.mergedParentId != null) {
                val commit = commitMap[cs.id]
                val mergedParent = commitMap[cs.mergedParentId]
                if (commit != null && mergedParent != null) {
                    commit.mergedCommit = mergedParent
                    if (mergedParent.childCommit == null) {
                        mergedParent.childCommit = commit
                    }
                }
            }
        }

        // Render commits (draws circles and parent lines)
        commits.forEach { commit ->
            commit.render(layer)
            commit.commitCircle.onDoubleClick(checkoutHandler(commit))
        }

        // Restore branches (non-HEAD)
        for (bs in snapshot.branches) {
            val targetCommit = commitMap[bs.targetCommitId] ?: continue
            val branch = Branch(
                id = bs.id,
                swimlane = bs.swimlane,
                commit = targetCommit,
                commitColor = bs.commitColor
            )
            branch.upstream = bs.upstream
            branch.onDoubleClick { checkout(bs.id, showLostCommits) }
            branches.add(branch)
            targetCommit.addBranch(branch)
            branch.render(layer)
            branch.getLabel().attachToCommit(targetCommit, layer)
        }
        branches.sortBy { it.id }

        // Restore tags
        for (ts in snapshot.tags) {
            val targetCommit = commitMap[ts.targetCommitId] ?: continue
            val tag = Tag(ts.id, targetCommit)
            tag.render(layer)
            tag.onDoubleClick { checkout(ts.id, showLostCommits) }
            tags.add(tag)
        }
        tags.sortBy { it.id }

        // Restore remote-tracking labels
        for (rs in snapshot.remoteBranches) {
            val targetCommit = commitMap[rs.targetCommitId] ?: continue
            val remote = RemoteBranch(rs.name, targetCommit.swimlane, targetCommit, targetCommit.commitColor)
            remoteBranches.add(remote)
            remote.render(layer)
            remote.attachToCommit(targetCommit, layer)
        }
        remoteBranches.sortBy { it.name }

        // Restore HEAD
        val headSnapshot = snapshot.head
        if (headSnapshot.targetBranchId != null) {
            // Attached HEAD
            val targetBranch = branches.find { it.id == headSnapshot.targetBranchId }
            if (targetBranch != null) {
                head = Head(targetBranch.commit, targetBranch)
                targetBranch.checkedOut(head)
                head.attachToBranch(targetBranch, layer)
            } else {
                // Fallback: detach to the first commit
                val fallbackCommit = commits.firstOrNull() ?: Commit("", 0, 0, config = config)
                head = Head(fallbackCommit)
                head.attachToCommit(fallbackCommit, layer)
            }
        } else if (headSnapshot.detachedCommitId != null) {
            // Detached HEAD
            val targetCommit = commitMap[headSnapshot.detachedCommitId]
            if (targetCommit != null) {
                head = Head(targetCommit)
                head.attachToCommit(targetCommit, layer)
            }
        }
        head.render(layer)
        branches.add(head)

        updateRegionBounds()
        layer.batchDraw()
    }
}
