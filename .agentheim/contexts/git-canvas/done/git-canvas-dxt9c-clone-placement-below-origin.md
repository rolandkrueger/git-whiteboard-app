---
id: git-canvas-dxt9c
title: Place clones below their origin in a triangle layout
status: done
type: feature
context: git-canvas
created: 2026-11-24
completed: 2026-11-24
depends_on: []
blocks: [git-canvas-f2ced, git-canvas-wcbec]
tags: [clone, remote, multi-repo, layout, repo-region]
related_adrs: [0001, 0005]
related_research: []
prior_art: [git-canvas-wv6va, git-canvas-003, git-canvas-001]
---

## Why
Today a clone is placed to the right of all existing repos (`xOffset = max + repoSeparation`, `yOffset = 0`).
For push/fetch/pull demos the trainer wants the classic picture: the remote on top, the clones
below it. With two clones the three repos should form a triangle, so the audience sees one
upstream and two developers side by side.

## What
Change where the Clone action places the new repo region:

- **First clone of a repo** goes directly below its origin (same horizontal center, origin above).
- **Second clone of the same origin** goes below as well; the first clone shifts half a
  separation to the left and the new clone sits half a separation to the right, so origin +
  two clones form a symmetric triangle (variant A — the existing clone is moved once, predictably).
- **Clone of a clone** goes below its own origin (a vertical chain), not into the triangle.
- **"Add repository"** (non-clone) keeps today's behavior: to the right of the existing repos.
- Vertical gap: the clone's region top edge must sit clearly below the origin's region bottom
  edge. Regions grow upward (bottom anchor fixed, see `RepoRegion.updateBounds`), so the gap must
  be generous enough that a few new commits in a clone don't immediately overlap the origin.
  Automatic re-layout on growth is explicitly out of scope (git-canvas-wcbec).
- Positions must survive the Share URL round-trip: `createGraph` currently ignores the snapshot's
  `yOffset` for repos after the first (`App.kt` restore path passes only `xOffset`). Fix that so
  restored repos keep both offsets.

## Acceptance criteria
- [ ] Cloning the initial repo places the clone region below the origin region, horizontally centered under it, with its top edge at least a named constant gap (>= 150 px in canvas coordinates) below the origin's bottom edge.
- [ ] Cloning the same origin a second time moves the first clone left and places the second clone right, both on the same vertical level, symmetric about the origin's horizontal center, regions not overlapping.
- [ ] After the move, the first clone's commits, labels, HEAD, remote-tracking labels and region border are all at the new position (nothing left behind), and click-to-activate hits the new position.
- [ ] Cloning a clone places the new region below that clone (its origin), not below the top-level origin.
- [ ] "Add repository" still places a new, non-cloned repo to the right of the existing repos.
- [ ] Push, fetch and pull still work between the moved/placed repos (smoke test per CLAUDE.md).
- [ ] Share URL round-trip of a triangle scenario restores every repo at the same x and y offsets as before sharing; `createGraph` accepts a `yOffset`.
- [ ] ADR 0001 "Spatial arrangement" is amended to note that clones are auto-placed below their origin (triangle for two clones), and the git-canvas README "Layout decisions" reflects it.
- [ ] The user manual's clone section describes the new placement.
- [ ] Full verification build passes; no new browser console errors.
- [ ] With origin and two clones on screen, the arrangement reads as "one remote on top, two clones below" at a glance. [human-eye]

## Notes
- `GitGraphConfiguration.xOffset/yOffset` are immutable `val`s and are written into snapshots
  (`GitGraph.getSnapshot`). Moving the first clone therefore needs a deliberate approach: either
  rebuild that graph at the new offset (snapshot + restore into a fresh layer at the new offset)
  or offset its Konva `Layer` and make snapshot/hit-testing (`containsPoint`) aware of it. Pick
  one that keeps snapshot offsets truthful.
- The triangle may extend to negative x (left of the origin). That is fine on the infinite
  canvas; consider panning the stage so all three regions are visible after the second clone.
- Gap constant belongs in `GitGraphConfiguration` / next to `repoSeparation`, not as a magic number.
- Follow-up for growth-triggered re-layout: git-canvas-wcbec. Remote highlighting builds on this:
  git-canvas-f2ced.

## Outcome
Clone placement implemented in `App.kt` (clone button handler): first clone centered below origin at `cloneVerticalGap` (200 px, `GitGraphConfiguration`); second clone moves the first by rebuilding it (snapshot + restore into a fresh layer, old layer destroyed via new `GitGraph.destroy()`) at origin.x - 400 and places the new one at origin.x + 400; clone of a clone goes below its own origin. `createGraph` now accepts `yOffset` and the URL restore path passes it. ADR 0001, BC README and manual updated. Iteration 2: the clone's y offset is now based on the origin's CURRENT region height (new `GitGraph.currentRegionHeight`, computed from commit positions), so the clone region's top edge stays `cloneVerticalGap` below the origin's bottom edge even for a grown origin; the same offset serves the rebuilt first clone and the second clone in the triangle. Full build passes. Behavioural criteria (placement, move, push/fetch/pull smoke, human-eye) were NOT verified in a browser (no browser automation available) and are left unticked. The stage is not auto-panned after the second clone.

## Verifier note (iteration 1)

**REASONS:**
- Criterion 1 ("clone region top edge at least a named constant gap (>= 150 px) below the origin's bottom edge") fails whenever the origin has grown. In `src/jsMain/kotlin/App.kt` (clone handler), `cloneYOffset = source.yOffset + source.initialRegionHeight + GitGraphConfiguration.cloneVerticalGap` uses the fixed initial height (480). A region grows upward from a fixed bottom edge (`GitGraph.updateRegionBounds`, `RepoRegion.updateBounds`), and a clone copies all of its origin's commits at the same line positions. So the clone's region grows upward by the same amount as the origin's, and that growth comes straight out of the gap.
- Worked numbers (`regionInitialTop = bottomOffset - 320`; `neededTop = bottomOffset - 70*line - 85`): growth starts at line position 4. With 6 commits on a line (line 5) the gap is about 200 - 115 = 85 px, below the 150 px floor. With 8 commits the clone's region overlaps the origin's. This is a normal trainer setup (add a few commits, then clone), not an edge case.
- The same defect affects criterion 2. The rebuilt first clone and the second clone are both placed at the same `cloneYOffset`, so the triangle level also overlaps the origin once the origin has grown.
- Observation: the rest of the logic is sound. The first clone is rebuilt through snapshot and restore into a fresh layer, so everything moves together, and snapshot offsets stay truthful.

**SUGGESTED_FIX:** Base the clone's y offset on the origin's current region height, not `initialRegionHeight`. One way is to expose the current region top or height from `GitGraph`/`RepoRegion` and set `cloneYOffset = source.yOffset + currentRegionHeight + cloneVerticalGap`, so the clone's region, which starts as tall as the origin's, still has its top edge `cloneVerticalGap` below the origin's bottom edge. Use the same value for the rebuilt first clone in the triangle case.

**ITERATION_HINT:** likely-fixable
