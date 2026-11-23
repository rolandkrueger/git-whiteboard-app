package graph

/** Cross-repo operations between two [GitGraph]s (ADR 0005). Ref movement stays in GitGraph; this only orchestrates. */
object RemoteOps {
    enum class Kind { SUCCESS, INFO, ERROR }

    class Result(val kind: Kind, val message: String)

    /** Single availability check (reused by fetch): a remote is unavailable when missing or cleared. */
    fun isAvailable(remote: GitGraph?): Boolean = remote != null && !remote.wasCleared

    /** Pushes [local]'s current branch to its upstream (or `origin/<same name>`) in [remote]; no force push. */
    fun push(local: GitGraph, remote: GitGraph?): Result {
        if (remote == null || !isAvailable(remote)) return Result(Kind.ERROR, "remote not available")
        val status = local.upstreamStatus()
        if (status.detached) return Result(Kind.ERROR, "Cannot push: HEAD is detached")
        val branchName = status.branchName ?: return Result(Kind.ERROR, "Cannot push: no branch checked out")
        val targetName = local.branchUpstream(branchName)?.removePrefix("origin/") ?: branchName
        val localTip = local.branchTip(branchName) ?: return Result(Kind.ERROR, "Cannot push: no branch checked out")
        val remoteTip = remote.branchTip(targetName)

        if (remoteTip != null && remoteTip.id != localTip.id && remoteTip.id !in local.ancestorIds(localTip)) {
            remote.flashAlert()
            return Result(Kind.ERROR, "rejected: non-fast-forward - fetch first")
        }

        val upToDate = remoteTip != null && remoteTip.id == localTip.id
        if (!upToDate) {
            val missing = local.describeMissingCommits(localTip) { remote.doesCommitExist(it) }
            remote.receiveCommits(missing, remoteTip?.swimlane)
            val newTip = remote.findCommitFor(localTip.id) ?: return Result(Kind.ERROR, "push failed")
            if (remoteTip != null) {
                remote.moveBranchTo(targetName, newTip)
            } else {
                remote.createBranchAt(targetName, newTip, local.branchColor(branchName))
            }
        }
        // Local bookkeeping: tracking label and upstream (push -u behaviour for a branch without upstream).
        val label = local.getRemoteBranches().find { it.name == targetName }
        if (label == null || label.commit.id != localTip.id) local.setRemoteBranch(targetName, localTip)
        if (local.branchUpstream(branchName) == null) local.setBranchUpstream(branchName, "origin/$targetName")
        return if (upToDate) Result(Kind.INFO, "Everything up-to-date")
        else Result(Kind.SUCCESS, "Pushed $branchName -> origin/$targetName")
    }
}
