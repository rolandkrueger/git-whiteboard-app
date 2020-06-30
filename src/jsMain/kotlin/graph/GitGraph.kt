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

    init {
        head = Head(Commit("", 0, 0))
        checkoutHandler = { commit -> { checkout(commit.id) } }
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

    fun addCommit(mergedParentCommit: Commit? = null, newCommitId: String? = null) {
        fun moveHeadTo(commit: Commit) {
            head.commit.removeBranch(head)
            head.commit = commit
            commit.addBranch(head)
        }

        val oldHeadCommit = head.commit
        if (head.isDetached && head.swimlane == -1) {
            head.swimlane = globalSwimlaneCounter++
        }
        val currentBranch = currentBranch()
        val swimlane = head.targetBranch?.swimlane ?: head.swimlane
        val id = newCommitId ?: calcCommitId(currentBranch)
        val commit = Commit(id, globalCommitNumber++, swimlane, head.commit,
            if (head.isDetached) head.commitColor else head.targetBranch?.commitColor ?: ""
        )
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
        val branch = Branch(id, globalSwimlaneCounter++, commit = head.commit, counter = counter, commitColor = commitColor)
        branches.add(branch)
        head.commit.removeBranch(head)
        branch.onDoubleClick { checkout(id) }
        branch.render(canvas)
        branch.attachToCommit(head.commit, canvas)
        branch.render(canvas)
        checkout(id)
        return branch
    }

    fun addTag(id: String) {
        val tag = Tag(id, head.commit)

        tag.render(canvas)
        tag.onDoubleClick {
            checkout(tag.commit.id)
        }
        tags.add(tag)
    }

    fun checkout(id: String) {
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
        canvas.renderAll()
    }

    fun merge(targetBranchId: String) {
        val targetBranch = findBranch(targetBranchId)
        if (targetBranch != null && targetBranch != currentBranch()) {
            val mergeCommitId = "${currentBranch().commit.id}${targetBranch.commit.id}"
            addCommit(targetBranch.commit, mergeCommitId)
        }
    }

    private fun calculateLostCommits() {
        fun traverseHistory(commit: Commit, callback: (Commit) -> Unit) {
            callback(commit)
            if (commit.parent != null) {
                traverseHistory(commit.parent, callback)
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

    private fun calcCommitId(branch: AbstractBranch) = "${branch.id.first()}${branch.counter++}"
    override fun toString(): String = commits.reversed().joinToString("\n")

    fun runGarbageCollection() {
        commits.forEach {
            if (it.commitCircle.isLostInReflog) {
                it.removeFrom(canvas)
            }
        }
        commits.removeAll { it.commitCircle.isLostInReflog }
        canvas.renderAll()
    }

    fun deleteTag(tagName: String) = deleteRef(tagName, tags)
    fun deleteBranch(branchName: String) = deleteRef(branchName, branches)

    fun isBranchCheckedOut(branchName: String): Boolean = findBranch(branchName)?.isCheckedOut() ?: false

    private fun deleteRef(refName: String, refList: MutableList<AbstractBranch>) {
        val targetRef = refList.find { it.id == refName }
        if (targetRef != null) {
            refList.remove(targetRef)
            targetRef.commit.removeBranch(targetRef)
            targetRef.commit.repositionBranches(canvas)
            targetRef.removeFrom(canvas)
        }
        calculateLostCommits()
    }

    private fun findBranch(branchName: String): AbstractBranch? = branches.find { it.id == branchName }
}