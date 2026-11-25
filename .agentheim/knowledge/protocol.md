# Protocol

Chronological log of everything that happens in this project.
Newest entries on top.

---

## 2026-11-25 17:10 -- Capture / Captured: git-canvas-vbxva - Add force push, both plain --force and --force-with-lease

**Type:** Capture
**BC:** git-canvas
**Filed to:** backlog
**Summary:** Add force push, both plain --force and --force-with-lease.

---

## 2026-11-25 17:10 -- Capture / Captured: git-canvas-maf17 - Add git revert - a new commit that undoes an earlier one

**Type:** Capture
**BC:** git-canvas
**Filed to:** backlog
**Summary:** Add git revert - a new commit that undoes an earlier one.

---

## 2026-11-25 16:15 -- Batch started: [git-canvas-cdy8k]

**Type:** Work / Batch start
**Tasks:** git-canvas-cdy8k - Free branch names - replace first-letter uniqueness with Git ref-name rules and per-branch short codes (1-2 chars) for commit IDs that fit the commit circle
**Parallel:** no (1 worker)

---

## 2026-11-25 16:14 -- Task verified and completed: git-canvas-pjz9w - Check out remote-tracking branches like real Git - origin/<name> gives a detached HEAD, <name> without a local branch creates a local tracking branch (DWIM)

**Type:** Work / Task completion
**Task:** git-canvas-pjz9w - Check out remote-tracking branches like real Git - origin/<name> gives a detached HEAD, <name> without a local branch creates a local tracking branch (DWIM)
**Summary:** check out remote-tracking branches like real Git (detached HEAD on origin/<name>, DWIM local tracking branch)
**Duration:** 14m
**Verification:** PASS (iteration 2)
**Files changed:** 5
**Tests added:** 0
**ADRs written:** none (ADR 0005 amended)

---

## 2026-11-25 16:40 -- Modeling / Captured: git-canvas-cdy8k - Free branch names with Git ref-name rules and per-branch short codes

**Type:** Modeling / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** The first-letter uniqueness rule for branch names is replaced by Git's ref-name rules; commit IDs use a 1-2 char per-branch short code (auto-derived or entered) and the ID label shrinks to fit the commit circle. Also switches the DWIM checkout of git-canvas-pjz9w (in flight) to the new rules.
**ADRs written:** 0007

---

## 2026-11-25 16:03 -- Verification failed: git-canvas-pjz9w - Check out remote-tracking branches like real Git

**Type:** Work / Verification failure
**Task:** git-canvas-pjz9w - Check out remote-tracking branches like real Git
**Iteration:** 1 of 3
**Reasons:** build passes, but no browser smoke test was run, so the "no new console errors" criterion and all behavioural criteria lack runtime evidence (code reading looked correct)
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-11-25 16:00 -- Batch started: [git-canvas-pjz9w]

**Type:** Work / Batch start
**Tasks:** git-canvas-pjz9w - Check out remote-tracking branches like real Git - origin/<name> gives a detached HEAD, <name> without a local branch creates a local tracking branch (DWIM)
**Parallel:** no (1 worker)

---

## 2026-11-25 16:10 -- Modeling / Captured: git-canvas-pjz9w - Check out remote-tracking branches like real Git

**Type:** Modeling / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** Remote-tracking branches become checkout-able with real Git semantics: double-click or `origin/<name>` detaches HEAD; `<name>` without a local branch creates a local branch tracking `origin/<name>` (DWIM), offered in the checkout dropdown and free-text input. Uniqueness conflicts are refused with a message.

---

## 2026-11-25 15:46 -- Work session ended

**Type:** Work / Session end
**Duration:** from 08:02 (first batch start) to 15:46; wall time includes a ~7h interruption after a network failure (worker for git-canvas-kngx5 aborted before any change, re-dispatched)
**Completed:** 8 (first-try PASS: 2, re-dispatched: 6, skipped: 0)
**Bounced:** 0
**Failed:** 0 (one worker aborted by a network error, re-dispatched fresh; not counted as a task failure)
**Escalated after verification:** 0
**Dispatches:** mrfrh: 2, c35ww: 2, nymhr: 2, r3dq7: 1, q38zx: 1, xt4ch: 2, kngx5: 2 (+1 aborted), sxqsj: 2
**Commits:** 14 (5 batch starts, 8 task commits, 1 session end; plus 4 modeling commits for the mrfrh refinement and promotions)
**Vision-conformance:** none — batch aligns with vision (delivers "Conflict resolution scenarios can be demonstrated without leaving the tool"; readability tasks serve projector demos)
**Batch mix:** 100% product-facing / 0% harness / 0% bookkeeping (8 tasks)
**Carry-over:** .agentheim/.dashboard/: left behind (owner: Agentheim dashboard, runtime state of the running /dashboard server)

---

## 2026-11-25 15:45 -- Task verified and completed: git-canvas-sxqsj - Draw commit edges GitKraken-style - straight vertical runs in a lane with a short rounded bend where the edge changes lanes

**Type:** Work / Task completion
**Task:** git-canvas-sxqsj - Draw commit edges GitKraken-style - straight vertical runs in a lane with a short rounded bend where the edge changes lanes
**Summary:** draw commit edges GitKraken-style
**Duration:** 430m (wall time; includes a ~7h interruption after a network failure, actual work ~15m)
**Verification:** PASS (iteration 2)
**Files changed:** 4
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-25 15:44 -- Task verified and completed: git-canvas-kngx5 - Double the font size of repo header labels and the origin connector label; repo name and connector label bold

