---
id: git-canvas-w3ndg
title: Push with fast-forward and visible rejection
status: todo
type: feature
context: git-canvas
created: 2026-11-23
completed:
depends_on: [git-canvas-r6szj]
blocks: [git-canvas-sz7p8]
tags: [push, remote]
related_adrs: [0004, 0005]
related_research: []
prior_art: []
---

## Why
Push is the headline demo, and the trainer must be able to show why Git rejects a non-fast-forward push.

## What
- Push button on the Remote tab, acting on the current branch's upstream.
- Accepted if the remote branch is missing or its tip is an ancestor of (or equal to) the local tip: missing commits are created in the remote repo in topological order, keeping ID, color and parents; layout is decided by the receiving graph (new linePosition, lane of the receiving branch, `shiftCommitsToTheRight` on collision — ADR 0005).
- The remote branch moves to the local tip (created if missing); if it is checked out in the remote, the remote HEAD follows. The local `origin/<name>` label updates.
- A branch without upstream is pushed to `origin/<same name>` and gets that upstream.
- Non-fast-forward: rejected with "rejected: non-fast-forward - fetch first" in the message area plus a short (~1.5 s) highlight on the remote region or branch label; nothing changes.
- Up to date: "Everything up-to-date". Remote missing/cleared: "remote not available". Detached HEAD: refused with a message.
- No force push.

## Acceptance criteria
- [ ] Clone A to B, commit twice in B, Push: both commits appear in A with the same IDs; A's `main` and B's `origin/main` move to the new tip.
- [ ] Pushed commits in A sit on `main`'s lane, in order, without overlapping existing commits. [human-eye]
- [ ] Pushing a new local branch `feature` creates `feature` in A and `origin/feature` in B and sets the upstream.
- [ ] Commit on `main` in A and in B, then Push from B: rejected with the visible message; no commits or labels change in either repo.
- [ ] Pushing when up to date shows "Everything up-to-date".
- [ ] If A has `main` checked out, A's HEAD moves with the pushed branch.
- [ ] Share URL, reload after a push: state matches.
- [ ] The rejection message is noticeable for the audience without opening dev tools. [human-eye]
- [ ] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Put cross-repo logic in a small `RemoteOps` helper called from `App.kt` with two `GitGraph` references. The receiving side needs a public, generic `receiveCommits(...)` on `GitGraph` that fetch reuses; `shiftCommitsToTheRight` and the counters are private today.
