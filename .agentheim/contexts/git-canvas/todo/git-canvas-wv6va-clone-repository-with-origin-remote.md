---
id: git-canvas-wv6va
title: Clone repository with origin remote
status: todo
type: feature
context: git-canvas
created: 2026-11-23
completed:
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
- [ ] With one repo (`main` with 3 commits, branch `feature` with 1 commit), Clone adds a second region with the same commit graph.
- [ ] The clone shows `origin/main` and `origin/feature` and exactly one local branch; HEAD is on the source's current branch.
- [ ] Commit IDs in the clone equal those in the source.
- [ ] A commit added in the clone gets an ID not used in the source; `origin/main` stays behind the local `main`.
- [ ] The clone is the active repo after cloning and control-panel commands act on it.
- [ ] With 3 repos, Clone is disabled.
- [ ] Renaming the source after cloning does not break the clone's remote link.
- [ ] Share URL, reload: both repos, tracking labels and the remote link survive.
- [ ] The new region does not overlap an existing region. [human-eye]
- [ ] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Cloning a clone is allowed; its origin is its direct source (not transitive). See ADR 0005.
