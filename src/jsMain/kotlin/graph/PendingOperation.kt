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
    val mergeOntoBranchId: String? = null
)
