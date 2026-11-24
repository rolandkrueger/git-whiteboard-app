---
id: git-canvas-dxt9c
title: Place clones below their origin in a triangle layout
status: doing
type: feature
context: git-canvas
created: 2026-11-24
completed:
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
