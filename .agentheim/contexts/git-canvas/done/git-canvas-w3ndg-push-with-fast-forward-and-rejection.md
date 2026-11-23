---
id: git-canvas-w3ndg
title: Push with fast-forward and visible rejection
status: done
type: feature
context: git-canvas
created: 2026-11-23
completed: 2026-11-23
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
- [x] Clone A to B, commit twice in B, Push: both commits appear in A with the same IDs; A's `main` and B's `origin/main` move to the new tip.
- [ ] Pushed commits in A sit on `main`'s lane, in order, without overlapping existing commits. [human-eye]
- [x] Pushing a new local branch `feature` creates `feature` in A and `origin/feature` in B and sets the upstream.
- [x] Commit on `main` in A and in B, then Push from B: rejected with the visible message; no commits or labels change in either repo.
- [x] Pushing when up to date shows "Everything up-to-date".
- [x] If A has `main` checked out, A's HEAD moves with the pushed branch.
- [x] Share URL, reload after a push: state matches.
- [ ] The rejection message is noticeable for the audience without opening dev tools. [human-eye]
- [x] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Put cross-repo logic in a small `RemoteOps` helper called from `App.kt` with two `GitGraph` references. The receiving side needs a public, generic `receiveCommits(...)` on `GitGraph` that fetch reuses; `shiftCommitsToTheRight` and the counters are private today.

## Outcome
Implemented push. `GitGraph` gained generic `receiveCommits` (parents first, receiver-chosen lane/linePosition, shift on collision), `describeMissingCommits`, `ancestorIds`, ref primitives (`moveBranchTo`, `createBranchAt`, upstream accessors) and `flashAlert` (red region border 1.5 s). `RemoteOps.push` (graph/RemoteOps.kt) holds the cross-repo logic; App.kt enables the Push button (remote present + attached HEAD) and shows the result in `remoteMessage` (is-danger/is-success/is-info). Build succeeds; behavioural criteria are unverified (no browser) and left unticked for the conductor's smoke test. Limitation: all pushed commits land on one lane (merge histories are not split across lanes). Pre-existing HEAD-label bug after restore may affect how HEAD looks in restored repos.

Iteration 2 fix: a remote cleared via "Clear graph" now counts as not available. `GitGraph.wasCleared` is set by `clearGraph()` (not persisted, so a Share/reload restore treats the repo as normal again); `RemoteOps.isAvailable(remote)` is the single availability check (null or cleared), reusable by the upcoming fetch task. Push stays clickable and reports "remote not available" with no red flash and no state change; the clone keeps its link and tracking labels (ADR 0005); the Remote tab shows "origin: <name> (not available)". Minor `createBranchAt` first-letter uniqueness bypass left as is (not blocking).

## Verifier note (iteration 1)

**REASONS:**
- Pushing to a cleared remote does not show "remote not available". `RemoteOps.push` only checks `remote == null`; the push handler in `App.kt` resolves the remote by `remoteRepoId`. `GitGraph.clearGraph()` keeps the same instance and `repoId`, and nothing clears the clone's `remoteRepoId`, so the cleared repo is still found; `initGraph()` gives it a fresh root commit the clone does not have, so the push falls into the non-fast-forward branch and shows "rejected: non-fast-forward - fetch first" (flashing the cleared repo red). Wrong message, misleading for the audience. Contradicts the task's What and ADR 0005 Consequences.
- Removed-remote case is moot (no repo-removal feature). Detached-HEAD refusal is correct. Everything else checks out.
- Minor (not a fail reason): `createBranchAt` bypasses the first-letter uniqueness rule (`isBranchNameValid`); pushing `feature` into a repo that already has `fix` creates two branches starting with "f".

**SUGGESTED_FIX:** Make a cleared remote count as "not available" — e.g. when `clearGraph` runs from the Clear button in `App.kt`, set `remoteRepoId = null` on every graph whose `remoteRepoId` equals the cleared repo's `repoId` (tracking labels stay, per ADR 0005); or have `RemoteOps.push` detect the cleared state and return "remote not available" before the fast-forward check. Then smoke-test: clone, clear the source, push from the clone.

**ITERATION_HINT:** likely-fixable

Conductor browser smoke test (2026-11-23): fast-forward push of two commits (IDs kept, remote main + HEAD moved, origin/main moved), 'Everything up-to-date' on repeat, new branch push created feature + origin/feature + upstream, non-fast-forward push rejected with red message and red region flash and no state change, Share + reload restored state; iteration 2: cleared remote shows '(not available)' and push reports 'remote not available'.
