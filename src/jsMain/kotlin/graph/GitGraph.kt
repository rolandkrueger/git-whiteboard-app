package graph

import components.CommitCircle
import config.GitGraphConfiguration
import fabricjs.Point

class GitGraph {
    val commits = mutableListOf<Commit>()
    val branches = mutableListOf<Branch>()
    val tags = mutableListOf<Tag>()
    var currentBranch: Branch
    var head: Head
    private var globalCommitNumber = 0
    private var globalSwimlaneCounter = 0

    init {
        val initialCommit = Commit("m1", globalCommitNumber++, 0)
        commits.add(initialCommit)
        head = Head(initialCommit)
        currentBranch = Branch("master", globalSwimlaneCounter++, counter = 2, commit = initialCommit)
        currentBranch.isActive = true
        initialCommit.addBranch(currentBranch)
        branches.add(currentBranch)
        head.targetBranch = currentBranch
    }

    fun addCommit(): Commit {
        val swimlane = head.targetBranch?.swimlane ?: head.swimlane

        val commit = Commit(calcCommitId(currentBranch), globalCommitNumber++, swimlane, head.commit)
        moveBranch(currentBranch, currentBranch.commit, commit)
        currentBranch.commit = commit
        head.commit = commit
        commits.add(commit)
        return commit
    }

    private fun moveBranch(branch: Branch, from: Commit, to: Commit) {
        from.removeBranch(branch)
        to.addBranch(branch)
    }

    fun addBranch(id: String) {
        branches.add(Branch(id, globalSwimlaneCounter++, commit = head.commit))
        checkout(id)
    }

    fun addTag(id: String) = tags.add(Tag(id, head.commit))

    fun checkout(id: String) {
        val targetBranch = branches.find { it.id == id }
        if (targetBranch != null) {
            currentBranch.isActive = false
            targetBranch.isActive = true
            currentBranch.commit.removeBranch(head)
            targetBranch.commit.addBranch(targetBranch)
            currentBranch = targetBranch
            head.commit = currentBranch.commit
            head.targetBranch = currentBranch
        } else {
            val targetCommit = commits.find { it.id == id }
            if (targetCommit != null) {
                currentBranch.isActive = false
                currentBranch = head
                head.swimlane = globalSwimlaneCounter++
                head.commit.removeBranch(head)
                targetCommit.addBranch(head)
                head.commit = targetCommit
                head.targetBranch = null
            }
        }
    }

    fun merge(targetBranchId: String) {
        val targetBranch = branches.find { it.id == targetBranchId }
        if (targetBranch != null && targetBranch != currentBranch) {
            val mergeCommitId = "${currentBranch.commit.id}${targetBranch.commit.id}"
            val mergeCommit = addCommit()
            mergeCommit.id = mergeCommitId
            mergeCommit.mergedCommit = targetBranch.commit
        }
    }

    fun calculateLostCommits() {
        commits.forEach {
            it.commitCircle.isLostInReflog = true }
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

class Commit(var id: String, linePosition: Int, val swimlane: Int, val parent: Commit? = null) {
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

    override fun toString(): String = id
}

open class Branch(val id: String, var swimlane: Int, var counter: Int = 1, var commit: Commit) {
    var isActive: Boolean = false

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
