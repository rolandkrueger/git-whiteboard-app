---
id: git-canvas-003
title: RepoRegion auto-resize — frame must grow with graph content
status: done
type: bug
context: git-canvas
created: 2026-05-25
completed: 2026-05-25
commit: 011bd02
depends_on: []
blocks: []
tags: [region, resize, bounds, layout, reporegion, clickable, containspoint]
related_adrs: ["0001"]
related_research: []
prior_art: [git-canvas-002]
---

## Why

The `RepoRegion` border is sized once at graph initialization and never updated. As the trainer adds commits, branches, or tags, the graph grows beyond the initial frame. Two problems result:

1. **Visual overflow** — commit nodes, branch labels, and edges render outside the visible frame border, which looks broken during a live session.
2. **Dead click zone** — `containsPoint()` checks against the fixed frame bounds. Commits rendered outside the original frame cannot be clicked to activate the repo. The trainer loses the ability to switch to that repo from the canvas.

## What

After every graph-mutating operation — add commit, add branch, checkout, merge, rebase, reset, cherry-pick, tag, garbage collection — the `RepoRegion` recalculates its bounding rectangle to fully enclose all rendered content, then redraws the border.

## Acceptance criteria

- [x] After adding commits that extend the graph vertically (new line positions), the region border grows upward to enclose all commit nodes.
- [x] After adding a branch on a new swimlane, the region border grows horizontally to enclose the new lane (on the next commit placed on that swimlane).
- [x] `containsPoint(x, y)` returns true for coordinates inside the updated (grown) region, including commits that were outside the original static frame.
- [x] Click-to-focus works correctly on any commit node regardless of how far the graph has grown from its initial size.
- [x] The region border is redrawn without visible flicker or artifacts (Konva rect attrs updated in-place; no destroy/re-create).
- [x] Production webpack build passes cleanly.

## Outcome

Added `updateBounds(newTop, newWidth, newHeight)` to `RepoRegion` that updates the Konva `Rect` and the name label in-place, only ever expanding bounds (never shrinking). Added `updateRegionBounds()` private method to `GitGraph` that iterates all `Commit` objects to compute max swimlane x and min linePosition y, then calls `repoRegion.updateBounds(...)`. This method is called before `layer.batchDraw()` in `initGraph()`, `private addCommit()`, and `restoreFromSnapshot()` — the three paths that change commit positions. The region's left edge and bottom edge are fixed anchors; it grows upward (decreasing top y) and rightward (increasing width). `containsPoint()` automatically uses the updated `currentTop`, `currentWidth`, `currentHeight` fields.

Key files:
- `src/jsMain/kotlin/components/RepoRegion.kt`
- `src/jsMain/kotlin/graph/GitGraph.kt`
