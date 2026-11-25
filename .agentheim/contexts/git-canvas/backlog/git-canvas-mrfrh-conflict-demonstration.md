---
id: git-canvas-mrfrh
title: Conflict marker on commits (set, render, share, transfer)
status: backlog
type: feature
context: git-canvas
created: 2026-11-23
completed:
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
- [ ] Manual (Commits section) and git-canvas README ubiquitous language ("Conflicting change" / conflict marker) describe the marker; the README links ADR 0006.
- [ ] Full verification build passes (`sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`); no new browser console errors.
- [ ] On a projector-sized view, a marked commit is recognizable at a glance. [human-eye]

## Notes
- ADR 0006 is the contract for all three conflict tasks; do not implement the pause here.
- `CommitSnapshot` is in `scenario/GraphSnapshot.kt`, decoding in `scenario/ScenarioSerializer.kt`;
  `receiveCommits` / `CommitDescription` in `GitGraph.kt` (around line 330-362); clone snapshot in
  `buildCloneSnapshot`.
- Keep the new controls in the existing Commits tab layout (Bulma), DOM-safe APIs only.
