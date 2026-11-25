---
id: git-canvas-xt4ch
title: Draw label connectors as thin blue arrows pointing toward the commit, so they no longer look like commit edges
status: todo
type: feature
context: git-canvas
created: 2026-11-25
completed:
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

Implementation hint: Konva ships an `Arrow` shape (`konva/lib/shapes/Arrow`, a `Line`
subclass with `pointerLength`, `pointerWidth`, `pointerAtEnding`/`pointerAtBeginning`).
There is no Kotlin binding for it yet; add one under `konvajs/` following the existing
handwritten-binding style (ADR-0003) and verify the module export shape in the browser.
Introduce a dedicated component (e.g. `components.LabelConnector`) rather than adding
options to `components.Line`, so commit edges cannot accidentally pick up the new look.
Keep the connector non-listening and drawn below labels and commits, as today.

## Acceptance criteria
- [ ] A Kotlin external binding for Konva's `Arrow` shape exists under `src/jsMain/kotlin/konvajs/` and loads at runtime without console errors.
- [ ] `CommitLabel.attachToCommit` and `CommitLabel.attachToLabel` render the new arrow connector; `components.Line` is no longer used by `CommitLabel`.
- [ ] Label connectors use a blue stroke/fill defined as a named constant, a stroke width below the commit-edge width of 3, and the arrowhead at the commit end (or the previous-label end for chained labels).
- [ ] Commit parent and merge-parent edges are unchanged (black, 3px, no arrowhead).
- [ ] Arrows follow labels correctly after the usual relayout triggers: new commit, branch move/checkout, tag creation, fetch/pull updating `origin/<name>` labels, clone, and snapshot restore — no stale connectors left behind (removal still destroys the arrow).
- [ ] Full verification build passes: `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`.
- [ ] In the running app, label connectors are clearly distinguishable from commit edges, and the arrowheads are small enough not to overlap the commit circle or label pill. [human-eye]

## Notes
- Builder decisions (2026-11-25): all label connectors get the arrow (not only label -> commit);
  connectors are thinner than commit edges.
- Remote-link lines between repo regions (`RepoRegion.setRemoteLinks`) are out of scope.
- If the manual describes or shows label connectors, update `src/docs/asciidoc/manual.adoc`
  accordingly.
