---
id: git-canvas-sz7p8
title: Pull as fetch plus merge (fast-forward or merge commit)
status: done
type: feature
context: git-canvas
created: 2026-11-23
completed: 2026-11-23
depends_on: [git-canvas-w3ndg, git-canvas-rx1y3]
blocks: [git-canvas-ktja3]
tags: [pull, merge, remote]
related_adrs: [0005]
related_research: []
prior_art: []
---

## Why
Pull is the most common sync command, and the demo must make its fetch-then-merge nature visible.

## What
- Pull button (the "rebase" checkbox stays disabled until the pull --rebase task).
- Pull runs fetch, then merges `origin/<upstream>` into the current branch via the existing `merge()` (fast-forward when possible, else a merge commit).
- `merge()` accepts a tracking ref as source (today it takes a branch id), without exposing tracking refs in dropdowns.
- Message area: "Fast-forward", "Merge made" or "Already up to date".
- Detached HEAD, no upstream or unavailable remote: refused with a message.

## Acceptance criteria
- [x] Clone, commit in A, Pull in B: fast-forward; local `main` and `origin/main` on the same commit; no merge commit.
- [x] Diverged history (commits in A and B), Pull in B: a merge commit with two parents is created.
- [x] Pull when up to date shows "Already up to date" and changes nothing.
- [x] End to end: push rejected -> Pull -> Push succeeds.
- [x] Pull on a branch without upstream is refused with a message.
- [x] Share URL, reload after a pull: state matches.
- [x] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Merge currently looks up its source in `branches`; add a lookup for remote branches.

## Outcome
Added `RemoteOps.pull` (fetch, then `GitGraph.merge(false, "origin/<upstream>")`; merge now falls back to tracking refs for its source) and enabled `pullButton` in `updateRemoteTab` when remote + attached HEAD + upstream exist; refusals for detached HEAD, no upstream, unavailable remote. Build succeeds. Behavioural criteria are not browser-verified (left unticked; conductor smoke-tests). In cloned/restored repos the known HEAD-label issue may show HEAD in the wrong place after a fast-forward/merge.
Files: src/jsMain/kotlin/graph/RemoteOps.kt, graph/GitGraph.kt, App.kt.

Conductor browser smoke test (2026-11-23): fast-forward pull moved main, HEAD and origin/main to m2; repeat pull 'Already up to date'; diverged history: push rejected, pull created merge commit m4m3 ('Merge made'), push then succeeded and the remote received m4 and m4m3; Share + reload matched; no new console errors. Branch without upstream: Pull button disabled with 'no upstream' shown (code also refuses with a message).
