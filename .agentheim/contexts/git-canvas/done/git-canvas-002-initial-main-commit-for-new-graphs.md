---
id: git-canvas-002
title: Initial 'main' commit for all new graphs — fix detached HEAD regression
status: done
type: bug
context: git-canvas
created: 2026-05-25
completed: 2026-05-25
commit: fff62aa
depends_on: []
blocks: []
tags: [initialization, detached-head, default-branch, main, regression]
related_adrs: ["0001", "0003"]
related_research: []
prior_art: []
---

## Why

Dynamically added graphs (via "Add Repository") currently start with an empty canvas — no initial commit, no branch. When the trainer clicks "Add Commit" on such a graph the commit is created in detached HEAD state because there is no branch pointer to advance. This is a regression introduced by infrastructure-006, whose acceptance criteria explicitly (but incorrectly) specified "no initial commit".

In the same fix, rename the default branch from `master` to `main` to match modern Git conventions. Trainers use this tool to teach Git; shipping `master` as the default sends the wrong signal.

## What

Every freshly initialized graph — both the "origin" graph created on startup and any graph added via "Add Repository" — must begin with an initial commit on a branch named `main`.

URL-restored graphs are not affected: they use branch names verbatim from the snapshot.

## Acceptance criteria

- [x] On startup with no `#state=` fragment: the "origin" graph has exactly one initial commit and a `main` branch label. No `master` label appears anywhere on a fresh canvas.
- [x] A graph added via "Add Repository" also starts with one initial commit and a `main` branch. Adding further commits advances `main` normally — no detached HEAD.
- [x] Clicking "Add Commit" on any newly created graph works correctly from the very first click.
- [x] URL restore is unaffected: a snapshot containing `master` branch names renders those names as-is.
- [x] Production webpack build passes cleanly.

## Notes

### Root cause

`App.kt`'s "Add Repository" handler calls `GitGraph.renderEmptyRegion()`, which renders only the region border and label — no initial commit. Change this call to `initGraph()` (or equivalent), which creates the first commit with a default branch.

### Rename `master` → `main`

`GitGraph.initGraph()` currently hard-codes the initial branch name as `"master"`. Change it to `"main"`. Check for any other hard-coded `"master"` strings in `GitGraph.kt` and `App.kt` that refer to the default branch name — rename those too. Do **not** rename strings that are part of unrelated logic (e.g., branch-color mappings keyed by name) unless they also need updating.

### URL restore path

The URL-restore code path in `App.kt` creates an empty graph and then populates it from the snapshot. That path should continue using the snapshot's branch names without modification — do not inject `"main"` during restore.

### `renderEmptyRegion()` usage after this fix

After the fix, `renderEmptyRegion()` is no longer called for user-triggered "Add Repository". Check whether it is still used by the URL-restore path; if it is, keep it. If it is now dead code, remove it.

## Outcome

`GitGraph.initGraph()` now creates the initial branch as `main` instead of `master`. The dead `renderEmptyRegion()` method was removed from `GitGraph.kt`. The "Add Repository" handler in `App.kt` now calls `initGraph()` (via a callback pattern, since `activeGraph` is declared after `showAddRepoDialog`), so every new graph starts with commit m1 and a `main` branch. After creation the new graph becomes the active graph. The URL-restore path is unaffected — it continues to use `restoreFromSnapshot()` verbatim. Production webpack build passes cleanly.

Key files:
- `src/jsMain/kotlin/graph/GitGraph.kt` — `"master"` → `"main"` in `initGraph()`, `renderEmptyRegion()` removed
- `src/jsMain/kotlin/App.kt` — `showAddRepoDialog` accepts an `onNewGraph` callback; call site passes lambda that updates `activeGraph`
