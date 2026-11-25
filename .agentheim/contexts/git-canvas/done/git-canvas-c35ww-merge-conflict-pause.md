---
id: git-canvas-c35ww
title: Merge and pull (merge) stop on a conflict until Continue or Abort
status: done
type: feature
context: git-canvas
created: 2026-11-25
completed: 2026-11-25
depends_on: [git-canvas-mrfrh]
blocks: [git-canvas-nymhr]
tags: [conflict, merge, pull]
related_adrs: [0006, 0005]
related_research: []
prior_art: [git-canvas-sz7p8, git-canvas-f2ced]
---

## Why
Learners should see that a merge whose two sides both changed the same spot does not just produce
a merge commit: Git stops and waits for the user. This is the graph-level part of a conflict that
the app can show (ADR 0006, decisions 2, 3, 5).

## What
- **Conflict rule:** a non-fast-forward merge conflicts when, counted from the merge base, both the
  current branch side and the merged side contain at least one commit with the conflict marker
  (git-canvas-mrfrh). Fast-forward never conflicts; `No fast-forward merge` of an ancestor-only
  case follows the same rule (no marked commit on one side, no conflict).
- **Pending merging state** in the repo (`GitGraph`), instead of creating the merge commit:
  - region header badge `MERGING - CONFLICT` (red, same header area as the REMOTE pill);
  - the marked commits on both sides since the merge base are highlighted (e.g. red ring);
  - status message "CONFLICT: <current> and <other> both changed the same spot. Continue or Abort."
- **Controls:** the Merge/Rebase tab shows `Continue` and `Abort` buttons only while the active
  repo has a pending conflict (hidden otherwise, also when switching to a repo without one).
  - `Continue` creates the merge commit exactly as the normal merge would (unmarked), clears
    highlights and badge.
  - `Abort` leaves the graph exactly as before the merge.
- **Pull with merge** (`RemoteOps.pull`, rebase checkbox unset): the fetch happens as today, then
  the merge with `origin/<name>` follows the same rule and can end in the pending state; the pull
  message says so.
- **While pending:** every other graph operation on that repo (commit, amend, branch, tag,
  checkout, merge, rebase, reset, cherry-pick, GC, clear, push, fetch, pull, clone of it) is
  refused with "Resolve the conflict first: Continue or Abort"; `Share` is refused while any repo
  has a pending conflict. Other repos keep working; switching the active repo works.
- The pending state is not part of the snapshot.

## Acceptance criteria
- [ ] main and feature each have a marked commit since their merge base: merging feature into main does not create a merge commit; the region shows `MERGING - CONFLICT`, both marked commits are highlighted, and Continue/Abort appear on the Merge/Rebase tab.
- [ ] `Continue` then creates the same merge commit (id, parents, position) a conflict-free merge would have created; badge, highlights and buttons disappear.
- [ ] `Abort` restores the graph exactly (no new commit, branches and HEAD unchanged); badge, highlights and buttons disappear.
- [ ] Only one side marked, neither side marked, or a fast-forward: merge behaves exactly as today (no pending state).
- [ ] Pull (merge) in a clone whose local branch and `origin/<name>` both have a marked commit since their merge base ends in the pending state after the fetch; Continue/Abort work the same way.
- [ ] While pending, each listed operation on that repo is refused with the message and changes nothing; `Share` is refused; another repo can be activated and operated normally, and Continue/Abort are hidden while it is active and shown again when switching back.
- [x] Manual (Merge/Rebase and Pull sections) describes the conflict demonstration (how to set it up with markers, what the audience sees, Continue/Abort); git-canvas README describes the pending merging state.
- [x] Full verification build passes. (Browser console check not done.)
- [ ] During a live demo, the stopped merge reads clearly as "Git is waiting for me" before Continue. [human-eye]

## Notes
- Contract: ADR 0006. The pending state should be designed so git-canvas-nymhr can reuse it for
  rebase (one pending-operation slot per repo, kind = merge | rebase).
- Current merge: `GitGraph.merge(noFF, targetBranchId)`; pull: `RemoteOps.pull`.
- Badge: `RepoRegion` header already has `layoutHeader()` for REMOTE / `origin -> name`.
- Pulled conflicts are the classic demo: two clones each commit a marked change, the second push is
  rejected (non-fast-forward), pull -> conflict.

