---
id: git-canvas-vj6gr
title: Sync the user manual with the current app
status: doing
type: chore
context: git-canvas
created: 2026-11-24
completed:
depends_on: [git-canvas-wcbec, git-canvas-nywqs]
blocks: []
tags: [docs, manual]
related_adrs: []
related_research: []
prior_art: [git-canvas-bdecz]
---

## Why
The remote features (clone, push, fetch, pull, remote highlighting, clone placement) each updated
their own manual section, but nobody re-read the manual as a whole. A few passages from before the
multi-repo/remote work are now wrong or incomplete, so a trainer reading the manual front to back
gets contradictory statements.

## What
Audit `src/docs/asciidoc/manual.adoc` against the current control panel (`index.html`) and canvas
behavior, and fix every stale or missing passage. Known gaps:

1. The intro lists five tabs (`General`, `Commits`, `Refs`, `Merge/Rebase`, `About`); the `Remote`
   tab is missing, so there are six now.
2. "Working With Commits" says commit ids are "unique for the current graph". Since the shared
   allocator (ADR 0004) they are unique app-wide, and that sentence contradicts the "Commit Ids"
   subsection right below it.
3. "Working With Repositories" / `Share` doesn't say that repository positions on the canvas are
   restored as well.

Runs after git-canvas-wcbec and git-canvas-nywqs, which document their own behavior. This task does
not re-describe them; it only checks that the result reads consistently.

## Acceptance criteria
- [ ] The intro names all six control-panel tabs, including `Remote`, in the order shown in the UI.
- [ ] No sentence in the manual says commit ids are unique per graph or per repository; the commit-id wording matches the "Commit Ids" subsection (app-wide ids).
- [ ] The `Share` paragraph states that repository positions on the canvas (including the clone layout) are restored along with the graphs.
- [ ] Every button and checkbox in the control panel (`index.html`) is mentioned by its visible label somewhere in the manual, or the worker lists any intentionally undocumented control in the task's Outcome.
- [ ] `sh gradlew asciidoctor --warning-mode all` builds the manual without new warnings.
- [ ] Only `manual.adoc` (and if needed the prebuilt `try-it-out/` copy, if it bundles the manual) is changed; no source code changes.

## Notes
- Project language is English; keep the manual's existing tone and AsciiDoc structure.
- If the audit finds a behavior gap (the manual describes something the app doesn't do), record it
  in the Outcome as a follow-up idea instead of changing code.
