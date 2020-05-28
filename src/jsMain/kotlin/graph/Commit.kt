package graph

import components.CommitCircle
import components.Line
import components.Renderable
import config.GitGraphConfiguration
import fabricjs.FabricCanvas
import fabricjs.Point

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
