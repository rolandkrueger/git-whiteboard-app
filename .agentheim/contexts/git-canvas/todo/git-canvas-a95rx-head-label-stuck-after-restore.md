---
id: git-canvas-a95rx
title: HEAD label stays on the previous commit after restoring a snapshot or cloning
status: todo
type: bug
context: git-canvas
created: 2026-11-24
completed:
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
- [ ] Fresh app, add two commits, Share, reload, add a commit: the HEAD label sits next to the branch label on the new commit (not on the previous one).
- [ ] Clone a repo, add a commit in the clone: HEAD label moves with the branch label to the new commit.
- [ ] In a clone, Pull (fast-forward, merge commit, and with rebase) leaves the HEAD label next to the current branch label on the resulting tip.
- [ ] Push into a remote whose pushed branch is checked out moves the remote's HEAD label along (also when the remote was itself restored from a Share URL).
- [ ] Restore a snapshot with a detached HEAD, add a commit: the HEAD label moves to the new commit.
- [ ] After restore, checking out another branch (double-click on its label or the Refs tab) shows HEAD next to that branch label and no stale HEAD label remains anywhere.
- [ ] No regression in a fresh, non-restored graph (commit, branch, checkout, merge, rebase, reset, amend).
- [ ] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Likely cause (from code reading, to be confirmed): in `GitGraph.restoreFromSnapshot`, the attached-HEAD branch calls `targetBranch.commit.addBranch(head)`, which registers the Head in the commit's label list. The normal path (`initGraph` / `addBranch` / `Head.attachToBranch`) does not do that for an attached HEAD. On the next commit, `addCommit` calls `oldHeadCommit.repositionBranches(layer)`, which re-attaches every label in that list, including the Head, to the old commit. Also compare the order of `branches.add(head)` in restore versus `initGraph`.

Found during the conductor's browser smoke tests of the remote-operations chain (git-canvas-j2m8z, -wv6va, -w3ndg). Pre-existing, not introduced by that chain. Fix narrowly in the restore path; GitGraph couples state and rendering tightly.
