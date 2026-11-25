# Protocol

Chronological log of everything that happens in this project.
Newest entries on top.

---

## 2026-11-25 08:26 -- Task verified and completed: git-canvas-r3dq7 - Render remote-tracking labels in dark red instead of light grey, so they are readable and match the common red convention for remote branches

**Type:** Work / Task completion
**Task:** git-canvas-r3dq7 - Render remote-tracking labels in dark red instead of light grey, so they are readable and match the common red convention for remote branches
**Summary:** render remote-tracking labels in dark red
**Duration:** 6m
**Verification:** PASS (iteration 1)
**Files changed:** 5
**Tests added:** 0
**ADRs written:** 0005 (amended)

---

## 2026-11-25 08:20 -- Batch started: [git-canvas-nymhr, git-canvas-r3dq7, git-canvas-q38zx]

**Type:** Work / Batch start
**Tasks:** git-canvas-nymhr - Rebase and pull (rebase) stop mid-way on a conflict until Continue or Abort, git-canvas-r3dq7 - Render remote-tracking labels in dark red instead of light grey, so they are readable and match the common red convention for remote branches, git-canvas-q38zx - Raise the small info texts in the control panel (Bulma help / is-size-7) to the standard body font size
**Parallel:** yes (3 workers — kngx5 and xt4ch held to next wave: kngx5 overlaps nymhr in RepoRegion/App.kt, xt4ch overlaps r3dq7 in CommitLabel.kt)

---

## 2026-11-25 08:20 -- Modeling / Promoted: git-canvas-nymhr - Rebase and pull (rebase) stop mid-way on a conflict until Continue or Abort

**Type:** Modeling / Promote
**BC:** git-canvas
**From → To:** backlog → todo

---

## 2026-11-25 08:20 -- Task verified and completed: git-canvas-c35ww - Merge and pull (merge) stop on a conflict until Continue or Abort

**Type:** Work / Task completion
**Task:** git-canvas-c35ww - Merge and pull (merge) stop on a conflict until Continue or Abort
**Summary:** merge and pull (merge) stop on a conflict until Continue or Abort
**Duration:** 10m
**Verification:** PASS (iteration 2)
**Files changed:** 10
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-25 10:00 -- Modeling / Refined: git-canvas-nymhr - Rebase and pull (rebase) stop mid-way on a conflict until Continue or Abort

**Type:** Modeling / Refine
**BC:** git-canvas
**Status after:** todo
**Summary:** Kept as one task. Abort restores refs and HEAD attachment only (layout shift may remain, lost copies keep positions). Added: detached HEAD counts as a reachability root while a rebase is paused, so replayed copies are not greyed out as lost.

---

## 2026-11-25 08:17 -- Verification failed: git-canvas-c35ww - Merge and pull (merge) stop on a conflict until Continue or Abort

**Type:** Work / Verification failure
**Task:** git-canvas-c35ww - Merge and pull (merge) stop on a conflict until Continue or Abort
**Iteration:** 1 of 3
**Reasons:** double-click checkout on branch and tag labels bypasses the pending guard (five handlers in GitGraph.kt, including the restore path), so HEAD can move while a merge is pending and Continue would build the wrong merge commit
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-11-25 09:45 -- Modeling / Captured: git-canvas-q38zx - Raise the small info texts in the control panel to the standard font size

**Type:** Modeling / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** All control-panel texts using Bulma `help` or `is-size-7` (status lines, hints, error and result messages like "Fast-forward") move to the standard body font size; colour modifiers stay intact.

---

## 2026-11-25 09:30 -- Modeling / Captured: git-canvas-kngx5 - Double the font size of repo header labels and the origin connector label

**Type:** Modeling / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** Repo name (bold), `origin -> <name>` badge, REMOTE pill and the bold `origin` connector label get twice the font size for readability; header layout offsets and connector label offset are adjusted accordingly.

---

## 2026-11-25 08:10 -- Batch started: [git-canvas-c35ww]

**Type:** Work / Batch start
**Tasks:** git-canvas-c35ww - Merge and pull (merge) stop on a conflict until Continue or Abort
**Parallel:** no (1 worker — nymhr chained behind it)

---

## 2026-11-25 08:10 -- Modeling / Promoted: git-canvas-c35ww - Merge and pull (merge) stop on a conflict until Continue or Abort

**Type:** Modeling / Promote
**BC:** git-canvas
**From → To:** backlog → todo

---

## 2026-11-25 08:10 -- Task verified and completed: git-canvas-mrfrh - Conflict marker on commits (set, render, share, transfer)

**Type:** Work / Task completion
**Task:** git-canvas-mrfrh - Conflict marker on commits (set, render, share, transfer)
**Summary:** conflict marker on commits
**Duration:** 8m
**Verification:** PASS (iteration 2)
**Files changed:** 9
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-25 09:15 -- Modeling / Refined: git-canvas-xt4ch - Draw label connectors as thin blue Bezier-curved arrows pointing toward the commit

**Type:** Modeling / Refine
**BC:** git-canvas
**Status after:** todo
**Summary:** Label connectors are drawn as cubic Bezier curves entering commit/label horizontally (S-curve for vertically offset labels, straight otherwise); horizontal label gap may be widened. Added machine-checkable and human-eye criteria.

---

## 2026-11-25 08:06 -- Verification failed: git-canvas-mrfrh - Conflict marker on commits (set, render, share, transfer)

**Type:** Work / Verification failure
**Task:** git-canvas-mrfrh - Conflict marker on commits (set, render, share, transfer)
**Iteration:** 1 of 3
**Reasons:** toggling the marker rerenders the child chain and wipes their lost-in-reflog state (lost commits reappear, also in snapshots); toggling a hidden lost commit makes it visible
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-11-25 09:30 -- Modeling / Captured: git-canvas-r3dq7 - Render remote-tracking labels in dark red instead of light grey

