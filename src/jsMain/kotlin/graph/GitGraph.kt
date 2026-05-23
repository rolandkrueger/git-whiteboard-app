package graph

import components.RepoRegion
import config.GitGraphConfiguration
import konvajs.Layer
import scenario.BranchSnapshot
import scenario.CommitSnapshot
import scenario.HeadSnapshot
import scenario.RepoSnapshot
import scenario.TagSnapshot

class GitGraph(
    private val layer: Layer,
    val repoName: String = "origin",
    private val config: GitGraphConfiguration = GitGraphConfiguration()
) {
    private val checkoutHandler: (Commit) -> (() -> Unit)
    private val commits = mutableListOf<Commit>()
    private val branches = mutableListOf<AbstractBranch>()
    private val tags = mutableListOf<AbstractBranch>()
    private var globalCommitNumber = 0
    private var globalSwimlaneCounter = 0
    private var head: Head
    private var showLostCommits = true
    private val repoRegion = RepoRegion(repoName, config.leftOffset - 60, config.bottomOffset - 320)

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
        val initialCommit = Commit("m1", globalCommitNumber++, 0, commitColor = branchColor, config = config)
        initialCommit.commitCircle.onDoubleClick(checkoutHandler(initialCommit))
        commits.add(initialCommit)
        head = Head(initialCommit)
        branches.add(head)

        initialCommit.render(layer)
        addBranch("master", 2, branchColor)
        head.render(layer)
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
        val targetBranch = findBranch(targetBranchId) ?: throw IllegalStateException("target branch is null")
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
        val targetBranch = findBranch(targetBranchName)
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
        traverseHistory(head.commit, resetLostInReflog)
    }

    private fun calcCommitId(branch: AbstractBranch) = "${branch.id.substringAfter('/').first()}${branch.counter++}"
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
        commits.forEach { it.removeFrom(layer) }
        commits.clear()
        branches.forEach { it.removeFrom(layer) }
        branches.clear()
        tags.forEach { it.removeFrom(layer) }
        tags.clear()
        globalCommitNumber = 0
        globalSwimlaneCounter = 0
        head = Head(Commit("", 0, 0, config = config))
        initGraph()
        layer.batchDraw()
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
                    commitColor = branch.commitColor
                )
            }
        val tagSnapshots = tags.map { tag ->
            TagSnapshot(id = tag.id, targetCommitId = tag.commit.id)
        }
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
            head = headSnapshot
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

        // Restore counters
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

        // Restore HEAD
        val headSnapshot = snapshot.head
        if (headSnapshot.targetBranchId != null) {
            // Attached HEAD
            val targetBranch = branches.find { it.id == headSnapshot.targetBranchId }
            if (targetBranch != null) {
                head = Head(targetBranch.commit, targetBranch)
                targetBranch.checkedOut(head)
                targetBranch.commit.addBranch(head)
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
                targetCommit.addBranch(head)
                head.attachToCommit(targetCommit, layer)
            }
        }
        head.render(layer)
        branches.add(head)

        layer.batchDraw()
    }
}
