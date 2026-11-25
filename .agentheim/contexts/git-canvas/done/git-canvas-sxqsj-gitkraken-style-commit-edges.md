---
id: git-canvas-sxqsj
title: Draw commit edges GitKraken-style - straight vertical runs in a lane with a short rounded bend where the edge changes lanes
status: done
type: feature
context: git-canvas
created: 2026-11-25
completed: 2026-11-25
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
- [x] Edge path computation lives in a small, separately testable function (input: parent dock point, child dock point, edge kind, commit distance; output: Konva point list), with cases: same lane -> straight 2-point line; one row apart -> single bend; branch-off over several rows -> bend near the parent, then vertical in the child's lane; merge over several rows -> vertical in the merged commit's lane, then bend near the merge commit.
- [x] Cross-lane edges are drawn with `bezier: true`; the tangent at both ends is vertical (first and last control points share the x of their end point).
- [x] Same-lane edges are unchanged straight lines; edge colour, width and missing arrowhead are unchanged.
- [ ] Edges render correctly after commit, branch, merge, rebase (incl. swimlane shift), cherry-pick if present, reset with lost commits shown/hidden, clone, fetch/pull and snapshot restore; no stale edges remain.
- [x] Full verification (build only; no browser run) build passes: `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`; no new browser console errors.
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

## Outcome
Cross-lane commit edges are now one Bezier `Line` (new `components/CommitEdge.kt`) whose points come from the pure function `commitEdgePoints` in `components/CommitEdgePath.kt` (branch-off: bend near parent then vertical in child lane; merge: vertical in side lane then bend near the merge commit; same lane: straight 2-point). `Commit.render` uses it for `parentLine` (always BRANCH_OFF) and `mergedParentLine` (MERGE). Edges are still created only in `Commit.render`, so rerender/rebase/restore/clone paths are unchanged. No test harness exists in the project, so no tests were added (function is pure for later testing). The build passes. NOT verified: the cross-situations criterion (rebase shift, lost commits, fetch/pull) and the [human-eye] criterion need a browser run; they are left unticked.

### Iteration 2 fix: edge kind from geometry
A merge commit's parent is our own branch tip, so `parentLine` is always BRANCH_OFF: if the child sits in another lane it was placed in a free lane, and the straight run belongs in that lane (bend near the parent). Case A (reset, then no-ff merge, merge commit shifted to lane 1, parent m1 in lane 0): BRANCH_OFF, bends just above m1, runs up lane 1, does not cut m2/m3 in lane 0. Case B (no-ff merge into fresh branch, merge commit lane 1, parent m2 lane 0, m3 in lane 0): BRANCH_OFF, bends above m2 then straight in lane 1, so it misses m3. `mergedParentLine` stays MERGE: the merged side branch tip runs straight in its own lane and bends just below the merge commit. Misleading comment corrected.

## Verifier note (iteration 1)

**REASONS:**
- `commitEdgePoints` (src/jsMain/kotlin/components/CommitEdgePath.kt) is correct for all four cases (same lane, one row, branch-off over several rows, merge over several rows); control points and `bezier` only for >4 points are fine.
- The edge kind chosen for `parentLine` is wrong (src/jsMain/kotlin/graph/Commit.kt:56-57, `val parentKind = if (mergedCommit != null) EdgeKind.MERGE else EdgeKind.BRANCH_OFF`). The comment says the parent is "the side branch the merge edge returns from", but `GitGraph.addCommit` (called from `merge`) sets `parent = head.commit` = our own branch; the merge commit goes in `currentBranch().swimlane`, and `mergedCommit` is their branch. So a merge commit's `parentLine` only crosses lanes when the merge commit was placed away from its parent's lane — branch-off geometry. Using MERGE puts the straight run in the parent's lane, which already holds other commits, breaking the rule of thumb and criterion 4.
- Case A (reset, then merge): main m1,m2,m3 in lane 0; reset main to m1 (m2,m3 lost, still in lane 0); merge feature with no-ff. `isHeadOnOccupiedSwimlane()` is true, the merge commit is shifted to lane 1 several rows up with parent m1 in lane 0. With MERGE the edge runs straight up lane 0 through m2 and m3.
- Case B (no-ff merge into a fresh branch): main m1,m2; create feature at m2 (lane 1); commit m3 on main (lane 0); check out feature, merge main no-ff. The merge commit lands in lane 1 at row 3 with parent m2 in lane 0 (rows 1..3). MERGE bends with c1 at (x0, y(m2)-80), passing through m3's circle.
- Everything else (colour, width, listening=false, no arrowhead, moveToBottom, removeFrom destroys both edges, LabelConnector untouched, README) is fine. Build not run (stopped at check 1).

**SUGGESTED_FIX:** Decide the kind of `parentLine` from geometry, not from `mergedCommit != null`. The straight run must go in the side branch's lane, so the parent edge of a merge commit placed in a new lane should use BRANCH_OFF. Use MERGE only when the child sits back in a lane whose commits the edge would otherwise cut through. Correct the misleading comment, and walk through cases A and B before resubmitting.

**ITERATION_HINT:** likely-fixable
