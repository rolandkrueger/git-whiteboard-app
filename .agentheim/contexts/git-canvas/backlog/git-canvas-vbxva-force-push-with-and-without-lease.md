---
id: git-canvas-vbxva
title: Add force push, both plain --force and --force-with-lease
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
Force push is the classic way to lose a colleague's commits. Showing it next to `--force-with-lease` makes the case for the lease variant visible.

## What
Add force push to the remote operations, in two variants: plain `--force` overwrites the remote branch even if it has commits the pusher has not fetched (the colleague's commits become lost on the remote); `--force-with-lease` refuses when the remote branch moved since the last fetch, protecting those commits.

## Acceptance criteria
- [ ] To be defined during refinement.

## Notes
Captured via `quick-capture` on 2026-11-25 - raw, unrefined. Needs a `modeling` refine pass before
it can be promoted.
