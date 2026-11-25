---
id: git-canvas-m7z8d
title: Add merge --squash - a single commit without a second parent
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
Squash merges are common in PR workflows; in the graph the difference to a real merge (no second parent edge) is directly visible.

## What
Add a squash option to merge: `merge --squash` produces a single new commit on the current branch that contains the source branch's changes, without a second parent; the source branch is not marked as merged.

## Acceptance criteria
- [ ] To be defined during refinement.

## Notes
Captured via `quick-capture` on 2026-11-25 - raw, unrefined. Needs a `modeling` refine pass before
it can be promoted.
