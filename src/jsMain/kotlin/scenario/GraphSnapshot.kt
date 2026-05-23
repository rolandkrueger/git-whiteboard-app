package scenario

/**
 * Top-level snapshot of the full multi-repo canvas state. Version field allows detecting
 * schema changes in stored URLs (breaking on schema change as per ADR-0002).
 */
data class MultiRepoSnapshot(
    val version: Int = 1,
    val repos: List<RepoSnapshot>
)

/** Snapshot of a single GitGraph instance. */
data class RepoSnapshot(
    val repoName: String,
    val xOffset: Int,
    val yOffset: Int,
    val globalCommitNumber: Int,
    val globalSwimlaneCounter: Int,
    val commits: List<CommitSnapshot>,
    val branches: List<BranchSnapshot>,
    val tags: List<TagSnapshot>,
    val head: HeadSnapshot
)

data class CommitSnapshot(
    val id: String,
    val linePosition: Int,
    val swimlane: Int,
    val parentId: String?,
    val mergedParentId: String?,
    val commitColor: String,
    val isLostInReflog: Boolean
)

data class BranchSnapshot(
    val id: String,
    val swimlane: Int,
    val targetCommitId: String,
    val commitColor: String
)

data class TagSnapshot(
    val id: String,
    val targetCommitId: String
)

data class HeadSnapshot(
    val targetBranchId: String?,
    val detachedCommitId: String?
)
