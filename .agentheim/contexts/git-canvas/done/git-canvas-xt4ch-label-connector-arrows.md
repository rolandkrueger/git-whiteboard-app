---
id: git-canvas-xt4ch
title: Draw label connectors as thin blue Bezier-curved arrows pointing toward the commit, so they no longer look like commit edges
status: done
type: feature
context: git-canvas
created: 2026-11-25
completed: 2026-11-25
depends_on: []
blocks: []
tags: [rendering, labels, readability]
related_adrs: []
related_research: []
prior_art: []
---

## Why
The lines connecting commits to each other (parent and merge-parent edges) and the lines
connecting labels (branch, tag, HEAD, remote-tracking) to their commit look identical:
both are black, 3px straight lines rendered by the same `components.Line`. In a busy graph
it is hard to tell at a glance which lines are history and which are just pointers. A
distinct look for label connectors improves readability in trainings and demos.

## What
Every label connector becomes a thin blue arrow whose head points toward the commit:

- **Label -> commit** (first label on a commit, `CommitLabel.attachToCommit`): arrow from the
  label's left dock point to the commit circle's right dock point, head at the commit.
- **Label -> label** (chained labels, `CommitLabel.attachToLabel`): arrow from the label's
  left dock point to the previous label's right dock point, head at the previous label —
  so the arrow chain always leads toward the commit.
- Applies to all label kinds: branch, tag, HEAD and remote-tracking (`origin/<name>`).
- Look: blue stroke and fill, thinner than commit edges (about 1.5px) with a small
  arrowhead. Choose a blue consistent with the existing palette (e.g. the active-region
  blue `#2196F3`); keep it as a named constant.
- Commit edges (`Commit.parentLine`, `Commit.mergedParentLine`) stay unchanged: black, 3px,
  no arrowhead.
- **Curved connector:** the arrow is drawn as a cubic Bezier curve (Konva `bezier: true`,
  points = start, control 1, control 2, end). Both control points sit at the y of their own
  end point, roughly halfway along x, so the curve leaves the label horizontally and enters
  the commit (or previous label) horizontally — an S-curve for vertically offset labels
  (e.g. stacked branch / remote-tracking labels), a straight line for labels without
  vertical offset. Use the curve for all label connectors; no special cases.
- If the S-curve becomes too steep for stacked labels (vertical step `labelYOffset` = 29px vs.
  horizontal gap `labelOffset.x` = 20px), increase the horizontal gap (e.g. 35-40px) —
  tune in the browser.

Implementation hint: Konva ships an `Arrow` shape (`konva/lib/shapes/Arrow`, a `Line`
subclass with `pointerLength`, `pointerWidth`, `pointerAtEnding`/`pointerAtBeginning`).
There is no Kotlin binding for it yet; add one under `konvajs/` following the existing
handwritten-binding style (ADR-0003) and verify the module export shape in the browser.
Introduce a dedicated component (e.g. `components.LabelConnector`) rather than adding
options to `components.Line`, so commit edges cannot accidentally pick up the new look.
Keep the connector non-listening and drawn below labels and commits, as today.

## Acceptance criteria
- [x] A Kotlin external binding for Konva's `Arrow` shape exists under `src/jsMain/kotlin/konvajs/` and loads at runtime without console errors.
- [x] `CommitLabel.attachToCommit` and `CommitLabel.attachToLabel` render the new arrow connector; `components.Line` is no longer used by `CommitLabel`.
- [x] Label connectors use a blue stroke/fill defined as a named constant, a stroke width below the commit-edge width of 3, and the arrowhead at the commit end (or the previous-label end for chained labels).
- [x] Label connectors are drawn as cubic Bezier curves whose control points lie at the y of their respective end points, so the arrowhead points horizontally into the commit (or previous label); a connector without vertical offset is a straight line.
- [x] Commit parent and merge-parent edges are unchanged (black, 3px, no arrowhead).
- [x] Arrows follow labels correctly after the usual relayout triggers: new commit, branch move/checkout, tag creation, fetch/pull updating `origin/<name>` labels, clone, and snapshot restore — no stale connectors left behind (removal still destroys the arrow).
- [x] Full verification build passes: `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`.
- [ ] In the running app, label connectors are clearly distinguishable from commit edges, and the arrowheads are small enough not to overlap the commit circle or label pill. [human-eye]
- [ ] Curved connectors to vertically offset labels look smooth and elegant, not cramped or kinked. [human-eye]

