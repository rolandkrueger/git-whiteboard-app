package graph

import fabricjs.FabricCanvas

class GitGraph(private val canvas: FabricCanvas) {
    private val checkoutHandler: (Commit) -> (() -> Unit)
    private val commits = mutableListOf<Commit>()
    private val branches = mutableListOf<AbstractBranch>()
    private val tags = mutableListOf<AbstractBranch>()
    private var globalCommitNumber = 0
    private var globalSwimlaneCounter = 0
    private var head: Head
    private var showLostCommits = true

    init {
        head = Head(Commit("", 0, 0))
        checkoutHandler = { commit -> { checkout(commit.id, showLostCommits) } }
    }

    fun initGraph() {
        if (globalCommitNumber > 0) {
            throw IllegalStateException("Git graph may only be initialized once.")
        }

        console.log("Initialize a new Git graph")
        val branchColor = BranchColors.nextColor()
        val initialCommit = Commit("m1", globalCommitNumber++, 0, commitColor = branchColor)
        initialCommit.commitCircle.onDoubleClick(checkoutHandler(initialCommit))
        commits.add(initialCommit)
        head = Head(initialCommit)
        branches.add(head)

        initialCommit.render(canvas)
        addBranch("master", 2, branchColor)
        head.render(canvas)
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

        val oldHeadCommit = head.commit
        if (head.isDetached && head.swimlane == -1) {
            head.swimlane = swimlane
        }
        val currentBranch = currentBranch()
        val id = (newCommitId ?: calcCommitId(currentBranch)) + commitIdSuffix
        val newCommitColor =
            commitColor ?: if (head.isDetached) head.commitColor else head.targetBranch?.commitColor ?: ""
        val commit = Commit(
            id, linePosition, swimlane, parentCommit,
            newCommitColor
        )
        parentCommit?.childCommit = commit
        mergedParentCommit?.childCommit = commit
        commit.mergedCommit = mergedParentCommit
        commit.render(canvas)
        commit.commitCircle.onDoubleClick(checkoutHandler(commit))
        commits.add(commit)
        if (head.isDetached) {
            moveHeadTo(commit)
        } else {
            moveBranch(currentBranch, head.commit, commit)
        }

        if (head.isDetached) {
            head.attachToCommit(head.commit, canvas)
        } else {
            head.attachToBranch(currentBranch, canvas)
            currentBranch.attachToCommit(head.commit, canvas)
        }

        oldHeadCommit.repositionBranches(canvas)
        canvas.renderAll()
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
        canvas.renderAll()
    }

    private fun shiftCommitsToTheRight(swimlaneStart: Int) {
        commits.forEach {
            if (it.swimlane >= swimlaneStart) {
                it.shiftRight(canvas)
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
                it.rerender(canvas)
            }
        }
    }

    fun doesCommitExist(id: String) = commits.any { it.id == id }
    fun getCommitFor(id: String) = commits.find { it.id == id }

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
        branch.getLabel().attachToCommit(to, canvas)
        if (!head.isDetached) {
            head.attachToBranch(branch, canvas)
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
        branch.render(canvas)
        branch.attachToCommit(head.commit, canvas)
        branch.render(canvas)
        checkout(id, showLostCommits)
        return branch
    }

    fun getBranches(): List<AbstractBranch> = branches

    fun addTag(id: String) {
        val tag = Tag(id, head.commit)

        tag.render(canvas)
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
            head.attachToBranch(targetBranch, canvas)
        } else {
            // try to check out a commit directly
            val targetCommit = commits.find { it.id == id }
            if (targetCommit != null) {
                // a commit id was given to check out: create a detached HEAD
                if (!head.isDetached) {
                    head.targetBranch?.headRemoved()
                    head.targetBranch = null
                }
                head.swimlane = -1 // calculate new swimlane as soon as a new commit is added
                moveBranch(head, head.commit, targetCommit)
                head.attachToCommit(targetCommit, canvas)
            }
        }
        oldHeadCommit.repositionBranches(canvas)
        calculateLostCommits()
        showLostCommits(doShowLostCommits)
        canvas.renderAll()
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
            commitsToBeRebased.first().rerender(canvas)
            calculateLostCommits()
            canvas.renderAll()
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
                it.removeFrom(canvas)
                console.log("Remove lost commit $it")
            }
        }
        commits.removeAll { it.commitCircle.isLostInReflog }
        realignCommits()
        canvas.renderAll()
    }

    private fun realignCommits() {
        commits.forEach {
            if (it.parent?.swimlane == it.childCommit?.swimlane) {
                it.swimlane = it.parent?.swimlane ?: it.childCommit?.swimlane ?: 0
                it.rerender(canvas)
            }
        }
        currentBranch().attachToCommit(currentBranch().commit, canvas)
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
            targetRef.commit.repositionBranches(canvas)
            targetRef.removeFrom(canvas)
        }
        calculateLostCommits()
    }

    private fun findBranch(branchName: String): AbstractBranch? = branches.find { it.id == branchName }

    fun showLostCommits(doShowLostCommits: Boolean) {
        showLostCommits = doShowLostCommits
        commits.forEach {
            if (it.isLostInReflog) {
                it.show(doShowLostCommits, canvas)
            }
        }
        canvas.renderAll()
    }
}