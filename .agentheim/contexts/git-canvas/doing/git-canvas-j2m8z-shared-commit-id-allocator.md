---
id: git-canvas-j2m8z
title: Shared commit ID allocator across repos
status: doing
type: feature
context: git-canvas
created: 2026-11-23
completed:
depends_on: []
blocks: [git-canvas-b5f0r]
tags: [commit-id, multi-repo, scenario]
related_adrs: [0004]
related_research: []
prior_art: []
---

## Why
Remote operations need "same ID == same commit" across repos. Today every repo mints its own IDs from a per-branch counter, so two repos both produce e.g. `m2` for different commits.

## What
- Add an app-wide `CommitIdAllocator`, created in `App.kt` and passed to every `GitGraph`.
- New commit ID = `<branch first letter><allocator.next()>`; the per-branch `counter` is no longer used for ID generation.
- The first repo's initial commit stays `m1`; initial commits of later blank repos take `m<next>`.
- `globalCommitNumber` (the per-graph linePosition counter) stays per graph.
- Rebased `<id>*` naming is unchanged.
- Snapshot: add `nextCommitId` to `MultiRepoSnapshot` and bump the snapshot version to 2. Restore re-seeds the allocator. Version-1 URLs still load; the allocator then starts at max numeric suffix of all commit IDs + 1.

## Acceptance criteria
- [ ] Fresh app: initial commit is `m1`; adding commits on `main` and then on a new branch yields strictly increasing numbers (e.g. `m2`, `m3`, `f4`).
- [ ] After "Add repository", the new repo's initial commit has an ID that appears in no other repo.
- [ ] Commits created in repo B continue the numbering of repo A (no duplicate IDs across repos in any sequence of adds).
- [ ] Share URL, reload, add a commit: the new ID does not collide with any restored ID.
- [ ] A Share URL generated before this change (snapshot version 1) still opens and shows the same graph.
- [ ] Merge and rebase still work; rebased commits show `<id>*`.
- [ ] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Touch points: `calcCommitId`, `initGraph`, `addBranch`'s counter, `getSnapshot`/`restoreFromSnapshot`, `scenario/GraphSnapshot.kt`, `scenario/ScenarioSerializer.kt`. See ADR 0004.
