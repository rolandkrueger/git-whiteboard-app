package graph

enum class PendingKind { MERGE, REBASE }

/**
 * An operation stopped on a conflict until Continue or Abort (ADR 0006); a repo holds at most one.
 * Refers to commits by id so it survives a graph rebuild (snapshot/restore).
 */
class PendingOperation(
    val kind: PendingKind,
    /** Region header badge text, e.g. `MERGING - CONFLICT`. */
    val badge: String,
    /** Status message shown next to Continue/Abort. */
    val message: String,
    /** Marked commits on both sides that are highlighted while pending. */
    val highlightIds: List<String>,
    /** MERGE: id of the commit being merged in (second parent of the merge commit). */
    val mergeSourceCommitId: String? = null,
    /** MERGE: id of the branch (or detached HEAD) that was checked out when the merge stopped. */
    val mergeOntoBranchId: String? = null,
    /** REBASE: id of the branch being rebased (its label stays on the original tip until the end); null = detached HEAD. */
    val rebaseBranchId: String? = null,
    /** REBASE: id of the commit HEAD was on when the rebase started (Abort returns a detached HEAD there). */
    val rebaseOriginalHeadId: String? = null,
    /** REBASE: ids of the original commits to replay, oldest first. */
    val rebaseOriginalIds: List<String> = emptyList(),
    /** REBASE: index in [rebaseOriginalIds] of the commit that stopped on the conflict. */
    val rebaseNext: Int = 0,
    /** REBASE: swimlane the replayed copies are placed in. */
    val rebaseLane: Int = 0,
    /** REBASE: marked commits on the upstream side since the merge base. */
    val rebaseUpstreamIds: List<String> = emptyList()
)
