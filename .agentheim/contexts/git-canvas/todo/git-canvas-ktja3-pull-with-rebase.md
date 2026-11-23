---
id: git-canvas-ktja3
title: Pull with rebase option
status: todo
type: feature
context: git-canvas
created: 2026-11-23
completed:
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
- [ ] Diverged history: Pull with rebase yields linear history; local commits are re-created as `<id>*` on top of `origin/<name>`; no merge commit.
- [ ] The original local commits appear when "Show lost commits" is on.
- [ ] Pull with rebase when only behind fast-forwards.
- [ ] Pull with rebase when up to date shows "Already up to date".
- [ ] After a rebase pull, Push is accepted when origin has not moved on.
- [ ] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Reuse the existing rebase code; keep changes narrow because `rebase()` is tightly coupled to rendering.
