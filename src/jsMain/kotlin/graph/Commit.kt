package graph

import components.CommitCircle
import components.Line
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
    val config: GitGraphConfiguration = GitGraphConfiguration()
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
            config.leftOffset + swimlane * config.swimlaneDistance,
            config.bottomOffset - linePosition * config.commitDistance
        ),
        commitColor
    )

    override fun render(layer: Layer) {
        commitCircle.render(layer)
        val parent = parent
        // draw line to parent commit(s)
        if (parent != null) {
            val dockPoint: Point = parent.commitCircle.getUpperDockPoint()
            parentLine = Line(
                dockPoint,
                commitCircle.getLowerDockPoint()
            )
            parentLine?.render(layer)
            val mergedCommit = mergedCommit
            if (mergedCommit != null) {
                mergedParentLine = Line(mergedCommit.commitCircle.getUpperDockPoint(), commitCircle.getLowerDockPoint())
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
