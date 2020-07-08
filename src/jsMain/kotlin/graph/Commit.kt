package graph

import components.CommitCircle
import components.Line
import components.Renderable
import config.GitGraphConfiguration
import fabricjs.FabricCanvas
import fabricjs.Point

class Commit(
    var id: String,
    val linePosition: Int,
    var swimlane: Int,
    val parent: Commit? = null,
    val commitColor: String = ""
) : Renderable {
    var isLostInReflog = false
        set(value) {
            commitCircle.isLostInReflog = value
            field = value
        }
        get() = commitCircle.isLostInReflog

    var mergedCommit: Commit? = null
    val branches = mutableSetOf<AbstractBranch>()
    var commitCircle = createCommitCircle()
    var parentLine: Line? = null
    var mergedParentLine: Line? = null
    var childCommit: Commit? = null

    fun addBranch(branch: AbstractBranch) = branches.add(branch)
    fun removeBranch(branch: AbstractBranch) = branches.remove(branch)

    private fun createCommitCircle(): CommitCircle = CommitCircle(
        id, Point(
            GitGraphConfiguration.leftOffset + swimlane * GitGraphConfiguration.swimlaneDistance,
            GitGraphConfiguration.bottomOffset - linePosition * GitGraphConfiguration.commitDistance
        ),
        commitColor
    )

    override fun render(canvas: FabricCanvas) {
        commitCircle.render(canvas)
        val parent = parent
        // draw line to parent commit(s)
        if (parent != null) {
            val dockPoint: Point =
                if (swimlane == parent.swimlane) {
                    parent.commitCircle.getUpperDockPoint()
                } else if (swimlane < parent.swimlane) {
                    parent.commitCircle.getUpperDockPoint()
                }
                else {
                    parent.commitCircle.getUpperDockPoint()
                }
            parentLine = Line(
                dockPoint,
                commitCircle.getLowerDockPoint()
            )
            parentLine?.render(canvas)
            val mergedCommit = mergedCommit
            if (mergedCommit != null) {
                mergedParentLine = Line(mergedCommit.commitCircle.getUpperDockPoint(), commitCircle.getLowerDockPoint())
                mergedParentLine?.render(canvas)
            }
        }
    }

    fun shiftRight(canvas: FabricCanvas) {
        removeFrom(canvas)
        swimlane++
        commitCircle = createCommitCircle()
        childCommit?.rerender(canvas)
        render(canvas)
        branches.forEach {
            it.attachToCommit(this, canvas)
        }
        repositionBranches(canvas)
    }

    fun rerender(canvas: FabricCanvas) {
        removeFrom(canvas)
        commitCircle = createCommitCircle()
        childCommit?.rerender(canvas)
        render(canvas)
    }

    override fun removeFrom(canvas: FabricCanvas) {
        commitCircle.removeFrom(canvas)
        parentLine?.removeFrom(canvas)
        mergedParentLine?.removeFrom(canvas)
    }

    override fun toString(): String = id

    fun repositionBranches(canvas: FabricCanvas) {
        val branchesCopy = ArrayList(branches)
        branches.clear()
        branchesCopy.forEach { branch ->
            branch.attachToCommit(this, canvas)
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

    fun show(doShow: Boolean, canvas: FabricCanvas) {
        if (doShow) {
            render(canvas)
        } else {
            commitCircle.removeFrom(canvas)
            parentLine?.removeFrom(canvas)
            mergedParentLine?.removeFrom(canvas)
        }
    }
}