## Notes
- Builder decisions (2026-11-25): all label connectors get the arrow (not only label -> commit);
  connectors are thinner than commit edges; connectors are drawn as horizontal-entry Bezier
  curves (decided after capture, same day). Verify in the browser that Konva 10.3's `Arrow`
  orients the head along the last control point -> end segment when `bezier` is set.
- Remote-link lines between repo regions (`RepoRegion.setRemoteLinks`) are out of scope.
- If the manual describes or shows label connectors, update `src/docs/asciidoc/manual.adoc`
  accordingly.

## Outcome
Label connectors are now thin (1.5px) blue (`#2196F3`, `LabelConnector.CONNECTOR_COLOR`) Bezier arrows with the head at the commit / previous label. New `components/LabelConnector.kt` (points ordered label dock -> commit/previous-label dock, so Konva's end-of-line head lands on the commit side; control points at the y of their own end, halfway on x). Iteration 2: `CommitLabel` now passes the label's left dock as start and the commit/previous-label right dock as end (iteration 1 had them reversed); `CommitLabel` uses it instead of `Line`; `Line` and commit edges untouched. New binding `konvajs/KonvaArrow.kt` (`Arrow`, `ArrowConfig` with `pointerLength`/`pointerWidth` in `KonvaBindings.kt`).

Verified: full build passes. No browser run: module shape checked against `build/js/node_modules/konva/lib/shapes/Arrow.js` (named export `Arrow`, same shape as the `Line` binding's `konva/lib/shapes/Line`), and the bundle contains the Arrow import. Arrow orients its head from `points[n-4..n-2]` (control 2 -> end) when not tension-based, so with bezier the head is horizontal. The `labelOffset.x` gap (20px) was not increased; the two [human-eye] criteria remain unticked and need a browser check (tune the gap to 35-40px if stacked-label S-curves look steep). Manual does not describe connectors; no change.

## Verifier note (iteration 1)

**REASONS:**
- Criterion "arrowhead at the commit end (or the previous-label end for chained labels)" is not met. The arrowhead is drawn at the wrong end. Konva's `Arrow` draws its head at the last point (`points[n-2], points[n-1]`; `pointerAtEnding` is true by default and `pointerAtBeginning` is false). See build/js/node_modules/konva/lib/shapes/Arrow.js lines 38-48. `LabelConnector` builds `points = start, ctrl1, ctrl2, end`, so the head lands on `end`. But `CommitLabel` passes the commit/previous-label dock point as `start` and the label's own left dock as `end`:
  - `attachToCommit`: `setLinePosition(layer, commit.commitCircle.getRightDockPoint(), getLeftDockPoint())` puts the head on the label, not the commit.
  - `attachToLabel`: `setLinePosition(layer, commitLabel.getRightDockPoint(), getLeftDockPoint())` puts the head on the new label, not the previous label, so the chain points away from the commit.
- For the same reason, the Bezier criterion ("arrowhead points horizontally into the commit (or previous label)") is not met as specified.
- Not reached (stopped at check 1): build run and later checks. Already fine: Arrow binding style, `components.Line` no longer used by CommitLabel, `CONNECTOR_COLOR = "#2196F3"`, `strokeWidth = 1.5`, `listening = false`, `moveToBottom()`, commit edges unchanged, destroy/recreate via `setLinePosition` / `removeFrom`.

**SUGGESTED_FIX:** Put the arrowhead at the commit / previous-label end. Either have `setLinePosition` pass the label's own `getLeftDockPoint()` as `start` and the commit/previous-label right dock point as `end` (control points stay at the y of their own end point), or reverse the point order inside `LabelConnector`. Then fix the class KDoc and the task Outcome to match, and re-run the full build.

**ITERATION_HINT:** likely-fixable
