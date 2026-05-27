---
id: git-canvas-004
title: User-configurable initial repository name
status: done
type: feature
context: git-canvas
created: 2026-05-27
completed: 2026-05-27
commit: 869fc5c
depends_on: []
blocks: []
tags: [repository, multi-repo]
related_adrs: []
related_research: []
prior_art: []
---

## Why
The initial repository is currently hard-coded as `origin`, which makes demos less flexible and also implies a remote-style name even when the trainer wants a neutral or local starting repository.

## What
Allow the Git Trainer to change the name of the initial repository. The default initial repository name should be `my-repo` instead of `origin`.

## Acceptance criteria
- [ ] A freshly opened app shows the initial repository named `my-repo`.
- [ ] The trainer can edit/rename the initial repository from the UI.
- [ ] The rendered repository label updates immediately after renaming.
- [ ] Git graph operations continue to target the active repository after it has been renamed.
- [ ] Existing multi-repo behavior remains intact; repository names are not hard-coded to `origin`.

## Notes
The current hard-coded `origin` default should be replaced wherever the initial repository is created or labelled.