## Outcome
Merge and pull (merge) now stop on a conflict. `GitGraph.merge` computes marked commits on each side since the merge base (difference of `ancestorIds` of both tips, so merge commits are handled); both sides marked -> a `PendingOperation` (kind MERGE) is stored instead of the merge commit. `Continue` (`continuePending`) creates the normal merge commit, `Abort` (`abortPending`) just clears the state. The slot (`PendingKind.MERGE | REBASE`, badge, message, highlight ids) is in `graph/PendingOperation.kt` and is meant for reuse by git-canvas-nymhr (`continuePending` has a placeholder REBASE branch).

Key files: graph/PendingOperation.kt, graph/GitGraph.kt, graph/RemoteOps.kt, components/RepoRegion.kt (red header badge), components/CommitCircle.kt (`center()`), App.kt (guarded buttons, Continue/Abort wiring, Share refusal), index.html (pending controls on the Merge/Rebase tab), manual.adoc, BC README.

Refusals: App.kt `doWhenGuardedButtonClicked` wraps all graph-operation buttons (incl. clone); commit double-click checkout refuses via `GitGraph.onPendingRefused`; `RemoteOps` push/fetch/pull refuse (a push into a pending remote too); Share refuses when any repo is pending.

Growth re-layout: pending state is not in the snapshot, so `rebuildGraph` carries it over (`restorePending` from commit ids) - chosen over preventing rebuilds because other repos' growth can legitimately push a pending repo.

Not verified in a browser (build only; no UI test infra): all acceptance criteria were implemented but the behaviour checks and "no new browser console errors" and the [human-eye] criterion remain unticked.

Iteration 2: every canvas entry point that changes graph state is now guarded while a conflict is pending: commit double-click checkout (`checkoutHandler`: initial commit, addCommit, restoreFromSnapshot, etc.) and branch/tag label double-click checkout (new shared `labelCheckoutHandler`, used in createBranchAt, addBranch, addTag and the branch/tag restore in restoreFromSnapshot). There is no label/commit dragging in the app (only region drag and canvas pan, which do not change graph state). Control-panel buttons use `doWhenGuardedButtonClicked`; push/fetch/pull refuse in RemoteOps. `continuePending` now checks out the branch recorded when the merge stopped (`PendingOperation.mergeOntoBranchId`) before building the merge commit.

## Verifier note (iteration 1)

**REASONS:**
- Criterion "While pending, each listed operation on that repo is refused with the message and changes nothing" is not met for checkout. Double-clicking a **branch label** or a **tag label** on the canvas still checks out with no pending guard. The guard was added only to the commit-circle path, `checkoutHandler` at src/jsMain/kotlin/graph/GitGraph.kt:91. These five handlers in the same file call `checkout(...)` directly and are not guarded:
  - :446 `createBranchAt`: `branch.onDoubleClick { checkout(name, showLostCommits) }`
  - :499 `addBranch`: `branch.onDoubleClick { checkout(id, showLostCommits) }`
  - :512 `addTag`: `tag.onDoubleClick { checkout(tag.commit.id, showLostCommits) }`
  - :1027 `restoreFromSnapshot`, branches: `branch.onDoubleClick { checkout(bs.id, showLostCommits) }`
  - :1040 `restoreFromSnapshot`, tags: `tag.onDoubleClick { checkout(ts.id, showLostCommits) }`
  The restore paths also matter because a growth re-layout rebuild re-creates every label through `restoreFromSnapshot`.
- Knock-on effect on criterion "`Continue` then creates the same merge commit (id, parents, position) a conflict-free merge would have created": if HEAD is moved by one of those double-clicks while the merge is pending, `continuePending` builds the merge commit on whatever branch or detached HEAD is now checked out — wrong first parent and wrong id.

**SUGGESTED_FIX:** Put the pending guard (`if (pending != null) GitGraph.onPendingRefused?.invoke() else checkout(...)`) on all branch-label and tag-label double-click handlers. The cleanest way is one shared label-checkout handler, like `checkoutHandler`, used at GitGraph.kt:446, :499, :512, :1027 and :1040. Then re-check that no other canvas interaction (drag of labels, etc.) changes the graph while pending.

**ITERATION_HINT:** likely-fixable
