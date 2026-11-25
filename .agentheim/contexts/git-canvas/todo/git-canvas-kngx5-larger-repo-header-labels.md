---
id: git-canvas-kngx5
title: Double the font size of repo header labels and the origin connector label; repo name and connector label bold
status: todo
type: feature
context: git-canvas
created: 2026-11-25
completed:
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
- [ ] Repo name renders at fontSize 36 with fontStyle bold.
- [ ] `origin -> <name>` badge renders at fontSize 28 with normal weight.
- [ ] `REMOTE` pill text renders at fontSize 26 bold, and the pill rectangle is sized to enclose it.
- [ ] The `origin` label on the clone-to-origin connector renders at fontSize 32 with fontStyle bold.
- [ ] Header positions stay consistent after region growth (`updateBounds`), rename, clone and snapshot restore — no element snaps back to the old offsets.
- [ ] Full verification build passes: `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`.
- [ ] In the running app with a remote and a clone, header elements don't overlap each other, the graph content or the border, and the `origin` connector label sits clearly beside its line. [human-eye]

## Notes
- Builder request (2026-11-25), with screenshot of a clone region `my-repo-clone` showing
  `origin -> my-repo` and the `origin` connector label.
- The `REMOTE` pill was not named explicitly; it is included because it is a repo header
  label and would otherwise look undersized next to the doubled name.
- If the manual contains screenshots or describes the header look, check
  `src/docs/asciidoc/manual.adoc`.
