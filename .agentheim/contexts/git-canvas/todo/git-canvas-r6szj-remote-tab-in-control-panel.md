---
id: git-canvas-r6szj
title: Remote tab in control panel with remote status
status: todo
type: feature
context: git-canvas
created: 2026-11-23
completed:
depends_on: [git-canvas-wv6va]
blocks: [git-canvas-w3ndg, git-canvas-rx1y3]
tags: [ui, control-panel, remote]
related_adrs: [0005]
related_research: []
prior_art: []
---

## Why
Push, fetch and pull need one coherent home in the UI, and the trainer needs to see which repo has a remote and what the current branch's upstream is.

## What
- Add a "Remote" tab to the control-panel tabs in `index.html`; move the Clone button there.
- Show the active repo's remote (`origin: <remote repo name>` or "no remote") and the current branch's upstream (`main -> origin/main` or "no upstream").
- Show ahead/behind counts relative to the upstream.
- Add disabled placeholder buttons for Push, Fetch, Pull and a "rebase" checkbox (wired up in later tasks).
- Tab content updates on active-repo change, checkout, commit, rename and restore.
- Add a small inline message area for operation results and rejections, built with `textContent`/`classList`.

## Acceptance criteria
- [ ] A "Remote" tab exists and opens like the other tabs.
- [ ] For a repo without remote, the tab shows "no remote" and the operation buttons are disabled.
- [ ] For a clone, the tab shows `origin: <source name>` and `main -> origin/main`; the ahead count updates after a commit.
- [ ] Clicking another repo region updates the tab.
- [ ] Renaming the remote repo updates the displayed remote name.
- [ ] Detached HEAD shows "detached HEAD - no upstream".
- [ ] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Ahead/behind = commit-ID set difference over the ancestry of both tips. Reuse existing tab/UI helpers in `ui/UiControl.kt` and `App.kt`.
