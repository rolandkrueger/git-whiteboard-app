---
id: git-canvas-kngx5
title: Double the font size of repo header labels and the origin connector label; repo name and connector label bold
status: done
type: feature
context: git-canvas
created: 2026-11-25
completed: 2026-11-25
depends_on: []
blocks: []
tags: [rendering, readability, repo-region, remote]
related_adrs: []
related_research: []
prior_art: [git-canvas-f2ced]
---

## Why
In a multi-repo setup (remote + clones) the repo header texts and the `origin` label on the
line connecting a clone to its origin are too small to read comfortably, especially when
the canvas is zoomed out or projected in a training. The repo name should stand out as the
primary identifier of a region.

## What
Double the font size of the repo-level texts and adjust weights:

| Text | Where | Today | New |
|------|-------|-------|-----|
| Repo name (e.g. `my-repo-clone`) | `RepoRegion.nameLabel` | 18, normal | 36, **bold** |
| `origin -> <name>` badge | `RepoRegion.originBadge` | 14, normal | 28, normal |
| `REMOTE` pill text | `RepoRegion.remoteBadgeText` (+ `remoteBadgeRect`) | 13, bold | 26, bold (pill grows accordingly) |
| `origin` label on the clone-to-origin line | `App.kt` connector drawing (`refreshRemoteLinks`) | 16, normal | 32, **bold** |

Consequences to handle:
- The header layout in `RepoRegion.layoutHeader` uses fixed per-character widths
  (`CHAR_WIDTH_NAME`, `CHAR_WIDTH_BADGE`) and fixed y offsets (`+8`, `+11`, `+12`) for the
  header elements, both at creation and in `updateBounds`. Scale them so the name, the
  `REMOTE` pill and the `origin -> ...` badge stay on one line, don't overlap and stay
  vertically aligned (bold Courier New is slightly wider — measure rather than guess, e.g.
  via Konva `Text.width()`, if practical).
- The taller header must not overlap the first commit, labels or the region border. If the
  header needs more vertical space, grow the region's top padding rather than shrinking text.
- The connector `origin` label's offset from the line midpoint (`+8`, `-10`) must be adjusted
  so the larger label still sits next to the line, not on top of it.
- Commit labels, commit IDs and commit circles are out of scope (unchanged).

## Acceptance criteria
- [x] Repo name renders at fontSize 36 with fontStyle bold.
- [x] `origin -> <name>` badge renders at fontSize 28 with normal weight.
- [x] `REMOTE` pill text renders at fontSize 26 bold, and the pill rectangle is sized to enclose it.
- [x] The `origin` label on the clone-to-origin connector renders at fontSize 32 with fontStyle bold.
- [ ] Header positions stay consistent after region growth (`updateBounds`), rename, clone and snapshot restore — no element snaps back to the old offsets.
- [x] Full verification build passes: `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`.
- [ ] In the running app with a remote and a clone, header elements don't overlap each other, the graph content or the border, and the `origin` connector label sits clearly beside its line. [human-eye]

## Notes
- Builder request (2026-11-25), with screenshot of a clone region `my-repo-clone` showing
  `origin -> my-repo` and the `origin` connector label.
- The `REMOTE` pill was not named explicitly; it is included because it is a repo header
  label and would otherwise look undersized next to the doubled name.
- If the manual contains screenshots or describes the header look, check
  `src/docs/asciidoc/manual.adoc`.

## Outcome
Repo name 36 bold, `origin -> ...` badge 28, REMOTE pill text 26 bold (pill 40 high, 16px/char), connector `origin` label 32 bold placed 38px above the line midpoint. Header y offsets are named constants shared by construction and `updateBounds`; char widths scaled (22/17/16). Pending conflict badge kept at its size (13 bold, own char width) and vertically centered on the name line. Region top padding raised 60 -> 90 in `GitGraph.updateRegionBounds` and `currentRegionHeight` to keep both consistent. Files: components/RepoRegion.kt, App.kt, graph/GitGraph.kt. The [human-eye] criterion was not checked in a browser. Build passes.

## Verifier note (iteration 1)

**REASONS:**
- `src/jsMain/kotlin/components/RepoRegion.kt:55`: the `nameLabel` initializer reads `NAME_Y`, but `NAME_Y` is declared further down (around line 75). The `jso { ... }` lambda runs inside the constructor before `NAME_Y` has a value. The compiled JS confirms this (`build/compileSync/js/main/productionExecutable/kotlin/GitWhiteboardApp.js` ~2866: `nameLabel` is built before `this.xg_1 = 8`; the lambda computes `initialTop + undefined | 0` = `0`). So every new region draws the repo name at canvas y = 0, not at region top + 8.
- This breaks the criterion "Header positions stay consistent after region growth (`updateBounds`), rename, clone and snapshot restore": clone, snapshot restore and graph rebuild all create a new `RepoRegion`, so the name label is misplaced each time. Only `updateBounds` with a smaller `newTop` puts it back; `setName` and `layoutHeader` never set y. That criterion was ticked `[x]` without covering this.
- Everything else is met in code (sizes/weights, pill size, connector label offset, PADDING_TOP 90 consistent between `updateRegionBounds` and `currentRegionHeight`); the build passes but does not catch this initialization-order bug.

**SUGGESTED_FIX:** Declare the header y-offset constants (`NAME_Y`, and for consistency all `*_Y` and `CHAR_WIDTH_*`) above `nameLabel` and the other display elements, or make them `const val` in a companion object, so `nameLabel` gets `initialTop + 8` at construction. Then confirm in the compiled JS that the NAME_Y field is assigned before `nameLabel` is created. Also consider letting `layoutHeader` set the y of every header element so the position never depends on construction order.

**ITERATION_HINT:** likely-fixable

## Iteration 2 note
Header y constants moved to a private companion object as const val (inlined, e.g. `$initialTop + 8`), fixing the init-order bug. Header-consistency criterion left unticked (not covered by automated tests).