**Type:** Work / Task completion
**Task:** git-canvas-kngx5 - Double the font size of repo header labels and the origin connector label; repo name and connector label bold
**Summary:** double the font size of repo header labels and the origin connector label
**Duration:** 429m (wall time; includes a ~7h interruption after a network failure, actual work ~12m)
**Verification:** PASS (iteration 2)
**Files changed:** 3
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-25 15:42 -- Verification failed: git-canvas-sxqsj - GitKraken-style commit edges

**Type:** Work / Verification failure
**Task:** git-canvas-sxqsj - Draw commit edges GitKraken-style
**Iteration:** 1 of 3
**Reasons:** parent edge of a merge commit is classified as MERGE whenever mergedCommit is set, so the straight run lands in the busy parent lane and cuts through other commits (reset-then-merge and no-ff-merge-into-fresh-branch cases)
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-11-25 15:41 -- Verification failed: git-canvas-kngx5 - Larger repo header labels

**Type:** Work / Verification failure
**Task:** git-canvas-kngx5 - Double the font size of repo header labels and the origin connector label
**Iteration:** 1 of 3
**Reasons:** initialization-order bug in RepoRegion: nameLabel reads NAME_Y before it is initialized, so every new region (start, clone, restore, rebuild) draws the repo name at y = 0
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-11-25 08:35 -- Batch started: [git-canvas-kngx5, git-canvas-sxqsj]

**Type:** Work / Batch start
**Tasks:** git-canvas-kngx5 - Double the font size of repo header labels and the origin connector label; repo name and connector label bold, git-canvas-sxqsj - Draw commit edges GitKraken-style - straight vertical runs in a lane with a short rounded bend where the edge changes lanes
**Parallel:** yes (2 workers — no file overlap: kngx5 RepoRegion/App.kt, sxqsj Commit.kt/line components)

---

## 2026-11-25 08:35 -- Task verified and completed: git-canvas-nymhr - Rebase and pull (rebase) stop mid-way on a conflict until Continue or Abort

**Type:** Work / Task completion
**Task:** git-canvas-nymhr - Rebase and pull (rebase) stop mid-way on a conflict until Continue or Abort
**Summary:** rebase and pull (rebase) stop mid-way on a conflict until Continue or Abort
**Duration:** 15m
**Verification:** PASS (iteration 2)
**Files changed:** 5
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-25 08:34 -- Task verified and completed: git-canvas-xt4ch - Draw label connectors as thin blue Bezier-curved arrows pointing toward the commit, so they no longer look like commit edges

**Type:** Work / Task completion
**Task:** git-canvas-xt4ch - Draw label connectors as thin blue Bezier-curved arrows pointing toward the commit, so they no longer look like commit edges
**Summary:** draw label connectors as thin blue Bezier arrows pointing toward the commit
**Duration:** 8m
**Verification:** PASS (iteration 2)
**Files changed:** 4
**Tests added:** 0
**ADRs written:** none

---

## 2026-11-25 08:32 -- Verification failed: git-canvas-nymhr - Rebase and pull (rebase) stop mid-way on a conflict until Continue or Abort

**Type:** Work / Verification failure
**Task:** git-canvas-nymhr - Rebase and pull (rebase) stop mid-way on a conflict until Continue or Abort
**Iteration:** 1 of 3
**Reasons:** a rebase from a detached HEAD never stops on a conflict (guard skips detached HEAD; pending state cannot record a detached original), contradicting ADR 0006 decision 2/4 without a recorded exception
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-11-25 08:28 -- Verification failed: git-canvas-xt4ch - Label connector arrows

**Type:** Work / Verification failure
**Task:** git-canvas-xt4ch - Draw label connectors as thin blue Bezier-curved arrows pointing toward the commit
**Iteration:** 1 of 3
**Reasons:** arrowhead drawn at the label end instead of the commit / previous-label end (Konva Arrow puts the head on the last point; CommitLabel passes the commit dock as start)
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-11-25 10:20 -- Modeling / Captured: git-canvas-sxqsj - Draw commit edges GitKraken-style

**Type:** Modeling / Capture
**BC:** git-canvas
**Filed to:** todo
**Summary:** Cross-lane commit edges become straight vertical runs in the side branch's lane plus a short vertical-tangent Bezier bend near the branch-off or merge commit; edges stay black. Depends on git-canvas-xt4ch (shared Bezier helper).

---

## 2026-11-25 08:26 -- Batch started: [git-canvas-xt4ch]

**Type:** Work / Batch start
**Tasks:** git-canvas-xt4ch - Draw label connectors as thin blue Bezier-curved arrows pointing toward the commit, so they no longer look like commit edges
**Parallel:** yes (joins running nymhr; kngx5 held until nymhr is merged — both touch RepoRegion/App.kt)

---

## 2026-11-25 08:26 -- Task verified and completed: git-canvas-q38zx - Raise the small info texts in the control panel (Bulma help / is-size-7) to the standard body font size

**Type:** Work / Task completion
**Task:** git-canvas-q38zx - Raise the small info texts in the control panel (Bulma help / is-size-7) to the standard body font size
**Summary:** raise control panel info texts to the standard font size
**Duration:** 6m
**Verification:** PASS (iteration 1)
**Files changed:** 1
**Tests added:** 0
**ADRs written:** none

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

