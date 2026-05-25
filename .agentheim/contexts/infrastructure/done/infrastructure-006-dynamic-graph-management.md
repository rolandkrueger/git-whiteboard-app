---
id: infrastructure-006
title: Dynamic graph management — start with one, add up to two more
status: done
type: feature
context: infrastructure
created: 2026-05-23
completed: 2026-05-23
commit: 044454f
depends_on: []
blocks: []
tags: [multi-repo, graph-lifecycle, dynamic, initialization, add-graph]
related_adrs: ["0001", "0003"]
related_research: []
prior_art: [infrastructure-001, infrastructure-004, infrastructure-005]
---

## Why

The app currently initialises two hardcoded graphs ("origin" and "alice") every time. Trainers often need only one graph to start a session and want to introduce additional repos (alice, bob, …) at the moment that topic comes up in the training — not before. Hardcoding two graphs forces the trainer to explain the second repo before it is relevant.

## What

Replace the two hardcoded GitGraph instances with a single dynamic graph (default name "origin"). A new "Add Repository" button lets the trainer add up to two more graphs during a session, each named via a prompt (like the existing create-branch dialog). Maximum three graphs total.

## Acceptance criteria

- [ ] On startup with no `#state=` fragment: exactly one graph named "origin" is rendered on the canvas.
- [ ] A clearly labelled "Add Repository" button is visible in the control panel. It is disabled (or hidden) once three graphs exist.
- [ ] Clicking "Add Repository" shows a name-input dialog (same pattern as create branch). The name must be non-empty and not already in use; the button is only enabled when the input is valid.
- [ ] Confirming the dialog adds a new GitGraph to the canvas, positioned at a fixed horizontal offset to the right of the last existing graph (same `repoSeparation` constant as before). The new graph starts with an empty canvas (no initial commit yet — the trainer uses "Add Commit" to create the first commit manually).
- [ ] The click-to-focus mechanism works correctly across all dynamically added graphs.
- [ ] All control panel operations (add commit, checkout, branch, tag, reset, merge, …) route to the currently active graph regardless of how many graphs exist.
- [ ] The Share button encodes all existing graphs (1, 2, or 3) and URL-restore correctly brings back whichever graphs were present. (The `MultiRepoSnapshot.repos` list already supports variable length — verify the round-trip still works.)
- [ ] Production webpack build passes cleanly.

## Notes

### Current initialisation to replace

`App.kt` currently creates two named vals:
```kotlin
val originGraph = GitGraph(originLayer, repoName = "origin", config = originConfig)
val aliceGraph  = GitGraph(aliceLayer,  repoName = "alice",  config = aliceConfig)
```
and routes clicks/operations to them by name. These must become a `MutableList<GitGraph>`.

### Canvas layer management

Each GitGraph owns one Konva `Layer`. When a graph is added dynamically, a new Layer must be created and added to the Stage (`stage.add(newLayer)`). Layer z-order should be preserved (graphs added later render on top — or choose a consistent policy).

### Name default on startup

The first graph defaults to "origin" — no prompt on startup. A name other than "origin" can only come from a restored `#state=` URL fragment (the first repo's `repoName` field).

### Add-graph dialog

Re-use or mirror the existing branch-name input pattern (`ui/` helpers). Validation: non-empty, not already used by another graph in the current session. No default value in the input field (name is required).

### Maximum graph count

Cap at 3. Once 3 graphs exist, disable the "Add Repository" button. No graph removal in this task.

### Control panel routing

The "active graph" variable (`activeGraph`) becomes a pointer into the dynamic list. The `containsPoint` dispatch loop must iterate the list instead of checking each graph by name.

### Share URL compatibility

`MultiRepoSnapshot.repos` is already a `List<RepoSnapshot>` with no fixed length. `App.kt` must build the snapshot list from the dynamic graph list rather than the hardcoded pair. Verify round-trip for 1, 2, and 3 graphs.

## Outcome

Replaced the hardcoded two-graph initialisation with a dynamic graph list starting with a single "origin" graph.

Key changes:
- `App.kt`: `val graphs = mutableListOf<GitGraph>()` replaces `originGraph`/`aliceGraph` vals. On startup (no hash), one graph named "origin" is created and `initGraph()` called. URL restore creates as many graphs as the snapshot contains. Click-to-focus loops over `graphs`. Share button maps all graphs to snapshots.
- `App.kt`: "Add Repository" button handler shows a Bulma modal dialog (re-using existing dialog patterns). Validates name (non-empty, not duplicate), creates a new Konva Layer, adds it to the Stage, and creates a GitGraph via `renderEmptyRegion()` — no initial commit. Button is disabled when 3 graphs exist.
- `index.html`: Added `addRepoButton` in the General tab, and an `addRepoDialog` modal for the name input.
- `GitGraph.kt`: Added `renderEmptyRegion()` method (renders region border/label only, no initial commit). Added `xOffset` read-only property so App.kt can compute placement offsets without accessing `config` directly.

Production webpack build passes cleanly.
