---
id: git-canvas-snjqw
title: Add interactive rebase (rebase -i) - squash/fixup, drop, reorder commits
status: backlog
type: feature
context: git-canvas
created: 2026-11-25
completed:
depends_on: []
blocks: []
tags: [captured]
related_adrs: []
related_research: []
prior_art: []
---

## Why
Cleaning up history with `rebase -i` is a staple of Git training; the graph shows how commits are combined, removed or reordered.

## What
Add an interactive rebase: the trainer picks, for each commit to be rebased, an action - pick, squash/fixup (combine with the previous commit), drop - and may reorder them; the rebase then replays according to that plan.

## Acceptance criteria
- [ ] To be defined during refinement.

## Notes
Captured via `quick-capture` on 2026-11-25 - raw, unrefined. Needs a `modeling` refine pass before
it can be promoted.
