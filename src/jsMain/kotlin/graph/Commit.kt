package graph

import components.CommitCircle
import components.CommitEdge
import components.EdgeKind
import components.Point
import components.Renderable
import config.GitGraphConfiguration
import konvajs.Layer

class Commit(
    var id: String,
    val linePosition: Int,
    var swimlane: Int,
    val parent: Commit? = null,
    val commitColor: String = "",
    val config: GitGraphConfiguration = GitGraphConfiguration(),
    /** Conflicting change marker (ADR 0006): the commit touches the one shared imaginary spot. */
    conflicting: Boolean = false
) : Renderable {
    var conflicting = conflicting
        private set

    var isLostInReflog = false
        set(value) {
            commitCircle.isLostInReflog = value
            field = value
        }
        get() = commitCircle.isLostInReflog

    var mergedCommit: Commit? = null
    val branches = mutableSetOf<AbstractBranch>()
    var commitCircle = createCommitCircle()
    var parentLine: CommitEdge? = null
    var mergedParentLine: CommitEdge? = null
    var childCommit: Commit? = null

    fun addBranch(branch: AbstractBranch) = branches.add(branch)
    fun removeBranch(branch: AbstractBranch) = branches.remove(branch)

    private fun createCommitCircle(): CommitCircle = CommitCircle(
        id, Point(
            config.leftOffset + swimlane * config.swimlaneDistance,
            config.bottomOffset - linePosition * config.commitDistance
        ),
        commitColor,
        conflicting
    )

    override fun render(layer: Layer) {
        commitCircle.render(layer)
        val parent = parent
        // draw line to parent commit(s)
        if (parent != null) {
            val dockPoint: Point = parent.commitCircle.getUpperDockPoint()
            // The parent is always our own branch tip (also for merge commits). If the child sits in
            // another lane, it was placed in a free lane, so the straight run belongs in the child's
            // lane (bend near the parent) and never cuts through commits in the parent's lane.
            parentLine = CommitEdge(
                dockPoint,
                commitCircle.getLowerDockPoint(),
                EdgeKind.BRANCH_OFF,
                config.commitDistance
            )
            parentLine?.render(layer)
            val mergedCommit = mergedCommit
            if (mergedCommit != null) {
                // Merged side branch: straight in its own lane, bend just below the merge commit
                mergedParentLine = CommitEdge(
                    mergedCommit.commitCircle.getUpperDockPoint(),
                    commitCircle.getLowerDockPoint(),
                    EdgeKind.MERGE,
                    config.commitDistance
                )
                mergedParentLine?.render(layer)
            }
        }
    }

    fun shiftRight(layer: Layer) {
        removeFrom(layer)
        swimlane++
        commitCircle = createCommitCircle()
        childCommit?.rerender(layer)
        render(layer)
        branches.forEach {
            it.attachToCommit(this, layer)
        }
        repositionBranches(layer)
    }

    fun setConflicting(value: Boolean, layer: Layer) {
        if (conflicting == value) return
        val lost = isLostInReflog
        conflicting = value
        rerender(layer)
        isLostInReflog = lost
    }

    fun rerender(layer: Layer) {
        removeFrom(layer)
        commitCircle = createCommitCircle()
        childCommit?.rerender(layer)
        render(layer)
    }

    override fun removeFrom(layer: Layer) {
        commitCircle.removeFrom(layer)
        parentLine?.removeFrom(layer)
        mergedParentLine?.removeFrom(layer)
    }

    override fun toString(): String = id

    fun repositionBranches(layer: Layer) {
        val branchesCopy = ArrayList(branches)
        branches.clear()
        branchesCopy.forEach { branch ->
            branch.attachToCommit(this, layer)
        }
    }

    fun isAncestorOf(commit: Commit): Boolean = commit.isParentCommit(this)

    private fun isParentCommit(commit: Commit): Boolean {
        if (parent == null) {
            // reached first commit in history
            return false
        }
        return if (parent == commit || mergedCommit == commit) {
            true
        } else {
            parent.isParentCommit(commit) || (mergedCommit?.isParentCommit(commit) ?: false)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as Commit

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun show(doShow: Boolean, layer: Layer) {
        if (doShow) {
            removeFrom(layer)
            render(layer)
        } else {
            removeFrom(layer)
        }
    }
}
