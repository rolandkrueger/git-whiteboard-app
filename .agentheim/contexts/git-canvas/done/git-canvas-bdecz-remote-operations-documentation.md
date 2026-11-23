---
id: git-canvas-bdecz
title: Remote operations documentation and doc sync
status: done
type: feature
context: git-canvas
created: 2026-11-23
completed: 2026-11-23
depends_on: [git-canvas-ktja3]
blocks: []
tags: [docs, remote]
related_adrs: [0004, 0005]
related_research: []
prior_art: []
---

## Why
The manual and the context docs must reflect the feature before it is shared with trainers.

## What
- Add a "Remote repositories" section to `src/docs/asciidoc/manual.adoc` covering Clone, Push (incl. rejection), Fetch, Pull and Pull with rebase.
- Update the git-canvas README (tracking-label language, key events, resolved open question).
- Update the scenario README (snapshot v2 fields) and `CLAUDE.md`/`AGENTS.md` architecture notes (commit ID allocator, `RemoteOps`).
- Check control-panel help texts. Do not commit `build/` artifacts.

## Acceptance criteria
- [x] The manual builds and contains a section describing each remote operation.
- [x] The READMEs describe snapshot v2 and the remote-tracking label concept and no longer list the resolved open question.
- [x] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
-

## Outcome
Added the "Remote Repositories" manual section (Clone, Remote tab status, Push, Fetch, Pull, Pull with rebase) and a commit-id note; synced git-canvas README (Upstream, Ahead/behind, events, no open questions), scenario README (snapshot v2), CLAUDE.md and AGENTS.md. Full verification build succeeded; the browser console was not checked (documentation-only).

## Verifier note (iteration 1)

**REASONS:**
- The manual's "Commit Ids" section (manual.adoc ~lines 38-45) still describes per-branch counters ("the third commit added to `main` ... is `m3`", detached HEAD "the HEAD's commit counter" `H2`, `feature/foo` -> `f1, f2, f3` vs `feature/bar` -> `b1, b2, b3`). The code uses one counter shared by all branches and repos (`GitGraph.kt` ~643 via `idAllocator.next()`; ADR 0004: ids are no longer per-branch contiguous). The added sentence about cross-repo numbering contradicts the remaining old text and examples.
- Minor gaps (not FAIL reasons alone): Push section lacks `Everything up-to-date`; Pull section lacks `Already up to date` and that the fetch summary prefixes the pull result; Clone section says "all commits" but lost commits are not copied.
- Everything else matches the code (labels, messages, snapshot v2 fields, CLAUDE.md file names, no open question left, English, build exit 0).

**SUGGESTED_FIX:** Rewrite the "Commit Ids" section to describe the single shared counter: first letter of the branch (or `H` when HEAD is detached) plus the next number from a counter shared by all branches and repositories; replace the per-branch examples with possible ones; keep the first-letter uniqueness rule. Also mention `Everything up-to-date` (Push), `Already up to date` + fetch-summary prefix (Pull), and that lost commits are not cloned.

**ITERATION_HINT:** likely-fixable


(Iteration 2: Commit Ids section rewritten to the shared-counter behaviour; Push/Pull/Clone messages completed and verified against RemoteOps.kt.)