**Type:** Modeling / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** Remote-tracking labels (origin/<name>) use light grey text that is hard to read. Text and dashed border switch to dark red #B71C1C, matching the common red convention for remote branches; the pill stays unfilled and dashed.

---

## 2026-11-25 09:00 -- Modeling / Captured: git-canvas-xt4ch - Draw label connectors as thin blue arrows pointing toward the commit

**Type:** Modeling / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** Label connectors (branch, tag, HEAD, remote-tracking; label->commit and label->label) currently look identical to commit edges. They become thin blue arrows pointing toward the commit via a new Konva Arrow binding; commit edges stay black and 3px.

---

## 2026-11-25 08:02 -- Batch started: [git-canvas-mrfrh]

**Type:** Work / Batch start
**Tasks:** git-canvas-mrfrh - Conflict marker on commits (set, render, share, transfer)
**Parallel:** no (1 worker — c35ww and nymhr chained behind it)

---

## 2026-11-25 08:02 -- Modeling / Promoted: git-canvas-mrfrh - Conflict marker on commits (set, render, share, transfer)

**Type:** Modeling / Promote
**BC:** git-canvas
**From → To:** backlog → todo

---

## 2026-11-25 08:02 -- Modeling / Refined: git-canvas-mrfrh - Conflict marker on commits (set, render, share, transfer)

**Type:** Modeling / Refine
**BC:** git-canvas
**Status after:** todo
**Summary:** Builder decided on a lean conflict demonstration: show what happens in the graph during a conflict, not how a concrete conflict is resolved. Commits get a "conflicting change" marker; a merge/pull or rebase whose two sides both contain a marked commit since the merge base stops until Continue or Abort; rebase stops mid-way on a detached HEAD.
**Split into:** git-canvas-mrfrh (marker), git-canvas-c35ww (merge/pull pause), git-canvas-nymhr (rebase/pull-rebase pause)
**ADRs written:** 0006

---

## 2026-11-24 16:15 -- Work session ended

**Type:** Work / Session end
**Duration:** from 16:08 (first batch start) to 16:15, about 8m
**Completed:** 3 (first-try PASS: 3, re-dispatched: 0, skipped: 0)
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Dispatches:** nywqs: 1, wcbec: 1, vj6gr: 1
**Commits:** 6
**Vision-conformance:** none — batch aligns with vision (remote/clone relationship readable at a glance; manual consistent)
**Batch mix:** 67% product-facing / 33% harness / 0% bookkeeping (3 tasks)
**Carry-over:** .agentheim/.dashboard/: left behind (owner: Agentheim dashboard, runtime state of the running /dashboard server)

---

## 2026-11-24 16:15 -- Task verified and completed: git-canvas-vj6gr - Sync the user manual with the current app

**Type:** Work / Task completion
**Task:** git-canvas-vj6gr - Sync the user manual with the current app
**Summary:** sync the user manual with the current app
**Duration:** 2m
**Verification:** PASS (iteration 1)
**Files changed:** 1
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-24 16:13 -- Batch started: [git-canvas-vj6gr]

**Type:** Work / Batch start
**Tasks:** git-canvas-vj6gr - Sync the user manual with the current app
**Parallel:** no (1 worker — only ready task; unblocked by nywqs and wcbec)

---

## 2026-11-24 16:13 -- Task verified and completed: git-canvas-wcbec - Re-layout repo regions when they grow into each other

**Type:** Work / Task completion
**Task:** git-canvas-wcbec - Re-layout repo regions when they grow into each other
**Summary:** re-layout repo regions when they grow into each other
**Duration:** 5m
**Verification:** PASS (iteration 1)
**Files changed:** 6
**Tests added:** 0
**ADRs written:** 0001 (amended)

---

## 2026-11-24 16:11 -- Task verified and completed: git-canvas-nywqs - Grey out the remote repo's HEAD label to hint at a bare repository

**Type:** Work / Task completion
**Task:** git-canvas-nywqs - Grey out the remote repo's HEAD label to hint at a bare repository
**Summary:** grey out the remote repo's HEAD label
**Duration:** 3m
**Verification:** PASS (iteration 1)
**Files changed:** 5
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-24 09:25 -- Modeling / Captured: git-canvas-vj6gr - Sync the user manual with the current app

**Type:** Modeling / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** Whole-manual audit after the remote features: fixes the stale five-tabs intro, the per-graph commit-id sentence and the Share paragraph (positions), and checks every control is documented. Depends on wcbec and nywqs (which document themselves) to avoid concurrent manual edits. Also added a manual criterion to backlog task git-canvas-mrfrh.

---

## 2026-11-24 16:08 -- Batch started: [git-canvas-nywqs, git-canvas-wcbec]

**Type:** Work / Batch start
**Tasks:** git-canvas-nywqs - Grey out the remote repo's HEAD label to hint at a bare repository, git-canvas-wcbec - Re-layout repo regions when they grow into each other
**Parallel:** yes (2 workers — both touch App.kt, git-canvas README and manual; merge sequentially)

---

## 2026-11-24 16:05 -- Modeling / Promoted: git-canvas-nywqs - Grey out the remote repo's HEAD label to hint at a bare repository

**Type:** Modeling / Promote
**BC:** git-canvas
**From → To:** backlog → todo

---

## 2026-11-24 16:05 -- Modeling / Refined: git-canvas-nywqs - Grey out the remote repo's HEAD label to hint at a bare repository

**Type:** Modeling / Refine
**BC:** git-canvas
**Status after:** todo
**Summary:** Builder decided visual only: the remote repo's HEAD label is greyed out and semi-transparent, and all operations stay available on the remote. Blocking or warning on operations, a bare toggle, and hiding the HEAD were rejected. Concrete criteria cover the refresh path (restore, triangle rebuild, clone chain), HEAD movement, active/inactive and docs.

---

## 2026-11-24 16:05 -- Modeling / Promoted: git-canvas-wcbec - Re-layout repo regions when they grow into each other

