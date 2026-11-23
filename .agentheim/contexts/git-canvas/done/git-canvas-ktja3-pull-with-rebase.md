---
id: git-canvas-ktja3
title: Pull with rebase option
status: done
type: feature
context: git-canvas
created: 2026-11-23
completed: 2026-11-23
depends_on: [git-canvas-sz7p8]
blocks: [git-canvas-bdecz, git-canvas-mrfrh]
tags: [pull, rebase, remote]
related_adrs: [0005]
related_research: []
prior_art: []
---

## Why
Trainers regularly contrast merge versus rebase pulls and the resulting linear history.

## What
- Enable the "rebase" checkbox; Pull with it set runs fetch, then rebases the current branch onto `origin/<upstream>` via the existing `rebase()`.
- `rebase()` accepts a tracking ref as target.
- Message area: "Rebased", "Fast-forward" or "Already up to date".
- Rebased commits get the usual `<id>*` IDs; the originals become lost commits.

## Acceptance criteria
- [x] Diverged history: Pull with rebase yields linear history; local commits are re-created as `<id>*` on top of `origin/<name>`; no merge commit.
- [x] The original local commits appear when "Show lost commits" is on.
- [x] Pull with rebase when only behind fast-forwards.
- [x] Pull with rebase when up to date shows "Already up to date".
- [x] After a rebase pull, Push is accepted when origin has not moved on.
- [x] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Reuse the existing rebase code; keep changes narrow because `rebase()` is tightly coupled to rendering.

## Outcome
Pull with the "rebase" checkbox now fetches, then rebases the current branch onto `origin/<upstream>` via `rebase()` (which gained the same tracking-ref fallback lookup as `merge`). Messages: "Fast-forward", "Rebased", "Already up to date". Checkbox is enabled together with the Pull button. Files: `graph/GitGraph.kt`, `graph/RemoteOps.kt`, `App.kt`.
Build `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor` succeeds. Behavioural criteria were not verified in a browser (no browser available to the worker); left unticked for the conductor's smoke test.

## Verifier note (iteration 1)

**REASONS:**
- Check 5 (BC README sync): the Pull entry in the git-canvas README's Ubiquitous language section (line ~25) still says "Rebase option still disabled" and describes Pull only as merging via `GitGraph.merge` with messages `Fast-forward`, `Merge made`, `Already up to date`. The diff enables the rebase checkbox, adds a rebase path reporting `Rebased`, and lets `GitGraph.rebase` accept tracking refs. Worker reported `BC_README_UPDATED: no`, so the README contradicts the code.
- Everything else checks out (build exit 0; non-rebase pull unchanged; checkbox enable tied to canPull; rebase() fallback lookup only, no dropdown exposure; `<id>*` naming kept, no allocator number consumed; behaviour covered by the conductor's smoke test).

**SUGGESTED_FIX:** Update only the Pull entry in the git-canvas README: Pull either merges (`GitGraph.merge`) or, with the rebase checkbox set, rebases the current branch onto its `origin/<name>` via `GitGraph.rebase` (which also accepts tracking refs that are not listed in dropdowns); add `Rebased` to the messages and remove "Rebase option still disabled". Report `BC_README_UPDATED: yes`.

**ITERATION_HINT:** likely-fixable

Iteration 2: BC README "Pull" entry updated to describe the rebase option; no code changes.

Conductor browser smoke test (2026-11-23): diverged pull with rebase gave m2* on top of m3 ('Rebased'), original m2 shown as lost commit, repeat pull 'Already up to date', push accepted and remote history linear; behind-only pull with rebase fast-forwarded; no new console errors.
