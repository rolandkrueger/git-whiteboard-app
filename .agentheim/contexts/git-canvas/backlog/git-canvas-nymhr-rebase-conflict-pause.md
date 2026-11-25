---
id: git-canvas-nymhr
title: Rebase and pull (rebase) stop mid-way on a conflict until Continue or Abort
status: backlog
type: feature
context: git-canvas
created: 2026-11-25
completed:
depends_on: [git-canvas-c35ww]
blocks: []
tags: [conflict, rebase, pull]
related_adrs: [0006, 0005]
related_research: []
prior_art: [git-canvas-ktja3, git-canvas-a95rx]
---

## Why
The half-finished rebase is the conflict situation that is hardest to draw on a whiteboard: Git
stops in the middle, HEAD is detached, some commits are already replayed and the branch still
points at the old tip. Showing this step by step is where the tool adds the most value
(ADR 0006, decisions 2, 4, 5).

## What
- **Step-wise rebase:** `GitGraph.rebase` replays the commits one by one on a detached HEAD
  (instead of moving the branch first and replaying everything at once). Without conflicts the end
  result must be identical to today (same new ids `<id>*`, same swimlanes and positions, branch on
  the new tip, HEAD attached).
- **Conflict rule:** replaying a marked commit (git-canvas-mrfrh) stops the rebase when the
  upstream side (from the merge base to the target) contains at least one marked commit. Every such
  marked commit stops again (one conflict per replayed marked commit).
- **Pending rebasing state** (reusing the pending-operation slot from git-canvas-c35ww, kind =
  rebase):
  - region badge `REBASING n/m - CONFLICT` (n = commit being replayed, m = total);
  - HEAD detached on the last successfully replayed commit (or on the target when the first commit
    conflicts); the branch label stays on the original tip; the original commits not yet replayed
    stay visible where they are;
  - the stopped marked commit and the marked upstream commit(s) are highlighted;
  - status message "CONFLICT while replaying <id>. Continue or Abort."
- **Continue** replays the stopped commit (the copy keeps the marker), then goes on until the next
  conflict (state updates n) or the end: branch moves to the new tip, HEAD re-attaches, badge and
  buttons disappear.
- **Abort** restores branch and HEAD exactly as before the rebase; replayed copies become lost
  commits (visible only with `Show lost commits`), as in Git.
- **Pull with rebase** (`RemoteOps.pull`, rebase checkbox set) uses the same step-wise rebase and
  can end in the pending state after the fetch.
- Same refusal rules while pending as git-canvas-c35ww; Continue/Abort buttons are the same ones.

## Acceptance criteria
- [ ] Rebasing without any marked commits produces exactly the same graph as before this change (ids, swimlanes, line positions, branch and HEAD), including the fast-forward and "already up to date" cases.
- [ ] feature has three commits, the second marked; main has a marked commit since the merge base: rebasing feature onto main replays the first commit, then stops with `REBASING 2/3 - CONFLICT`, HEAD detached on the first replayed copy, the feature label still on the original tip, the original second and third commits still visible, the conflicting commits highlighted.
- [ ] `Continue` replays the second commit (copy keeps the marker) and the third, moves feature to the new tip, re-attaches HEAD and clears badge/highlights/buttons.
- [ ] With two marked commits on feature, the rebase stops twice; the badge counter reflects the current commit each time.
- [ ] `Abort` at any stop restores feature and HEAD to exactly the pre-rebase state; the replayed copies are lost commits (hidden unless `Show lost commits` is on).
- [ ] Marked commit on only one side: rebase runs through without stopping.
- [ ] Pull (rebase) in a clone with a marked local commit and a marked commit on `origin/<name>` since the merge base ends in the pending rebasing state; Continue/Abort work as above.
- [ ] While pending, other operations on that repo and `Share` are refused exactly as for a pending merge; switching the active repo shows/hides Continue/Abort correctly.
- [ ] Manual (Rebase and Pull sections) describes the step-wise conflict demonstration; git-canvas README describes the pending rebasing state; ADR 0006 consequences hold.
- [ ] Full verification build passes; no new browser console errors.
- [ ] During a live demo, the half-finished rebase (detached HEAD, branch on the old tip, remaining commits) is understandable at a glance. [human-eye]

## Notes
- Contract: ADR 0006. Current implementation: `GitGraph.rebase` (around line 581) moves the branch
  first and calls `addCommit` for each commit; detached-HEAD commit creation already exists
  (`head.isDetached` paths in `addCommit`). HEAD-label pitfalls after restore were fixed in
  git-canvas-a95rx; keep HEAD label movement correct in every intermediate state.
- Pull rebase: `RemoteOps.pull(rebase = true)`.
