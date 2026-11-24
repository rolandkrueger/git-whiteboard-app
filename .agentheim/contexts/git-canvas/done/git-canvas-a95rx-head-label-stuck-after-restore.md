---
id: git-canvas-a95rx
title: HEAD label stays on the previous commit after restoring a snapshot or cloning
status: done
type: bug
context: git-canvas
created: 2026-11-24
completed: 2026-11-24
depends_on: []
blocks: []
tags: [head, restore, clone, rendering]
related_adrs: [0005]
related_research: []
prior_art: [git-canvas-wv6va, git-canvas-j2m8z]
---

## Why
After a graph is rebuilt via `restoreFromSnapshot` (loading a Share URL, and every clone, which is built through the same restore path), the first new commit leaves the HEAD label on the previous commit while the branch label moves on. In push/pull demos the clone shows HEAD in the wrong place, which teaches the audience the wrong thing.

## What
Make the HEAD label behave in a restored or cloned repo exactly as in a freshly created one: when HEAD is attached, it follows its branch label on commit, fast-forward, merge, rebase, reset and checkout; when detached, it follows the new commit.

## Acceptance criteria
- [x] Fresh app, add two commits, Share, reload, add a commit: the HEAD label sits next to the branch label on the new commit (not on the previous one).
- [x] Clone a repo, add a commit in the clone: HEAD label moves with the branch label to the new commit.
- [x] In a clone, Pull (fast-forward, merge commit, and with rebase) leaves the HEAD label next to the current branch label on the resulting tip.
- [x] Push into a remote whose pushed branch is checked out moves the remote's HEAD label along (also when the remote was itself restored from a Share URL).
- [x] Restore a snapshot with a detached HEAD, add a commit: the HEAD label moves to the new commit.
- [x] After restore, checking out another branch (double-click on its label or the Refs tab) shows HEAD next to that branch label and no stale HEAD label remains anywhere.
- [x] No regression in a fresh, non-restored graph (commit, branch, checkout, merge, rebase, reset, amend).
- [x] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Likely cause (from code reading, to be confirmed): in `GitGraph.restoreFromSnapshot`, the attached-HEAD branch calls `targetBranch.commit.addBranch(head)`, which registers the Head in the commit's label list. The normal path (`initGraph` / `addBranch` / `Head.attachToBranch`) does not do that for an attached HEAD. On the next commit, `addCommit` calls `oldHeadCommit.repositionBranches(layer)`, which re-attaches every label in that list, including the Head, to the old commit. Also compare the order of `branches.add(head)` in restore versus `initGraph`.

Found during the conductor's browser smoke tests of the remote-operations chain (git-canvas-j2m8z, -wv6va, -w3ndg). Pre-existing, not introduced by that chain. Fix narrowly in the restore path; GitGraph couples state and rendering tightly.

## Outcome

Root cause confirmed: `GitGraph.restoreFromSnapshot` registered the Head in the commit label list via `commit.addBranch(head)` (attached path, and redundantly in the detached path, where `attachToCommit` already does it). The normal path never does this for an attached HEAD, so `addCommit` -> `oldHeadCommit.repositionBranches` re-attached the HEAD label to the old commit. Fix: removed both explicit `addBranch(head)` calls in the restore path (src/jsMain/kotlin/graph/GitGraph.kt). Full build passes. Browser criteria are unverified (no browser available to the worker); left for the conductor smoke test.

Conductor browser smoke test (2026-11-24): HEAD follows the branch after Share/reload (m4), in a clone (m5), on push into a restored remote (m5), on pull fast-forward (m6), merge (m8m7) and rebase (m10*); detached restore + commit moves HEAD to H4; checkout of main after restore leaves no stale HEAD label; no new console errors. Amend/reset/cherry-pick/tag checkout not exercised in the browser (code paths untouched).
