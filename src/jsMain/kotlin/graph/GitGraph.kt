package graph

import components.*
import config.GitGraphConfiguration
import fabricjs.FabricCanvas
import fabricjs.Point
import fabricjs.plus

class GitGraph(private val canvas: FabricCanvas) {
    private val checkoutHandler: (Commit) -> (() -> Unit)
    private val commits = mutableListOf<Commit>()
    private val branches = mutableListOf<AbstractBranch>()
    private var globalCommitNumber = 0
    private var globalSwimlaneCounter = 0
    var head: Head

    init {
        head = Head(Commit("", 0, 0))
        checkoutHandler = { commit -> { checkout(commit.id) } }
    }

    fun initGraph() {
        if (globalCommitNumber > 0) {
            throw IllegalStateException("Git graph may only be initialized once.")
        }

        console.log("Initialize a new Git graph")
        val initialCommit = Commit("m1", globalCommitNumber++, 0)
        initialCommit.commitCircle.onDoubleClick(checkoutHandler(initialCommit))
        commits.add(initialCommit)
        head = Head(initialCommit)
        branches.add(head)

        initialCommit.render(canvas)
        addBranch("master", 2)
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
        val commit = Commit(id, globalCommitNumber++, swimlane, head.commit)
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
        return addBranch(id, 1)
    }

    private fun addBranch(id: String, counter: Int): Branch {
        console.log("Adding branch $id")
        val branch = Branch(id, globalSwimlaneCounter++, commit = head.commit, counter = counter)
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
    }

    fun checkout(id: String) {
        console.log("Checking out $id")
        val oldHeadCommit = head.commit
        val targetBranch = branches.find { it.id == id }
        if (targetBranch != null) {
            // checking out a branch
            head.targetBranch?.headRemoved()
            head.targetBranch = targetBranch
            targetBranch.checkedOut()
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
        val targetBranch = branches.find { it.id == targetBranchId }
        if (targetBranch != null && targetBranch != currentBranch()) {
            val mergeCommitId = "${currentBranch().commit.id}${targetBranch.commit.id}"
            addCommit(targetBranch.commit, mergeCommitId)
        }
    }

    fun calculateLostCommits() {
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
        traverseHistory(head.commit, resetLostInReflog)
    }


    private fun calcCommitId(branch: AbstractBranch) = "${branch.id.first()}${branch.counter++}"

    override fun toString(): String {
        return commits.reversed().joinToString("\n")
    }
}

class Commit(var id: String, linePosition: Int, val swimlane: Int, val parent: Commit? = null) : Renderable {
    var isLostInReflog = false
        set(value) {
            commitCircle.isLostInReflog = value
            field = value
        }

    var mergedCommit: Commit? = null
    val branches = mutableSetOf<AbstractBranch>()
    val commitCircle = CommitCircle(
        id, Point(
            GitGraphConfiguration.leftOffset + swimlane * GitGraphConfiguration.swimlaneDistance,
            GitGraphConfiguration.bottomOffset - linePosition * GitGraphConfiguration.commitDistance
        )
    )

    fun addBranch(branch: AbstractBranch) {
        branches.add(branch)
    }

    fun removeBranch(branch: AbstractBranch) {
        branches.remove(branch)
    }

    override fun render(canvas: FabricCanvas) {
        commitCircle.render(canvas)
        val parent = parent
        // draw line to parent commit(s)
        if (parent != null) {
            if (swimlane == parent.swimlane) {
                Line(
                    parent.commitCircle.getUpperDockPoint(),
                    commitCircle.getLowerDockPoint()
                ).render(canvas)
            } else {
                Line(
                    parent.commitCircle.getRightDockPoint(),
                    commitCircle.getLowerDockPoint()
                ).render(canvas)
            }
            val mergedCommit = mergedCommit
            if (mergedCommit != null) {
                Line(mergedCommit.commitCircle.getUpperDockPoint(), commitCircle.getLowerDockPoint()).render(canvas)
            }
        }
    }

    override fun toString(): String = id

    fun repositionBranches(canvas: FabricCanvas) {
        val branchesCopy = ArrayList(branches)
        branches.clear()
        branchesCopy.forEach { branch ->
            branch.attachToCommit(this, canvas)
        }
    }

}

abstract class AbstractBranch(val id: String, var swimlane: Int, var counter: Int = 1, var commit: Commit) :
    Renderable {

    abstract fun getLabel(): CommitLabel

    override fun render(canvas: FabricCanvas) {
        val label = getLabel()
        label.render(canvas)
    }

    fun onDoubleClick(handler: () -> Unit) {
        getLabel().onDoubleClick(handler)
    }

    fun headRemoved() {
        getLabel().isActive = false
        console.log("HEAD removed from $id")
    }

    fun checkedOut() {
        getLabel().isActive = true
        console.log("HEAD moved to $id")
    }

    fun attachToCommit(commit: Commit, canvas: FabricCanvas) {
        commit.addBranch(this)
        getLabel().attachToCommit(commit, canvas)
    }
}

open class Branch(id: String, swimlane: Int, counter: Int = 1, commit: Commit) :
    AbstractBranch(id, swimlane, counter, commit) {

    private val branchLabel = BranchLabel(
        id,
        commit.commitCircle.getRightDockPoint() + GitGraphConfiguration.labelOffset + Point(
            0,
            GitGraphConfiguration.labelYOffxet * commit.branches.size
        )
    )

    override fun getLabel(): CommitLabel {
        return branchLabel
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Branch

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String = "Branch $id -> $commit"
}

class Head(commit: Commit, var targetBranch: AbstractBranch? = null) : AbstractBranch("HEAD", 0, commit = commit) {
    val isDetached: Boolean
        get() = targetBranch == null
    private val headLabel = HeadLabel(Point(0, 0))

    override fun getLabel(): CommitLabel {
        return headLabel
    }

    fun attachToBranch(branch: AbstractBranch, canvas: FabricCanvas) {
        if (isDetached) {
            commit.removeBranch(this)
        }
        getLabel().attachToLabel(branch.getLabel(), canvas)
        commit = branch.commit
    }
}

class Tag(id: String, commit: Commit) : AbstractBranch(id, 0, commit = commit) {
    private val tagLabel = TagLabel(
        id,
        commit.commitCircle.getRightDockPoint() + GitGraphConfiguration.labelOffset
                + Point(0, commit.branches.size * (GitGraphConfiguration.labelHeight + 5))
    )

    init {
        commit.addBranch(this)
    }

    override fun render(canvas: FabricCanvas) {
        tagLabel.render(canvas)
        tagLabel.attachToCommit(commit, canvas)
    }

    override fun getLabel(): CommitLabel {
        return tagLabel
    }

    override fun toString(): String = "Tag $id -> $commit"
}
