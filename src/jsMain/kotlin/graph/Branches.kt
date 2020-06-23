package graph

import components.*
import config.GitGraphConfiguration
import fabricjs.FabricCanvas
import fabricjs.Point
import fabricjs.plus

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
            GitGraphConfiguration.labelYOffset * commit.branches.size
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