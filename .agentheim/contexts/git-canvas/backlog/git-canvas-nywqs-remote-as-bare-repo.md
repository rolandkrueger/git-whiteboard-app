---
id: git-canvas-nywqs
title: Show the remote repo as a bare repository (no working tree / HEAD emphasis)
status: backlog
type: feature
context: git-canvas
created: 2026-11-24
completed:
depends_on: []
blocks: []
tags: [remote, clone, head]
related_adrs: [0005]
related_research: []
prior_art: [git-canvas-wv6va]
---

## Why
A real remote (e.g. on GitHub) is a bare repository: nobody works in it, it has no working tree,
and its HEAD only names the default branch. Showing the remote like a normal working repo
(prominent HEAD, commits possible) blurs that distinction for learners.

## What
Idea: once a repo acts as a remote, de-emphasize (grey out) its HEAD label and possibly warn or
restrict working-tree operations (commit, checkout) on it. Changes Git semantics, not only visuals.

## Acceptance criteria
- [ ] To be refined: the remote's HEAD is visually de-emphasized compared to clones' HEADs.

## Notes
- Open: should commits on the remote stay possible (trainers may want to simulate "someone else
  pushed")? Probably yes — then only the visual part applies, or a toggle.
- Related visual cues: git-canvas-f2ced.
