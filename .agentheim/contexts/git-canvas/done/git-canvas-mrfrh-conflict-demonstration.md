---
id: git-canvas-mrfrh
title: Conflict marker on commits (set, render, share, transfer)
status: done
type: feature
context: git-canvas
created: 2026-11-23
completed: 2026-11-25
depends_on: [git-canvas-ktja3]
blocks: [git-canvas-c35ww, git-canvas-nymhr]
tags: [conflict, merge, rebase]
related_adrs: [0006, 0005]
related_research: []
prior_art: [git-canvas-b5f0r, git-canvas-wv6va, git-canvas-nywqs]
---

## Why
The vision asks for conflict demonstrations. The builder decided (2026-11-25) on a lean variant:
not how a concrete conflict is resolved, only what happens in the graph when one occurs. That needs
a way to say "this commit changes the shared spot" without any file model (ADR 0006). This task
delivers the marker itself; the merge/pull pause (git-canvas-c35ww) and the rebase pause
(git-canvas-nymhr) build on it.

## What
- A commit carries a boolean `conflicting` marker ("conflicting change": it touches the one shared
  imaginary spot, ADR 0006 decision 1).
- **Setting it:** in the Commits tab, a checkbox `Conflicting change` next to `Add commit` marks the
  newly created commit (and `Amend commit` keeps/sets it per the checkbox). In addition a small
  input + button `Toggle conflict marker` (commit id) toggles the marker on any existing commit of
  the active repo.
- **Rendering:** a marked commit shows a small warning badge (e.g. a red/orange lightning or `!`
  circle) attached to its commit circle, legible on a projector and consistent with the
  active/inactive fading; it moves with the commit (rebuild, re-layout).
- **Persistence and transfer:** `CommitSnapshot.conflicting` (default false on decode, old Share
  URLs still open); clone (`buildCloneSnapshot`), push and fetch (`receiveCommits` /
  `CommitDescription`) carry the marker, so the same commit id is marked in every repo. Cherry-pick
  and rebase copies keep the marker.
- No change to merge/rebase behavior in this task.

## Acceptance criteria
- [ ] With the `Conflicting change` checkbox set, `Add commit` creates a commit with the marker; without it, no marker; the checkbox does not reset other inputs.
- [ ] `Toggle conflict marker` with an existing commit id toggles the marker on that commit in the active repo; an unknown id shows the existing "commit does not exist" style message and changes nothing.
- [ ] A marked commit renders a warning badge on its circle; unmarked commits look exactly as today. The badge follows the commit when the region is rebuilt (triangle move, growth re-layout) and is faded like the rest of an inactive region.
- [ ] Share URL round-trip keeps every marker; a Share URL created before this change still opens (markers default to false).
- [ ] Clone copies markers; push and fetch transfer them (the received commit with the same id is marked); cherry-pick and rebase copies keep the marker.
- [x] Manual (Commits section) and git-canvas README ubiquitous language ("Conflicting change" / conflict marker) describe the marker; the README links ADR 0006.
- [x] Full verification build passes (`sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`); no new browser console errors. (build verified; browser console not checked)
- [ ] On a projector-sized view, a marked commit is recognizable at a glance. [human-eye]

## Notes
- ADR 0006 is the contract for all three conflict tasks; do not implement the pause here.
- `CommitSnapshot` is in `scenario/GraphSnapshot.kt`, decoding in `scenario/ScenarioSerializer.kt`;
  `receiveCommits` / `CommitDescription` in `GitGraph.kt` (around line 330-362); clone snapshot in
  `buildCloneSnapshot`.
- Keep the new controls in the existing Commits tab layout (Bulma), DOM-safe APIs only.

## Outcome
Implemented the conflict marker: `Commit.conflicting` with an orange `!` badge in `CommitCircle`, `Conflicting change` checkbox
(Add/Amend commit) and `Toggle conflict marker` control, `CommitSnapshot.conflicting` (default false) in
`GraphSnapshot.kt`/`ScenarioSerializer.kt`, and carry-over in `CommitDescription`/`receiveCommits`, clone (snapshot copy),
cherry-pick and rebase copies. README and manual updated. Full build passes.

Not verified in a browser (criteria left unticked): checkbox/toggle behaviour, badge rendering and fading, Share URL
round-trip and old URLs, clone/push/fetch transfer, cherry-pick/rebase copies, browser console errors, projector legibility.
The Toggle action re-renders the commit; whether the grey lost-commit state stays right was handled in code but not seen live.

## Verifier note (iteration 1)

**REASONS:**
- Acceptance criterion "Toggle conflict marker ... toggles the marker on that commit in the active repo" has a code-level defect: toggling also corrupts the commits after the toggled one. `GitGraph.toggleConflictMarker` (src/jsMain/kotlin/graph/GitGraph.kt:277-282) calls `Commit.setConflicting` (src/jsMain/kotlin/graph/Commit.kt:80-86), which calls `rerender(layer)`. `rerender` follows the `childCommit` chain (Commit.kt:91). Each rerendered child gets a new `CommitCircle`, and a new circle starts with `isLostInReflog = false`. `Commit.isLostInReflog` reads its value from the circle (Commit.kt:28), so the lost flag is wiped. `setConflicting` only saves and restores the lost state of the toggled commit, not of its children. Unlike the rebase path (GitGraph.kt:638-639), it never calls `calculateLostCommits()` afterwards.
- Concrete failure: commits m1-m2-m3 on main, then `Reset` main to m2, so m3 becomes lost and grey, and `m2.childCommit` is still m3. Toggling the marker on m2 (or m1) turns m3 back into a coloured, non-lost commit. If "Show lost commits" is off, m3 also reappears on the canvas, because `render` runs no matter what `showLostCommits` is set to. From then on `getSnapshot` records `isLostInReflog = false` for m3, so the Share URL, the triangle/growth rebuild and `buildCloneSnapshot` all keep it as a reachable commit.
- Same root cause, for the toggled commit itself: toggling a lost commit while lost commits are hidden (`showLostCommits == false`) calls `render` and makes it visible again. Its grey colour is restored, but it stays on the canvas.
- Minor design note (not a FAIL reason on its own): on a lost commit the badge stays orange while the circle turns grey (CommitCircle.isLostInReflog only recolours `circle`).

**SUGGESTED_FIX:** Make `toggleConflictMarker` keep the lost state of every commit it rerenders. For example, after `setConflicting` call `calculateLostCommits()` and then re-apply `showLostCommits(showLostCommits)`, as the rebase path does, or save and restore the lost flags around `rerender`. Also, a hidden lost commit should not be rendered when it is toggled.

**ITERATION_HINT:** likely-fixable


### Iteration 2 fix

Toggling the marker now recomputes lost-in-reflog state (`calculateLostCommits()`) and re-applies `showLostCommits(...)`, so rerendered commits keep their lost/hidden state. The badge fades to grey on lost commits (`CommitCircle.setColor`).