**Type:** Modeling / Promote
**BC:** git-canvas
**From → To:** backlog → todo

---

## 2026-11-24 09:10 -- Modeling / Refined: git-canvas-wcbec - Re-layout repo regions when they grow into each other

**Type:** Modeling / Refine
**BC:** git-canvas
**Status after:** todo
**Summary:** Decided automatic re-layout after every bounds change: the lower/right region of a colliding pair is pushed down/right (cascading, never back), instant jump via the dxt9c snapshot-rebuild, clearances as named constants (vertical = cloneVerticalGap, new horizontal constant), also resolved after scenario restore. Concrete acceptance criteria written; resolution must be deferred out of onRegionBoundsChanged to avoid re-entrancy.

---

## 2026-11-24 08:49 -- Work session ended

**Type:** Work / Session end
**Duration:** from 08:37 (first batch start) to 08:49, about 13m
**Completed:** 2 (first-try PASS: 1, re-dispatched: 1, skipped: 0)
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Dispatches:** dxt9c: 2, f2ced: 1
**Commits:** 5
**Vision-conformance:** none — batch aligns with vision (makes push/pull/clone between one remote and two clones visually obvious)
**Batch mix:** 100% product-facing / 0% harness / 0% bookkeeping (2 tasks)
**Carry-over:** none — working tree clean

---

## 2026-11-24 08:49 -- Task verified and completed: git-canvas-f2ced - Highlight the remote repo and connect clones to their origin

**Type:** Work / Task completion
**Task:** git-canvas-f2ced - Highlight the remote repo and connect clones to their origin
**Summary:** highlight the remote repo and connect clones to their origin
**Duration:** 4m
**Verification:** PASS (iteration 1)
**Files changed:** 6
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-24 08:45 -- Batch started: [git-canvas-f2ced]

**Type:** Work / Batch start
**Tasks:** git-canvas-f2ced - Highlight the remote repo and connect clones to their origin
**Parallel:** no (1 worker — only ready task)

---

## 2026-11-24 08:45 -- Task verified and completed: git-canvas-dxt9c - Place clones below their origin in a triangle layout

**Type:** Work / Task completion
**Task:** git-canvas-dxt9c - Place clones below their origin in a triangle layout
**Summary:** place clones below their origin in a triangle layout
**Duration:** 8m
**Verification:** PASS (iteration 2)
**Files changed:** 6
**Tests added:** 0
**ADRs written:** 0001 (amended)

---

## 2026-11-24 08:40 -- Verification failed: git-canvas-dxt9c - Place clones below their origin in a triangle layout

**Type:** Work / Verification failure
**Task:** git-canvas-dxt9c - Place clones below their origin in a triangle layout
**Iteration:** 1 of 3
**Reasons:** clone y offset uses the origin's initial region height, so the gap shrinks below 150 px (and regions overlap) once the origin has grown; same for the triangle level
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-11-24 08:37 -- Batch started: [git-canvas-dxt9c]

**Type:** Work / Batch start
**Tasks:** git-canvas-dxt9c - Place clones below their origin in a triangle layout
**Parallel:** no (1 worker — only ready task; git-canvas-f2ced blocked on it)

---

## 2026-11-24 08:34 -- Modeling / Captured: git-canvas-dxt9c, git-canvas-f2ced, git-canvas-wcbec, git-canvas-nywqs - Clone placement below origin and remote highlighting

**Type:** Modeling / Capture
**BC:** git-canvas
**Filed to:** todo (dxt9c, f2ced), backlog (wcbec, nywqs)
**Summary:** Clones are placed below their origin (triangle for two clones, first clone shifts left; fixed vertical gap; Share URL keeps y offsets) and the remote repo gets dashed/tinted region, REMOTE badge, origin header on clones and a connection line. Growth-triggered re-layout and bare-repo semantics captured as backlog follow-ups.

---

## 2026-11-24 08:31 -- Work session ended

**Type:** Work / Session end
**Duration:** single-task session (git-canvas-a95rx)
**Completed:** 1 (first-try PASS: 1, re-dispatched: 0, skipped: 0)
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Dispatches:** a95rx: 1
**Commits:** 2
**Vision-conformance:** none — batch aligns with vision
**Batch mix:** 100% product-facing / 0% harness / 0% bookkeeping (1 task)
**Carry-over:** none — working tree clean

---

## 2026-11-24 08:30 -- Task verified and completed: git-canvas-a95rx - HEAD label stays on the previous commit after restoring a snapshot or cloning

**Type:** Work / Task completion
**Task:** git-canvas-a95rx - HEAD label stays on the previous commit after restoring a snapshot or cloning
**Summary:** HEAD label follows commits after snapshot restore and clone
**Duration:** 18m
**Verification:** PASS (iteration 1)
**Files changed:** 1
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-24 08:26 -- Batch started: [git-canvas-a95rx]

**Type:** Work / Batch start
**Tasks:** git-canvas-a95rx - HEAD label stays on the previous commit after restoring a snapshot or cloning
**Parallel:** no (1 worker — only ready task)

---

## 2026-11-24 08:23 -- Modeling / Captured: git-canvas-a95rx - HEAD label stays on the previous commit after restoring a snapshot or cloning

