---
id: git-canvas-f2ced
title: Highlight the remote repo and connect clones to their origin
status: todo
type: feature
context: git-canvas
created: 2026-11-24
completed:
depends_on: [git-canvas-dxt9c]
blocks: []
tags: [clone, remote, multi-repo, repo-region, rendering]
related_adrs: [0001, 0005]
related_research: []
prior_art: [git-canvas-wv6va, git-canvas-b5f0r, git-canvas-003]
---

## Why
After cloning, the audience cannot tell from the canvas which repo is the remote and which are
working clones; only the names hint at it. The remote relationship should be visible on its own.

## What
Three combined visual cues, all derived from the existing `remoteRepoId` link (no new snapshot field):

- **Remote role is derived:** a repo is a *remote repo* exactly when at least one other repo's
  `remoteRepoId` points at it.
- **Remote region styling:** the remote's `RepoRegion` gets a dashed border and a light tinted
  background. This echoes the dashed remote-tracking label pills — "dashed = remote" becomes a
  consistent visual rule.
- **Header badges:** the remote's header shows a small `REMOTE` pill next to its name; each clone's
  header shows `origin -> <remote name>`.
- **Connection line:** a line from each clone region to its origin region, labelled `origin`.

## Acceptance criteria
- [ ] A repo with no clones looks exactly as today (solid border, no tint, no badge).
- [ ] As soon as a repo is cloned, its region border becomes dashed with a light tinted fill, and its header shows a `REMOTE` badge.
- [ ] Dashed border and tint persist through `setActive(true/false)` and after the red push-rejection flash (`setAlert`) ends; active vs. inactive stays distinguishable for the remote too.
- [ ] Each clone's header shows `origin -> <remote repo name>`; renaming the remote updates it.
- [ ] Each clone has a line to its origin region labelled `origin`; it stays attached to both regions when either region grows (`updateBounds`) or the first clone is moved by the triangle layout.
- [ ] The connection line and tint never capture clicks: click-to-activate and stage panning work as before.
- [ ] After a Share URL round-trip, badges, dashed styling and connection lines are restored from `remoteRepoIndex` alone.
- [ ] Clone of a clone: the middle repo shows both the `REMOTE` badge and its own `origin -> ...` header, with lines to both neighbours.
- [ ] Manual and git-canvas README (ubiquitous language: "Remote repo") describe the new cues.
- [ ] Full verification build passes; no new browser console errors.
- [ ] On a projector-sized view, the remote repo is identifiable at a glance without reading names. [human-eye]

## Notes
- Each repo has its own Konva `Layer`; a connector spanning two regions does not naturally belong
  to either. A dedicated connector layer below the repo layers is probably cleanest; it must be
  redrawn when region bounds change. Konva `Line` already has bindings; `Arrow` would need new
  handwritten bindings (verify in the browser, per CLAUDE.md).
- Konva `Rect` supports `dash` — check whether the binding exposes it before adding it.
- Bare-repo semantics (greyed-out HEAD in the remote) is a separate idea: git-canvas-nywqs.
