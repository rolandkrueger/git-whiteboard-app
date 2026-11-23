---
id: git-canvas-sz7p8
title: Pull as fetch plus merge (fast-forward or merge commit)
status: doing
type: feature
context: git-canvas
created: 2026-11-23
completed:
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
- [ ] Clone, commit in A, Pull in B: fast-forward; local `main` and `origin/main` on the same commit; no merge commit.
- [ ] Diverged history (commits in A and B), Pull in B: a merge commit with two parents is created.
- [ ] Pull when up to date shows "Already up to date" and changes nothing.
- [ ] End to end: push rejected -> Pull -> Push succeeds.
- [ ] Pull on a branch without upstream is refused with a message.
- [ ] Share URL, reload after a pull: state matches.
- [ ] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Merge currently looks up its source in `branches`; add a lookup for remote branches.
