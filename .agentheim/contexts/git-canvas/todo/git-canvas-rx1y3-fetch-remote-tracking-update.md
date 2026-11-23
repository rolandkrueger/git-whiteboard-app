---
id: git-canvas-rx1y3
title: Fetch updates tracking labels and brings in missing commits
status: todo
type: feature
context: git-canvas
created: 2026-11-23
completed:
depends_on: [git-canvas-r6szj]
blocks: [git-canvas-sz7p8]
tags: [fetch, remote]
related_adrs: [0005]
related_research: []
prior_art: []
---

## Why
Fetch is the prerequisite for understanding pull and the trainer's answer to a rejected push.

## What
- Fetch button on the Remote tab.
- For all branches of the remote, copy missing commits into the active repo (same receiving primitive as push), laid out on the lane of the tracking ref (ADR 0005).
- Create, move or remove `origin/<name>` labels to match the remote's branches; local branches and HEAD are untouched.
- Nothing changed: "Already up to date". Remote unavailable: message.

## Acceptance criteria
- [ ] Clone A to B, commit twice in A, Fetch in B: both commits appear in B with the same IDs; `origin/main` moves to the new tip; local `main` does not move.
- [ ] A new branch created in A appears in B after fetch as `origin/<name>` only (no local branch).
- [ ] A branch deleted in A: after fetch, B's `origin/<name>` label disappears; commits stay.
- [ ] After fetch, the Remote tab shows "behind N".
- [ ] Fetching twice in a row: the second fetch shows "Already up to date".
- [ ] Fetched commits do not overlap existing commits. [human-eye]
- [ ] Share URL, reload: state matches.
- [ ] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Copies must preserve `mergedCommit` links for fetched merge commits. Only depends on the Remote tab, so it can run in parallel with push if the receiving primitive is coordinated.
