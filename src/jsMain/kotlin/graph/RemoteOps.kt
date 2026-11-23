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

    /** Updates all tracking labels (origin/...) of [local] from [remote]; local branches and HEAD are untouched. */
    fun fetch(local: GitGraph, remote: GitGraph?): Result {
        if (remote == null || !isAvailable(remote)) return Result(Kind.ERROR, "remote not available")
        val changes = mutableListOf<String>()
        val remoteBranches = remote.getBranches().filterIsInstance<Branch>()
        for (rb in remoteBranches) {
            val label = local.getRemoteBranches().find { it.name == rb.id }
            if (label != null && label.commit.id == rb.commit.id) continue
            val missing = remote.describeMissingCommits(rb.commit) { local.doesCommitExist(it) }
            local.receiveCommits(missing, label?.swimlane)
            val tip = local.findCommitFor(rb.commit.id) ?: return Result(Kind.ERROR, "fetch failed")
            local.setRemoteBranch(rb.id, tip)
            changes.add(if (label == null) "new branch origin/${rb.id}" else "updated origin/${rb.id}")
        }
        val names = remoteBranches.map { it.id }.toSet()
        for (label in local.getRemoteBranches().toList()) {
            if (label.name !in names) {
                local.removeRemoteBranch(label.name)
                changes.add("deleted origin/${label.name}")
            }
        }
        return if (changes.isEmpty()) Result(Kind.INFO, "Already up to date")
        else Result(Kind.SUCCESS, "Fetched: " + changes.joinToString(", "))
    }

    /** Pull = fetch, then merge the upstream tracking ref into the current branch (fast-forward when possible). */
    fun pull(local: GitGraph, remote: GitGraph?): Result {
        if (remote == null || !isAvailable(remote)) return Result(Kind.ERROR, "remote not available")
        val status = local.upstreamStatus()
        if (status.detached) return Result(Kind.ERROR, "Cannot pull: HEAD is detached")
        val branchName = status.branchName ?: return Result(Kind.ERROR, "Cannot pull: no branch checked out")
        if (local.branchUpstream(branchName) == null) {
            return Result(Kind.ERROR, "Cannot pull: $branchName has no upstream branch")
        }
        val fetched = fetch(local, remote)
        if (fetched.kind == Kind.ERROR) return fetched
        val upstream = local.upstreamStatus().upstream
            ?: return Result(Kind.ERROR, "Cannot pull: upstream of $branchName no longer exists on the remote")
        val localTip = local.branchTip(branchName) ?: return Result(Kind.ERROR, "Cannot pull: no branch checked out")
        val upstreamTip = local.getRemoteBranches().first { it.id == upstream }.commit
        val prefix = if (fetched.kind == Kind.SUCCESS) fetched.message + "; " else ""
        if (upstreamTip.id in local.ancestorIds(localTip)) {
            return if (prefix.isEmpty()) Result(Kind.INFO, "Already up to date") else Result(Kind.SUCCESS, prefix + "Already up to date")
        }
        val fastForward = localTip.id in local.ancestorIds(upstreamTip)
        local.merge(false, upstream)
        return Result(Kind.SUCCESS, prefix + if (fastForward) "Fast-forward" else "Merge made")
    }
}
