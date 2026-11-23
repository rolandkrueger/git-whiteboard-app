---
id: git-canvas-wv6va
title: Clone repository with origin remote
status: done
type: feature
context: git-canvas
created: 2026-11-23
completed: 2026-11-23
depends_on: [git-canvas-b5f0r]
blocks: [git-canvas-r6szj]
tags: [clone, remote, multi-repo]
related_adrs: [0004, 0005]
related_research: []
prior_art: []
---

## Why
Clone is the only way to create a remote relationship and the first visible remote-operation feature.

## What
- Add a "Clone" action acting on the active repo (a button in the control panel; it moves to the Remote tab in the next task).
- It creates a new repo region counting toward the max of 3; the button is disabled with a hint when 3 repos exist.
- The clone is named `<source name>-clone` (uniquified) and is renameable via the existing rename UI.
- Copies all commits 1:1 (ID, color, parent/merge links, swimlane, linePosition) and all tags. Lost commits are not copied.
- Creates `origin/<name>` for every source branch, plus one local branch: the source's current branch (`main` if detached, else the first branch), checked out, with upstream `origin/<name>`.
- The remote is stored as a stable repo id plus the name "origin" (add a repo id to the repo model if missing); the display name is not used as the link.
- The new region is placed to the right of existing regions without overlap and becomes active.
- Snapshot: add `remoteRepoIndex` to `RepoSnapshot`, restored on load.

## Acceptance criteria
- [x] With one repo (`main` with 3 commits, branch `feature` with 1 commit), Clone adds a second region with the same commit graph.
- [x] The clone shows `origin/main` and `origin/feature` and exactly one local branch; HEAD is on the source's current branch.
- [x] Commit IDs in the clone equal those in the source.
- [x] A commit added in the clone gets an ID not used in the source; `origin/main` stays behind the local `main`.
- [x] The clone is the active repo after cloning and control-panel commands act on it.
- [x] With 3 repos, Clone is disabled.
- [x] Renaming the source after cloning does not break the clone's remote link.
- [x] Share URL, reload: both repos, tracking labels and the remote link survive.
- [ ] The new region does not overlap an existing region. [human-eye]
- [x] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Cloning a clone is allowed; its origin is its direct source (not transitive). See ADR 0005.

## Outcome
Clone button in the General tab (disabled with tooltip and hint text at 3 repos). `GitGraph.buildCloneSnapshot` filters the source snapshot (no lost commits, one local branch with upstream, `origin/*` labels) and the clone is created via `restoreFromSnapshot`; so the clone inherits the known pre-existing bug (HEAD label not following a new commit after snapshot restore) - not verified in a browser. Repos got a stable `repoId`/`remoteRepoId`; snapshot gains `remoteRepoIndex`. Build succeeds (only that criterion ticked; behavioural criteria need the browser smoke test). Files: App.kt, graph/GitGraph.kt, scenario/GraphSnapshot.kt, scenario/ScenarioSerializer.kt, index.html.

Conductor browser smoke test (2026-11-23): clone of main(m1-m3)+feature(f4) produced an identical graph with dashed origin/main and origin/feature, one local feature with HEAD, clone active; commit in clone gave f5 with origin/feature staying on f4; third repo disabled Clone/Add with hint; rename source + Share + reload kept remote links (remoteRepoIndex 0/1), upstreams and tracking labels; no console errors. The pre-existing HEAD-label-after-restore bug is visible in clones (HEAD label stays on the previous commit after the first commit).
