---
id: git-canvas-wcbec
title: Re-layout repo regions when they grow into each other
status: backlog
type: feature
context: git-canvas
created: 2026-11-24
completed:
depends_on: [git-canvas-dxt9c]
blocks: []
tags: [layout, repo-region, multi-repo]
related_adrs: [0001]
related_research: []
prior_art: [git-canvas-003]
---

## Why
Repo regions grow upward and rightward as commits and branches are added (`RepoRegion.updateBounds`,
bottom anchor fixed). With clones placed below their origin (git-canvas-dxt9c) a growing clone
eventually overlaps the origin above it, and two side-by-side clones can overlap horizontally.
git-canvas-dxt9c deliberately only uses a generous fixed gap.

## What
Keep repo regions from overlapping as they grow — e.g. push neighbouring regions away, grow
clones downward instead, or offer a manual "tidy up layout" action. To be decided in refinement.

## Acceptance criteria
- [ ] To be refined: no two repo regions overlap after any sequence of commits/branches in a three-repo triangle.

## Notes
- Open: automatic vs. trainer-triggered; animated or instant; interaction with Share URL offsets.
