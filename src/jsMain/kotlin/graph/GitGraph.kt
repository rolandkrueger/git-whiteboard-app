package graph

import components.CommitCircle
import components.Renderable
import config.GitGraphConfiguration
import fabricjs.FabricCanvas
import fabricjs.Point

class GitGraph(private val canvas: FabricCanvas) {
    val commits = mutableListOf<Commit>()
    val branches = mutableListOf<Branch>()
    val tags = mutableListOf<Tag>()
    var head: Head
    private var globalCommitNumber = 0
    private var globalSwimlaneCounter = 0

    init {
        val initialCommit = Commit("m1", globalCommitNumber++, 0)
        initialCommit.render(canvas)
        commits.add(initialCommit)
        head = Head(initialCommit)
        branches.add(head)
        addBranch("master", 2)
    }

    fun addCommit(): Commit {
        if (head.isDetached && head.swimlane == -1) {
            head.swimlane = globalSwimlaneCounter++
        }
        val swimlane = head.targetBranch?.swimlane ?: head.swimlane
        val currentBranch = currentBranch()
        val commit = Commit(calcCommitId(currentBranch), globalCommitNumber++, swimlane, head.commit)
        moveBranch(currentBranch, currentBranch.commit, commit)
        commits.add(commit)
        commit.render(canvas)
        return commit
    }

    /**
     * Calculates the current branch based on the HEAD pointer. If the HEAD is detached, return the HEAD as a result.
     * Otherwise return the currently checked out branch.
     */
    private fun currentBranch(): Branch {
        return if (head.isDetached) {
            head
        } else {
            head.targetBranch!!
        }
    }

    private fun moveBranch(branch: Branch, from: Commit, to: Commit) {
        from.removeBranch(branch)
        to.removeBranch(head)
        to.addBranch(branch)
        to.addBranch(head)
        branch.commit = to
        head.commit = to
    }

    fun addBranch(id: String) {
        addBranch(id, 1)
    }

    private fun addBranch(id: String, counter: Int) {
        val branch = Branch(id, globalSwimlaneCounter++, commit = head.commit, counter = counter)
        branches.add(branch)
        head.commit.addBranch(branch)
        checkout(id)
    }

    fun addTag(id: String) = tags.add(Tag(id, head.commit))

    fun checkout(id: String) {
        val targetBranch = branches.find { it.id == id }
        if (targetBranch != null) {
            moveBranch(head, head.commit, targetBranch.commit)
            head.targetBranch = targetBranch
        } else {
            val targetCommit = commits.find { it.id == id }
            if (targetCommit != null) {
                // a commit id was given to check out: create a detached HEAD
                head.swimlane = -1 // calculate new swimlane as soon as a new commit is added
                moveBranch(head, head.commit, targetCommit)
                head.targetBranch = null
            }
        }
    }

    fun merge(targetBranchId: String) {
        val targetBranch = branches.find { it.id == targetBranchId }
        if (targetBranch != null && targetBranch != currentBranch()) {
            val mergeCommitId = "${currentBranch().commit.id}${targetBranch.commit.id}"
            val mergeCommit = addCommit()
            mergeCommit.id = mergeCommitId
            mergeCommit.mergedCommit = targetBranch.commit
        }
    }

    fun calculateLostCommits() {
        commits.forEach {
            it.commitCircle.isLostInReflog = true
        }
        val resetLostInReflog: (Commit) -> Unit = { it.isLostInReflog = false }

        branches.forEach {
            traverseHistory(it.commit, resetLostInReflog)
        }
        traverseHistory(head.commit, resetLostInReflog)
    }

    private fun traverseHistory(commit: Commit, callback: (Commit) -> Unit) {
        callback(commit)
        if (commit.parent != null) {
            traverseHistory(commit.parent, callback)
        }
    }

    private fun calcCommitId(branch: Branch) = "${branch.id.first()}${branch.counter++}"

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
    val branches = mutableSetOf<Branch>()
    val commitCircle = CommitCircle(
        id, Point(
            GitGraphConfiguration.leftOffset + swimlane * GitGraphConfiguration.swimlaneDistance,
            GitGraphConfiguration.bottomOffset - linePosition * GitGraphConfiguration.commitDistance
        )
    )

    fun addBranch(branch: Branch) {
        branches.add(branch)
    }

    fun removeBranch(branch: Branch) {
        branches.remove(branch)
    }

    override fun render(canvas: FabricCanvas) {
        commitCircle.render(canvas)
    }

    override fun toString(): String = id
}

open class Branch(val id: String, var swimlane: Int, var counter: Int = 1, var commit: Commit) {
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

class Head(commit: Commit, var targetBranch: Branch? = null) : Branch("HEAD", 0, commit = commit) {
    val isDetached: Boolean
        get() = targetBranch == null
}

class Tag(val id: String, val commit: Commit) {
    override fun toString(): String = "Tag $id -> $commit"
}
