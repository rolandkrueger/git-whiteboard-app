---
id: git-canvas-maf17
title: Add git revert - a new commit that undoes an earlier one
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
`revert` is the safe counterpart to `reset`: it undoes a commit without rewriting history, so it is the right tool once a commit has been pushed. Trainers need it to contrast reset vs. revert.

## What
Add a revert operation: reverting a commit creates a new commit on the current branch that undoes the chosen commit; the original commit stays in history.

## Acceptance criteria
- [ ] To be defined during refinement.

## Notes
Captured via `quick-capture` on 2026-11-25 - raw, unrefined. Needs a `modeling` refine pass before
it can be promoted.
