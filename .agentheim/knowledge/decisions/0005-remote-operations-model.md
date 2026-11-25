---
id: "0005"
title: Remote operations model (clone, remote-tracking labels, push/fetch/pull, layout of transferred commits)
status: accepted
date: 2026-11-23
scope: git-canvas
related_tasks: [git-canvas-b5f0r, git-canvas-wv6va, git-canvas-r6szj, git-canvas-w3ndg, git-canvas-rx1y3, git-canvas-sz7p8, git-canvas-ktja3, git-canvas-bdecz, git-canvas-r3dq7]
related_research: []
---

# ADR 0005: Remote operations model

## Status

Accepted

## Context

The vision requires push/pull/clone between repos on the shared canvas (ADR 0001). Repos are independent `GitGraph` instances with their own Konva layer and per-graph layout (swimlane, linePosition). Decisions confirmed by the trainer: clone creates a new region; no manual "add remote"; fetch and pull (merge or rebase); push rejects non-fast-forward; no force push; conflicts later.

## Decision

1. **Remote reference.** A repo has at most one remote, named `origin`, stored as a reference to the source `GitGraph` instance by a stable repo id (not by display name, because repos can be renamed). Only `Clone` creates it.
2. **Clone.** Creates a new repo region (counts toward max 3) holding a copy of ALL commits of the source (same IDs/colours/parent links, also swimlane/linePosition copied 1:1, so the clone looks identical), an `origin/<branch>` tracking label for every source branch, and ONE local branch: the source's current branch (or `main`, or the first branch if source HEAD is detached) checked out, with upstream `origin/<same name>`. Tags are copied. Lost commits and the source's other local-only state are not.
3. **Remote-tracking labels** are a separate kind (`RemoteBranch`), stored in their own list, NOT in `branches`: excluded from first-letter uniqueness, checkout, merge/rebase pickers (until pull needs them), delete, drag and reset. They only move through fetch/push/pull. They render visually distinct from local labels: outlined (unfilled) pill with dashed border in the branch colour, grey-ish text, and the text `origin/<name>`. *(Amended 2026-11-25, git-canvas-r3dq7: border and text are now a single dark red `#B71C1C` instead of branch colour and grey, for readability and the common red remote convention; still unfilled and dashed.)* This answers the README open question: yes, visually distinct.
4. **Upstream and target branch.** Push, fetch and pull act on the CURRENT branch's upstream. A local branch gets `upstream = origin/<name>` at clone, or on first successful push of a branch with no upstream (push -u behaviour: pushes to `origin/<same name>`, creating it). Detached HEAD or repo without remote: operation disabled/refused with a message. Fetch always updates all `origin/*` labels.
5. **Transfer of commits** (push into remote, fetch into local): only commits missing by ID are created, in topological order (parents first), keeping ID, colour and parent link. Layout is chosen by the RECEIVING graph: `linePosition = receiver.nextLinePosition++`; swimlane = the swimlane of the receiving ref (local branch the ref fast-forwards, or the `origin/<branch>` tracking ref's lane); if that lane is occupied by commits that are not ancestors of the new commit, the existing `shiftCommitsToTheRight` mechanism is used; a brand-new ref gets `globalSwimlaneCounter++`. Tracking labels sit on the lane of the commit they point to.
6. **Push rule.** Allowed iff the remote's branch tip is an ancestor of (or equal to) the local tip (fast-forward) or the remote branch does not exist. Otherwise rejected with a visible message `rejected: non-fast-forward - fetch first`; no state changes. When the pushed-to branch is checked out in the remote repo, the remote HEAD follows it (the remote behaves like a bare-ish demo repo; Git's receive.denyCurrentBranch is intentionally not modelled). After success, local `origin/<branch>` is updated.
7. **Pull** = fetch + (merge | rebase of the current branch onto its `origin/<name>`), reusing the existing merge (ff or merge commit) and rebase code paths. Fetch never touches local branches or the working HEAD.
8. **Unrelated histories** (e.g. two separately created blank repos) cannot be connected because remotes only come from clone; no handling needed.
9. **Snapshot (version 2).** `MultiRepoSnapshot` gains `nextCommitId`; `RepoSnapshot` gains `remoteRepoIndex: Int?` (index in `repos`), `remoteBranches: List<RemoteBranchSnapshot>(name, targetCommitId)`; `BranchSnapshot` gains `upstream: String?`. Missing fields on decode default to null/empty so version-1 URLs still open.

## Alternatives rejected

- Manual "add remote": not needed for training scenarios yet.
- Tracking labels inside `branches` with a flag: would leak into uniqueness/checkout/merge logic everywhere in the monolithic GitGraph.
- Copying only the checked-out branch on clone: loses the visually obvious "origin has other branches" demo.

## Consequences

- GitGraph gains cross-instance operations; they are coordinated from `App.kt`/a small `RemoteOps` helper so GitGraph instances do not hold hard cross-references except the remote id.
- If the remote repo is cleared or removed, remote operations show "remote not available" and tracking labels stay as they are.