**Type:** Modeling / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** Pre-existing rendering bug found during remote-ops smoke tests: after restoreFromSnapshot (Share URL load, clones) the first new commit leaves the HEAD label behind. Likely cause noted (Head registered in the commit's label list during restore).

---

## 2026-11-23 21:04 -- Work session ended

**Type:** Work / Session end
**Duration:** from 20:27 (first batch start) to 21:04
**Completed:** 9 (first-try PASS: 6, re-dispatched: 3, skipped: 0)
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Dispatches:** j2m8z: 1, b5f0r: 1, wv6va: 1, r6szj: 1, w3ndg: 2, rx1y3: 1, sz7p8: 1, ktja3: 2, bdecz: 2
**Commits:** 18
**Vision-conformance:** none — batch aligns with vision (delivers "push/pull/clone between one remote and two local clones is visually obvious on screen")
**Batch mix:** 100% product-facing / 0% harness / 0% bookkeeping (9 tasks)
**Carry-over:** none — working tree clean

---

## 2026-11-23 21:04 -- Task verified and completed: git-canvas-bdecz - Remote operations documentation and doc sync

**Type:** Work / Task completion
**Task:** git-canvas-bdecz - Remote operations documentation and doc sync
**Summary:** document remote operations in manual, READMEs and agent guidance
**Duration:** 12m
**Verification:** PASS (iteration 2)
**Files changed:** 5
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-23 21:02 -- Verification failed: git-canvas-bdecz - Remote operations documentation and doc sync

**Type:** Work / Verification failure
**Task:** git-canvas-bdecz - Remote operations documentation and doc sync
**Iteration:** 1 of 3
**Reasons:** manual "Commit Ids" section still describes per-branch counters, contradicting the shared CommitIdAllocator
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-11-23 21:00 -- Batch started: [git-canvas-bdecz]

**Type:** Work / Batch start
**Tasks:** git-canvas-bdecz - Remote operations documentation and doc sync
**Parallel:** no (1 worker — last task in the remote-ops chain)

---

## 2026-11-23 21:00 -- Task verified and completed: git-canvas-ktja3 - Pull with rebase option

**Type:** Work / Task completion
**Task:** git-canvas-ktja3 - Pull with rebase option
**Summary:** pull with rebase option
**Duration:** 14m
**Verification:** PASS (iteration 2)
**Files changed:** 4
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-23 20:59 -- Verification failed: git-canvas-ktja3 - Pull with rebase option

**Type:** Work / Verification failure
**Task:** git-canvas-ktja3 - Pull with rebase option
**Iteration:** 1 of 3
**Reasons:** git-canvas README Pull entry still says the rebase option is disabled (BC README out of sync)
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-11-23 20:57 -- Batch started: [git-canvas-ktja3]

**Type:** Work / Batch start
**Tasks:** git-canvas-ktja3 - Pull with rebase option
**Parallel:** no (1 worker — only ready task in the sequential remote-ops chain)

---

## 2026-11-23 20:57 -- Task verified and completed: git-canvas-sz7p8 - Pull as fetch plus merge (fast-forward or merge commit)

**Type:** Work / Task completion
**Task:** git-canvas-sz7p8 - Pull as fetch plus merge (fast-forward or merge commit)
**Summary:** pull as fetch plus merge (fast-forward or merge commit)
**Duration:** 9m
**Verification:** PASS (iteration 1)
**Files changed:** 4
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-23 20:54 -- Batch started: [git-canvas-sz7p8]

**Type:** Work / Batch start
**Tasks:** git-canvas-sz7p8 - Pull as fetch plus merge (fast-forward or merge commit)
**Parallel:** no (1 worker — only ready task in the sequential remote-ops chain)

---

## 2026-11-23 20:54 -- Task verified and completed: git-canvas-rx1y3 - Fetch updates tracking labels and brings in missing commits

**Type:** Work / Task completion
**Task:** git-canvas-rx1y3 - Fetch updates tracking labels and brings in missing commits
**Summary:** fetch updates origin/* labels and brings in missing commits
**Duration:** 9m
**Verification:** PASS (iteration 1)
**Files changed:** 4
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-23 20:51 -- Batch started: [git-canvas-rx1y3]

**Type:** Work / Batch start
**Tasks:** git-canvas-rx1y3 - Fetch updates tracking labels and brings in missing commits
**Parallel:** no (1 worker — only ready task; pull waits on fetch)

---

## 2026-11-23 20:50 -- Task verified and completed: git-canvas-w3ndg - Push with fast-forward and visible rejection

**Type:** Work / Task completion
**Task:** git-canvas-w3ndg - Push with fast-forward and visible rejection
**Summary:** push with fast-forward, visible non-fast-forward rejection and unavailable-remote handling
**Duration:** 25m
**Verification:** PASS (iteration 2)
**Files changed:** 5
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-23 20:48 -- Verification failed: git-canvas-w3ndg - Push with fast-forward and visible rejection

**Type:** Work / Verification failure
**Task:** git-canvas-w3ndg - Push with fast-forward and visible rejection
**Iteration:** 1 of 3
**Reasons:** pushing to a cleared remote shows "rejected: non-fast-forward" instead of "remote not available" (cleared repo keeps its repoId and gets a fresh root commit)
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-11-23 20:43 -- Batch started: [git-canvas-w3ndg]

**Type:** Work / Batch start
**Tasks:** git-canvas-w3ndg - Push with fast-forward and visible rejection
**Parallel:** no (1 worker — git-canvas-rx1y3 (fetch) is also ready but held to the next wave: both need the same new GitGraph commit-receiving primitive, so they run sequentially to avoid a merge conflict)

---

## 2026-11-23 20:43 -- Task verified and completed: git-canvas-r6szj - Remote tab in control panel with remote status

**Type:** Work / Task completion
**Task:** git-canvas-r6szj - Remote tab in control panel with remote status
**Summary:** add Remote tab with remote, upstream and ahead/behind status
**Duration:** 12m
**Verification:** PASS (iteration 1)
**Files changed:** 3
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-23 20:40 -- Batch started: [git-canvas-r6szj]

**Type:** Work / Batch start
**Tasks:** git-canvas-r6szj - Remote tab in control panel with remote status
**Parallel:** no (1 worker — only ready task in the sequential remote-ops chain)

---

## 2026-11-23 20:39 -- Task verified and completed: git-canvas-wv6va - Clone repository with origin remote

**Type:** Work / Task completion
**Task:** git-canvas-wv6va - Clone repository with origin remote
**Summary:** clone the active repo into a new region with origin remote and tracking labels
**Duration:** 10m
**Verification:** PASS (iteration 1)
**Files changed:** 6
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-23 20:36 -- Batch started: [git-canvas-wv6va]

**Type:** Work / Batch start
**Tasks:** git-canvas-wv6va - Clone repository with origin remote
**Parallel:** no (1 worker — only ready task in the sequential remote-ops chain)

---

## 2026-11-23 20:35 -- Task verified and completed: git-canvas-b5f0r - Remote-tracking label model and rendering

**Type:** Work / Task completion
**Task:** git-canvas-b5f0r - Remote-tracking label model and rendering
**Summary:** add remote-tracking label kind (RemoteBranch) with dashed rendering and snapshot support
**Duration:** 8m
**Verification:** PASS (iteration 1)
**Files changed:** 7
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-23 20:32 -- Batch started: [git-canvas-b5f0r]

**Type:** Work / Batch start
**Tasks:** git-canvas-b5f0r - Remote-tracking label model and rendering
**Parallel:** no (1 worker — only ready task in the sequential remote-ops chain)

---

## 2026-11-23 20:32 -- Task verified and completed: git-canvas-j2m8z - Shared commit ID allocator across repos

**Type:** Work / Task completion
**Task:** git-canvas-j2m8z - Shared commit ID allocator across repos
**Summary:** share one commit ID allocator across all repos; snapshot v2 with nextCommitId
**Duration:** 14m
**Verification:** PASS (iteration 1)
**Files changed:** 6
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-23 20:27 -- Batch started: [git-canvas-j2m8z]

**Type:** Work / Batch start
**Tasks:** git-canvas-j2m8z - Shared commit ID allocator across repos
**Parallel:** no (1 worker — only ready task; remaining 8 remote-ops tasks blocked on the dependency chain)

---

## 2026-11-23 20:26 -- Modeling / Captured: git-canvas remote operations (9 tasks + 1 backlog)

**Type:** Modeling / Capture
**BC:** git-canvas
**Filed to:** todo (9), backlog (1)
**Summary:** Decomposed remote operations into a dependency chain: shared commit ID allocator (git-canvas-j2m8z) -> remote-tracking labels (git-canvas-b5f0r) -> clone (git-canvas-wv6va) -> Remote tab (git-canvas-r6szj) -> push (git-canvas-w3ndg) and fetch (git-canvas-rx1y3) -> pull merge (git-canvas-sz7p8) -> pull rebase (git-canvas-ktja3) -> docs (git-canvas-bdecz). Conflict demonstration (git-canvas-mrfrh) captured to backlog, under-refined. Builder decisions: clone creates a new repo with origin/* labels; globally unique commit IDs; fetch + pull with merge or rebase; non-fast-forward push rejected like Git, no force push.
**ADRs written:** 0004, 0005

---

## 2026-05-27 00:00 -- Work session ended

**Type:** Work / Session end
**Completed:** 1 (first-try PASS: 1, re-dispatched: 0, skipped: 0)
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 1 (3fcdc43)

---

## 2026-05-27 00:00 -- Task verified and completed: git-canvas-004 - User-configurable initial repository name

**Type:** Work / Task completion
**Task:** git-canvas-004 - User-configurable initial repository name
**Summary:** Added a repository name editor for the active repository and changed the default initial repository name from `origin` to `my-repo`.
**Verification:** PASS (iteration 1)
**Commit:** 3fcdc43
**Files changed:** 4
**Tests added:** 0
**ADRs written:** none

---

## 2026-05-27 00:00 -- Batch started: [git-canvas-004]

**Type:** Work / Batch start
**Tasks:** git-canvas-004 - User-configurable initial repository name
**Parallel:** no (1 worker)

---

## 2026-05-27 00:00 -- Model / Captured: git-canvas-004 - User-configurable initial repository name

**Type:** Model / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** The initial repository is currently hard-coded as `origin`. Make the initial repository name editable by the trainer and change the default starting name to `my-repo`.

---

## 2026-05-25 -- Work session ended

**Type:** Work / Session end
**Completed:** 1 (first-try PASS: 1 [infrastructure-007])
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 1 (b65046f)

---

## 2026-05-25 -- Task verified and completed: infrastructure-007 - Update README — multi-repo features, Konva.js migration, agentic development section

**Type:** Work / Task completion
**Task:** infrastructure-007 - Update README — multi-repo features, Konva.js migration, agentic development section
**Summary:** Updated README.md to reflect the current state of the app: added multi-repo features to the feature list, replaced the outdated "future push/pull" sentence, corrected build commands to sh gradlew, swapped fabric.js and kotlin-react for Konva.js in the tech stack, and added an Agentic Development section linking to Claude Code and the agentheim plugin.
**Verification:** PASS (iteration 1)
**Commit:** b65046f
**Files changed:** 1
**Tests added:** 0
**ADRs written:** none

---

## 2026-05-25 -- Batch started: [infrastructure-007]

**Type:** Work / Batch start
**Tasks:** infrastructure-007 - Update README — multi-repo features, Konva.js migration, agentic development section
**Parallel:** no (1 worker)

---

## 2026-05-25 -- Model / Captured: infrastructure-007 - Update README — multi-repo features, Konva.js migration, agentic development section

**Type:** Model / Capture
**BC:** infrastructure
**Filed to:** todo
**Summary:** README still describes the original single-repo Fabric.js app. Update feature list (multi-repo, Add Repository, Share button), replace fabric.js with Konva.js in tech stack, fix build command (sh gradlew), add Agentic Development section with links to Claude Code and the agentheim plugin (https://github.com/heimeshoff/Agentheim).

---

## 2026-05-25 -- Work session ended

**Type:** Work / Session end
**Completed:** 1 (first-try PASS: 1 [git-canvas-003])
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 1 (011bd02)

---

## 2026-05-25 -- Task verified and completed: git-canvas-003 - RepoRegion auto-resize — frame must grow with graph content

**Type:** Work / Task completion
**Task:** git-canvas-003 - RepoRegion auto-resize — frame must grow with graph content
**Summary:** RepoRegion now dynamically grows its bounding rectangle after every graph-mutating operation by iterating all Commit positions; containsPoint() uses the updated bounds so click-to-focus works on commits rendered beyond the original static frame.
**Verification:** PASS (iteration 1)
**Commit:** 011bd02
**Files changed:** 2
**Tests added:** 0
**ADRs written:** none

---

## 2026-05-25 -- Batch started: [git-canvas-003]

**Type:** Work / Batch start
**Tasks:** git-canvas-003 - RepoRegion auto-resize — frame must grow with graph content
**Parallel:** no (1 worker)

---

## 2026-05-25 -- Model / Captured: git-canvas-003 - RepoRegion auto-resize — frame must grow with graph content

**Type:** Model / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** RepoRegion border is sized once at init and never updated; as commits and branches are added the graph overflows the frame and commits outside the original bounds become unclickable (containsPoint miss). Fix: recalculate and redraw the region bounds after every graph-mutating operation.

---

## 2026-05-25 -- Work session ended

**Type:** Work / Session end
**Completed:** 1 (first-try PASS: 1 [git-canvas-002])
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 1 (fff62aa)

---

## 2026-05-25 -- Task verified and completed: git-canvas-002 - Initial 'main' commit for all new graphs — fix detached HEAD regression

**Type:** Work / Task completion
**Task:** git-canvas-002 - Initial 'main' commit for all new graphs — fix detached HEAD regression
**Summary:** Fixed detached HEAD regression by calling initGraph() instead of renderEmptyRegion() when adding a new repository; removed dead renderEmptyRegion() method; renamed default branch from 'master' to 'main'.
**Verification:** PASS (iteration 1)
**Commit:** fff62aa
**Files changed:** 2
**Tests added:** 0
**ADRs written:** none

---

## 2026-05-25 -- Batch started: [git-canvas-002]

**Type:** Work / Batch start
**Tasks:** git-canvas-002 - Initial 'main' commit for all new graphs — fix detached HEAD regression
**Parallel:** no (1 worker)

---

## 2026-05-25 -- Model / Captured: git-canvas-002 - Initial 'main' commit for all new graphs — fix detached HEAD regression

**Type:** Model / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** Regression fix from infrastructure-006: dynamically added graphs start without an initial commit, causing detached HEAD on the first "Add Commit". Fix calls initGraph() instead of renderEmptyRegion() for user-triggered "Add Repository". Bundled: rename the default branch from 'master' to 'main'. URL-restored graphs are unaffected.

---

## 2026-05-25 -- Work session ended

**Type:** Work / Session end
**Completed:** 1 (first-try PASS: 1 [infrastructure-006])
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 1 (044454f)

---

## 2026-05-25 -- Task verified and completed: infrastructure-006 - Dynamic graph management — start with one, add up to two more

**Type:** Work / Task completion
**Task:** infrastructure-006 - Dynamic graph management — start with one, add up to two more
**Summary:** Replaced hardcoded two-graph initialisation with a dynamic graph list starting from a single "origin" graph; added an "Add Repository" button with name-input dialog that creates new empty graphs (up to three total), and updated click-to-focus, share, and URL-restore to work across all dynamically added graphs.
**Verification:** PASS (iteration 1)
**Commit:** 044454f
**Files changed:** 3
**Tests added:** 0
**ADRs written:** none

---

## 2026-05-23 -- Batch started: [infrastructure-006]

**Type:** Work / Batch start
**Tasks:** infrastructure-006 - Dynamic graph management — start with one, add up to two more
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Model / Captured: infrastructure-006 - Dynamic graph management — start with one, add up to two more

**Type:** Model / Capture
**BC:** infrastructure
**Filed to:** todo
**Summary:** Replaces hardcoded two-graph initialisation with a single "origin" graph on startup; adds an "Add Repository" button that prompts for a name (like branch creation) and places the new graph at a fixed horizontal offset. Maximum three graphs total; no removal in this task.

---

## 2026-05-23 -- Work session ended

**Type:** Work / Session end
**Completed:** 1 (first-try PASS: 0, re-dispatched: 1 [scenario-002])
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 1 (20fa14e)

---

## 2026-05-23 -- Task verified and completed: scenario-002 - Share button — encode and restore graph state via URL fragment

**Type:** Work / Task completion
**Task:** scenario-002 - Share button — encode and restore graph state via URL fragment
**Summary:** Implemented end-to-end URL-based scenario sharing: Share button serializes multi-repo graph state to JSON, compresses with gzip, base64url-encodes, writes to URL fragment and copies to clipboard; on startup detects #state= fragment and asynchronously restores both graphs.
**Verification:** PASS (iteration 2 — iteration 1 fixed two bugs: version field jsIntOrNull coercion, return pump() in decompressFromBase64Url)
**Commit:** 20fa14e
**Files changed:** 5
**Tests added:** 0
**ADRs written:** none

---

## 2026-05-23 -- Verification failed: scenario-002 - Share button — encode and restore graph state via URL fragment

**Type:** Work / Verification failure
**Task:** scenario-002 - Share button — encode and restore graph state via URL fragment
**Iteration:** 1 of 3
**Reasons:** ScenarioSerializer.decode always returns null — version field uses as? Int instead of jsIntOrNull (JSON.parse returns Double not Int); decompressFromBase64Url missing return pump() in recursive loop causing Promise to never settle on mid-stream failure
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-05-23 -- Batch started: [scenario-002]

**Type:** Work / Batch start
**Tasks:** scenario-002 - Share button — encode and restore graph state via URL fragment
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Model / Captured: scenario-002 - Share button — encode and restore graph state via URL fragment

**Type:** Model / Capture
**BC:** scenario
**Filed to:** todo
**Summary:** Implements the EncodeCurrentStateAsUrl + LoadScenarioFromUrl commands from ADR-0002: a Share button serializes multi-repo graph state to JSON, compresses with gzip, base64url-encodes, and writes to the URL fragment; on startup, the app detects and restores from that fragment instead of rendering the default blank canvas.

---

## 2026-05-23 -- Work session ended

**Type:** Work / Session end
**Completed:** 1 (first-try PASS: 1 [infrastructure-005])
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 1 (4a4cc52)

---

## 2026-05-23 -- Task verified and completed: infrastructure-005 - Canvas drag-to-pan broken after Konva.js migration

**Type:** Work / Task completion
**Task:** infrastructure-005 - Canvas drag-to-pan broken after Konva.js migration
**Summary:** Restored drag-to-pan by replacing Konva synthetic stage events with native DOM events on the container; also added mouseleave guard and moved mouseup to window to handle release outside the canvas.
**Verification:** PASS (iteration 1)
**Commit:** 4a4cc52
**Files changed:** 1
**Tests added:** 0
**ADRs written:** none

---

## 2026-05-23 -- Batch started: [infrastructure-005]

**Type:** Work / Batch start
**Tasks:** infrastructure-005 - Canvas drag-to-pan broken after Konva.js migration
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Model / Captured: infrastructure-005 - Canvas drag-to-pan broken after Konva.js migration

**Type:** Model / Capture
**BC:** infrastructure
**Filed to:** todo
**Summary:** Regression from infrastructure-004 — canvas drag-to-pan stopped working after the Konva.js migration; Ctrl+scroll zoom is unaffected. Filed directly to todo as a concrete, observable regression with clear acceptance criteria.

---

## 2026-05-23 -- Work session ended

**Type:** Work / Session end
**Completed:** 2 (first-try PASS: 2 [infrastructure-003, infrastructure-004])
**Bounced:** 1 (infrastructure-003, env-bounce — Bash denied in first session; re-dispatched successfully)
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 2 (4959555, 09e7f74)

---

## 2026-05-23 -- Task verified and completed: infrastructure-004 - Konva.js integration: replace Fabric.js bindings and rewrite rendering

**Type:** Work / Task completion
**Task:** infrastructure-004 - Konva.js integration: replace Fabric.js bindings and rewrite rendering
**Summary:** Replaced Fabric.js v5 with Konva.js v10.3.0. Deleted fabricjs/ (3 files). Created konvajs/ package (8 files). Rewrote Renderable, all components/, graph/, and App.kt to use Layer. Pan/zoom via Stage transforms; click-to-focus via getPointerPosition(). Production webpack build passes.
**Verification:** PASS (iteration 1 — verifier false-positive overridden: flagged orchestrator-owned INDEX.md/protocol.md changes as worker violations; verifier confirmed build passes and all code correct)
**Commit:** 09e7f74
**Files changed:** 23
**ADRs written:** none

---

## 2026-05-23 -- Batch started: [infrastructure-004]

**Type:** Work / Batch start
**Tasks:** infrastructure-004 - Konva.js integration: replace Fabric.js bindings and rewrite rendering
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Task verified and completed: infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation

**Type:** Work / Task completion
**Task:** infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation
**Summary:** Dukat 0.5.8-rc.4 confirmed via npx; run against Konva v10.3.0 per-file .d.ts. All 7 scoped types produce structurally correct external classes; main degradation is GetSet mapped type → Any for shape property accessors. Recommendation: start from Dukat output with medium-effort mechanical cleanup (~15-20 props, write RectConfig, rename T$0 interfaces, prune internals).
**Verification:** PASS (iteration 1)
**Commit:** 4959555
**Files changed:** 1
**ADRs written:** none

---

## 2026-05-23 -- Batch started: [infrastructure-003]

**Type:** Work / Batch start
**Tasks:** infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Task bounced: infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation

**Type:** Work / Task bounced
**Task:** infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation
**Reason:** Bash tool was denied in this session. Spike requires shell access to install dukat, run npm install konva, and invoke dukat against the type definitions. Worker note added to task file.
**Moved to:** backlog

---

## 2026-05-23 -- Batch started: [infrastructure-003]

**Type:** Work / Batch start
**Tasks:** infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Model / Captured: infrastructure-003, infrastructure-004 - Konva.js migration tasks

**Type:** Model / Capture
**BC:** infrastructure
**Filed to:** todo
**Summary:** Two implementation tasks capturing the Konva.js migration work mandated by ADR-0003. infrastructure-003 (Dukat spike) must run first to assess binding generation quality; infrastructure-004 (full integration) depends on it and replaces all Fabric.js bindings and rendering code. Both tasks include prior art links to infrastructure-001 and infrastructure-002 and cross-reference ADRs 0001/0002/0003.

---

## 2026-05-23 -- Work session ended

**Type:** Work / Session end
**Completed:** 4 (first-try PASS: 3 [git-canvas-001, scenario-001, infrastructure-001], skipped: 1 [infrastructure-002])
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 0 (deferred — user chose to skip commits this session)

---

## 2026-05-23 -- Task verified and completed: infrastructure-001 - Walking skeleton: multi-repo proof-of-concept

**Type:** Work / Task completion
**Task:** infrastructure-001 - Walking skeleton: multi-repo proof-of-concept
**Summary:** Two GitGraph instances ("origin" and "alice") coexist on shared Fabric.js canvas at fixed positions 800px apart. Click-to-focus activation with colored border indicator. All control panel operations route to active repo. Production webpack build passes cleanly.
**Verification:** PASS (iteration 1)
**Files changed:** 6
**ADRs written:** none

---

## 2026-05-23 -- Batch started: [infrastructure-001]

**Type:** Work / Batch start
**Tasks:** infrastructure-001 - Walking skeleton: multi-repo proof-of-concept
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Task completed (verification skipped): infrastructure-002 - Migrate canvas foundation from Fabric.js to Konva.js

**Type:** Work / Task completion
**Task:** infrastructure-002 - Migrate canvas foundation from Fabric.js to Konva.js
**Summary:** ADR 0003 written confirming Konva.js migration. Fabric.js v5 rejected (abandoned, unpatched CVE); Fabric.js v7 not preferred (near-complete binding rewrite); Konva.js v10.x adopted. Dukat spike documented as environment-blocked.
**Verification:** SKIPPED — decision-only task, single ADR file
**Files changed:** 1
**ADRs written:** 0003-migrate-canvas-foundation-to-konvajs.md

---

## 2026-05-23 -- Task verified and completed: scenario-001 - URL encoding format for graph snapshots

**Type:** Work / Task completion
**Task:** scenario-001 - URL encoding format for graph snapshots
**Summary:** ADR 0002 written adopting JSON + gzip + base64url in URL fragment. BC README updated. Consequences section added after verifier flagged its absence; ADR numbering fixed after parallel-worker rename.
**Verification:** PASS (iteration 1, orchestrator-fixed numbering)
**Files changed:** 2
**ADRs written:** 0002-url-encoding-format-for-graph-snapshots.md

---

## 2026-05-23 -- Task verified and completed: git-canvas-001 - Multi-repo canvas layout strategy

**Type:** Work / Task completion
**Task:** git-canvas-001 - Multi-repo canvas layout strategy
**Summary:** ADR 0001 written adopting shared infinite canvas with click-to-focus and faded inactive repos. BC README updated.
**Verification:** PASS (iteration 1)
**Files changed:** 2
**ADRs written:** 0001-multi-repo-canvas-layout.md

---

## 2026-05-23 -- Batch started: [git-canvas-001, scenario-001, infrastructure-002]

**Type:** Work / Batch start
**Tasks:** git-canvas-001 - Multi-repo canvas layout strategy, scenario-001 - URL encoding format for graph snapshots, infrastructure-002 - Migrate canvas foundation from Fabric.js to Konva.js
**Parallel:** yes (3 workers)

---

## 2026-05-23 -- Research: Canvas library evaluation for multi-repo Kotlin/JS diagram app

**Type:** Research
**Requested by:** model
**Report:** knowledge/research/canvas-library-evaluation-2026-05-23.md
**Summary:**
- Fabric.js v5 is effectively abandoned; v7.4.0 (May 2026) is the current stable series, but migration from v5 is a near-complete rewrite due to TypeScript rewrite, renamed core types, and Group system overhaul.
- Konva.js (v10.3.0, April 2026) is the strongest alternative: first-class infinite canvas/pan-zoom, independent draggable groups with per-node events, 14.5k stars, bundled TypeScript definitions usable by Dukat.
- No production-grade Kotlin/JS bindings exist for Fabric.js, Konva.js, or PixiJS; Dukat can auto-generate stubs from bundled `.d.ts` files but manual cleanup is required, especially for inheritance-heavy APIs.

---

## 2026-05-23 -- Model / Refined: infrastructure-002 - Migrate canvas foundation from Fabric.js to Konva.js

**Type:** Model / Refine
**BC:** infrastructure
**Status after:** todo
**Summary:** Direction confirmed as Konva.js migration based on research findings. Fabric.js v5 abandoned (security CVE unpatched), v7 migration is a near-rewrite of Kotlin bindings regardless, Konva.js maps better to multi-repo canvas model and ships bundled TypeScript definitions. Task updated to: run Dukat spike against Konva's .d.ts, write ADR confirming migration. No production code in this task.

---

## 2026-05-23 -- Model / Refined: infrastructure-001 - Walking skeleton: multi-repo proof-of-concept

**Type:** Model / Refine
**BC:** infrastructure
**Status after:** todo
**Summary:** Updated acceptance criteria to match confirmed decisions: click-to-focus replaces radio buttons/dropdown; colored border replaces fade/desaturate for active repo indicator; fixed initial placement confirmed sufficient (drag-to-reposition deferred); explicit out-of-scope list added to keep the spike narrow.

---

## 2026-05-23 -- Model / Refined: scenario-001 - URL encoding format for graph snapshots

**Type:** Model / Refine
**BC:** scenario
**Status after:** todo
**Summary:** Confirmed Option A (JSON + gzip + base64url in URL fragment). Typical scenarios are small (few commits/branches per repo), so URL length is not a concern. URL always app-generated — no hand-editability needed. A control panel button serializes current state to the address bar. App detects fragment on startup and restores graph state. No length fallback required. Options B (custom DSL) and C (binary) rejected.

---

## 2026-05-23 -- Model / Refined: git-canvas-001 - Multi-repo canvas layout strategy

**Type:** Model / Refine
**BC:** git-canvas
**Status after:** todo
**Summary:** Confirmed Option C (shared infinite canvas) as the layout approach. Trainer positions repos freely by dragging; click to focus makes a repo active; active repo renders in full saturated colors, inactive repos are faded/desaturated. Pan/zoom is global. Repo labels (origin, alice, bob) are visible banners. Options A (fixed panels), B (tabs), and D (floating windows) explicitly rejected.

---

## 2026-05-23 -- Brainstorm: Git Whiteboard App vision

**Type:** Brainstorm
**Outcome:** vision created
**BCs identified:** git-canvas (core), scenario (supporting), infrastructure (generic)
**Summary:** Socratic session with the Git trainer. Established that the tool is used exclusively by trainers live during sessions, never prepared in advance. The primary missing feature is multi-repo visualization (1 remote + 2+ local clones) to enable push/pull/conflict demos. URL-encoded scenarios are the desired persistence model — no server. Git internals and realistic commit IDs are explicit non-goals.
**ADRs written:** none (decisions deferred to task queue)
**Foundation tasks emitted:** git-canvas-001 (multi-repo layout decision), scenario-001 (URL encoding format decision), infrastructure-001 (walking skeleton spike — depends on both decisions)

---
