---
id: git-canvas-sxqsj
title: Draw commit edges GitKraken-style - straight vertical runs in a lane with a short rounded bend where the edge changes lanes
status: doing
type: feature
context: git-canvas
created: 2026-11-25
completed:
depends_on: [git-canvas-xt4ch]
blocks: []
tags: [rendering, readability, commit-edges]
related_adrs: []
related_research: []
prior_art: []
---

## Why
Commit edges between lanes are drawn as straight diagonal lines from the parent's upper dock
point to the child's lower dock point. Long diagonals cut across other lanes and make the graph
look busy. GitKraken's style - edges run straight inside a lane and change lanes with a short
rounded bend - is the clearest graph presentation the builder knows and fits the curved label
connectors introduced by git-canvas-xt4ch.

## What
Parent edges (`Commit.parentLine`) and merge-parent edges (`Commit.mergedParentLine`) between
commits in **different swimlanes** are drawn as one smooth path:

- **Branch-off edge** (child in another lane than its parent, not a merge edge): leaves the
  parent vertically, bends into the child's lane within roughly one commit distance
  (`commitDistance` = 70px) above the parent, then runs straight up in the child's lane to the
  child's lower dock point.
- **Merge edge** (`mergedParentLine`, and a parent edge whose child sits in the parent's
  original lane after a fast-forward-free merge): leaves the merged commit vertically, runs
  straight up in the merged commit's lane, and bends into the merge commit's lane within roughly
  one commit distance below the merge commit.
- Rule of thumb: the straight vertical run always lies in the **side branch's lane**, never in
  the lane that holds the other commits, so it does not cut through them.
- If the two commits are only one row apart, the whole edge is the bend.
- Edges within the same lane stay straight vertical lines (unchanged).
- The bend is a cubic Bezier segment with vertical tangents at both ends (control points
  vertically above/below the bend's start and end), so the path enters and leaves commits
  vertically.
- Look unchanged otherwise: black, 3px, no arrowhead, non-listening, drawn below commits.

Implementation hint: a Konva `Line` with `bezier: true` draws chained cubic segments
(start, c1, c2, p1, c3, c4, p2, ...). A straight segment is a cubic whose control points lie on
the segment, so "straight - bend - straight" fits into one `Line` without a new binding.
Reuse the Bezier helper introduced by git-canvas-xt4ch if it fits; keep commit edges in their
own component so they do not pick up the label-arrow look. Lost commits, rebase, restore,
clone and rerender go through `Commit.render`, so they get the new shape automatically -
verify.

Out of scope: per-branch edge colours (edges stay black, builder decision 2026-11-25) and any
change to swimlane allocation / lane compaction.

## Acceptance criteria
- [ ] Edge path computation lives in a small, separately testable function (input: parent dock point, child dock point, edge kind, commit distance; output: Konva point list), with cases: same lane -> straight 2-point line; one row apart -> single bend; branch-off over several rows -> bend near the parent, then vertical in the child's lane; merge over several rows -> vertical in the merged commit's lane, then bend near the merge commit.
- [ ] Cross-lane edges are drawn with `bezier: true`; the tangent at both ends is vertical (first and last control points share the x of their end point).
- [ ] Same-lane edges are unchanged straight lines; edge colour, width and missing arrowhead are unchanged.
- [ ] Edges render correctly after commit, branch, merge, rebase (incl. swimlane shift), cherry-pick if present, reset with lost commits shown/hidden, clone, fetch/pull and snapshot restore; no stale edges remain.
- [ ] Full verification build passes: `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`; no new browser console errors.
- [ ] A graph with a feature branch spanning several rows and a merge back reads like GitKraken: straight lane runs, short smooth bends, no diagonals crossing other lanes' commits. [human-eye]

## Notes
- Builder decisions (2026-11-25): GitKraken-style bends rather than one long S-curve; edges
  stay black; ordered after git-canvas-xt4ch to share its Bezier helper and avoid parallel
  edits of the line components.
- Current code: `Commit.render` (`src/jsMain/kotlin/graph/Commit.kt`) creates
  `components.Line(parent.upperDock, child.lowerDock)`; dock points in `CommitCircle`.
- Known limit: edges can still cross where the current lane layout places lanes apart (e.g.
  after a rebase shift); lane compaction is a separate topic.
- If the manual shows graph screenshots, check `src/docs/asciidoc/manual.adoc`.
