package graph

import components.*
import config.GitGraphConfiguration
import konvajs.Layer

abstract class AbstractBranch(val id: String, var swimlane: Int, var counter: Int = 1, var commit: Commit, val commitColor: String = "") :
    Renderable {

    var head: Head? = null

    abstract fun getLabel(): CommitLabel

    override fun render(layer: Layer) {
        getLabel().render(layer)
    }

    fun onDoubleClick(handler: () -> Unit) {
        getLabel().onDoubleClick(handler)
    }

    fun headRemoved() {
        getLabel().isActive = false
        head = null
        console.log("HEAD removed from $id")
    }

    fun checkedOut(head: Head) {
        getLabel().isActive = true
        this.head = head
        console.log("HEAD moved to $id")
    }

    fun isCheckedOut() = head != null

    fun attachToCommit(commit: Commit, layer: Layer) {
        commit.addBranch(this)
        getLabel().attachToCommit(commit, layer)
        head?.attachToBranch(this, layer)
    }

    override fun removeFrom(layer: Layer) {
        getLabel().removeFrom(layer)
    }

    fun shiftToNextSwimlane() {
        swimlane += 1
    }
}

open class Branch(id: String, swimlane: Int, counter: Int = 1, commit: Commit, commitColor: String) :
    AbstractBranch(id, swimlane, counter, commit, commitColor) {

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

    /** Remote-tracking ref this branch tracks (e.g. "origin/main"); data only for now. */
    var upstream: String? = null

    override fun toString(): String = "Branch $id -> $commit"
}

/**
 * Remote-tracking label such as `origin/main` (ADR 0005). Lives in its own list in GitGraph, never in
 * `branches`, so it is invisible to checkout, pickers, uniqueness, deletion, drag and reset.
 */
class RemoteBranch(val name: String, swimlane: Int, commit: Commit, commitColor: String) :
    AbstractBranch("origin/$name", swimlane, 1, commit, commitColor) {

    private val remoteLabel = RemoteBranchLabel(
        id,
        commitColor,
        commit.commitCircle.getRightDockPoint() + GitGraphConfiguration.labelOffset + Point(
            0,
            GitGraphConfiguration.labelYOffset * commit.branches.size
        )
    )

    override fun getLabel(): CommitLabel = remoteLabel

    override fun toString(): String = "RemoteBranch $id -> $commit"
}

class Head(commit: Commit, var targetBranch: AbstractBranch? = null) : AbstractBranch("HEAD", 0, commit = commit, commitColor = BranchColors.HEAD_COLOR) {
    val isDetached: Boolean
        get() = targetBranch == null
    private val headLabel = HeadLabel(Point(0, 0))

    override fun getLabel(): CommitLabel {
        return headLabel
    }

    fun attachToBranch(branch: AbstractBranch, layer: Layer) {
        if (isDetached) {
            commit.removeBranch(this)
        }
        getLabel().attachToLabel(branch.getLabel(), layer)
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

    override fun render(layer: Layer) {
        tagLabel.render(layer)
        tagLabel.attachToCommit(commit, layer)
    }

    override fun getLabel(): CommitLabel {
        return tagLabel
    }

    override fun toString(): String = "Tag $id -> $commit"
}
