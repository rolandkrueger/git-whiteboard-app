---
id: git-canvas-bdecz
title: Remote operations documentation and doc sync
status: doing
type: feature
context: git-canvas
created: 2026-11-23
completed:
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
- [ ] The manual builds and contains a section describing each remote operation.
- [ ] The READMEs describe snapshot v2 and the remote-tracking label concept and no longer list the resolved open question.
- [ ] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
-
