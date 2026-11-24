---
id: git-canvas-wcbec
title: Re-layout repo regions when they grow into each other
status: doing
type: feature
context: git-canvas
created: 2026-11-24
completed:
depends_on: [git-canvas-dxt9c]
blocks: []
tags: [layout, repo-region, multi-repo]
related_adrs: [0001]
related_research: []
prior_art: [git-canvas-003, git-canvas-dxt9c]
---

## Why
Repo regions grow upward and rightward as commits and branches are added (`RepoRegion.updateBounds`,
left and bottom edges fixed). With clones placed below their origin (git-canvas-dxt9c) a growing
clone eventually runs into the origin above it (vertical gap starts at `cloneVerticalGap` = 200 px),
and the two clones of a triangle start only about 100 px apart horizontally (offsets
origin.x +/- 400, initial width 700), so the left clone overlaps the right one after a few extra
swimlanes. git-canvas-dxt9c deliberately used only a fixed gap. Regions are not draggable (only the
stage pans), so the trainer cannot fix an overlap by hand mid-demo.

## What
After every operation that changes a region's bounds, the app automatically moves regions apart so
that no two regions are closer than a minimum clearance. Decisions made in refinement:

- **Automatic**, no trainer action and no extra button.
- **The lower / right region moves; the other stays put.** For a colliding pair A, B:
  - If their horizontal extents (plus clearance) overlap and B is below A (B's bottom edge is lower
    than A's), B is pushed **down** until its top edge is at least the vertical clearance below A's
    bottom edge.
  - Otherwise, if B lies to the right of A (B's left edge > A's left edge), B is pushed **right**
    until its left edge is at least the horizontal clearance right of A's right edge.
  - Moves only ever go down or right, and are repeated until no pair conflicts (cascade). With at
    most 3 repos this always terminates.
  - Regions never move back up/left when space frees up (no shrinking, no "magnetic" pull-back).
- **Instant jump**, no animation. Reuse the dxt9c mechanism: rebuild the moved graph at the new
  offsets (snapshot + restore into a fresh layer, old one destroyed via `GitGraph.destroy()`), so
  snapshot offsets stay truthful and hit-testing follows automatically.
- **Clearances** are named constants in `GitGraphConfiguration`: vertical = existing
  `cloneVerticalGap` (200 px); horizontal = a new constant (about 100 px, the triangle's initial
  gap). No magic numbers in `App.kt`.
- **Share URL:** moved positions are just x/y offsets, which snapshots already persist, so a shared
  scenario restores the post-move layout. Resolution also runs once after restoring a scenario, so
  old share links that already contain overlaps are fixed up on load.
- Applies to all repo pairs, not just clone triangles ("Add repository" repos placed to the right
  are covered by the same rule).

## Acceptance criteria
- [ ] In a three-repo triangle (origin + two clones), adding enough commits to a clone that its region would reach within `cloneVerticalGap` of the origin's bottom edge moves that clone down instead; afterwards the clone's top edge is >= `cloneVerticalGap` below the origin's bottom edge.
- [ ] In the triangle, adding branches/swimlanes to the left clone until it would come within the horizontal clearance of the right clone moves the right clone right; afterwards the gap between them is >= the horizontal clearance constant.
- [ ] When moving one region causes a new conflict (e.g. the pushed-right clone now hits an "Add repository" repo further right), that region moves as well; after any single operation no two regions are closer than the clearances.
- [ ] Only the lower/right region of a pair ever moves, and only down or right; the origin of a triangle never moves because of a clone's growth.
- [ ] A moved region keeps everything: commits, labels, HEAD, remote-tracking labels, remote styling / REMOTE pill / `origin -> <name>` header, active-repo highlight if it was active, and the `origin` connector lines follow it. Click-to-activate hits the new position.
- [ ] Push, fetch and pull still work between moved repos (smoke test per CLAUDE.md).
- [ ] Share URL round-trip after a move restores every repo at its post-move offsets; restoring a scenario whose regions overlap resolves the overlap on load.
- [ ] Clearance values are named constants in `GitGraphConfiguration`.
- [ ] ADR 0001 is amended with the automatic growth re-layout rule (lower/right moves, instant, down/right only), and the git-canvas README "Layout decisions" and the user manual's multi-repo/clone section describe it.
- [ ] Full verification build passes; no new browser console errors.
- [ ] During a live triangle demo, a region jumping away reads as a sensible, predictable move rather than a glitch. [human-eye]

## Notes
- Trigger point: `GitGraph.onRegionBoundsChanged` already exists (App.kt wires it to
  `refreshRemoteLinks`). It fires from inside `updateRegionBounds()`, which runs in the middle of a
  graph operation, before `batchDraw`. Rebuilding a graph (possibly the one that is currently
  mutating) from inside that callback is re-entrant. Defer the resolution until the operation has
  finished, for example by queueing it via `window.setTimeout(0)` or by running it at the end of the
  control-panel handler. Then rebuild only the regions that must move, never the growing one.
- The rule needs each graph's current region rect (left, top, width, height). `currentRegionHeight`
  exists (dxt9c); expose the full current bounds from `GitGraph`/`RepoRegion` instead of
  recomputing in App.kt.
- "Below" is decided by bottom edges because they are stable anchors (regions grow up, never down).
- After a rebuild, re-apply `setRemoteLinks` and the active highlight, and call
  `refreshRemoteLinks()`, the same as the dxt9c triangle move does. Check whether view toggles
  (e.g. lost-commit visibility) survive the snapshot/restore rebuild; if not, carry them over.
- Not auto-panning the stage after a move is acceptable; the trainer can pan.
- Out of scope: animated moves, a manual "tidy layout" button, re-compacting regions when space
  frees up, draggable regions.
