---
id: git-canvas-r6szj
title: Remote tab in control panel with remote status
status: done
type: feature
context: git-canvas
created: 2026-11-23
completed: 2026-11-23
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
- [x] A "Remote" tab exists and opens like the other tabs.
- [x] For a repo without remote, the tab shows "no remote" and the operation buttons are disabled.
- [x] For a clone, the tab shows `origin: <source name>` and `main -> origin/main`; the ahead count updates after a commit.
- [x] Clicking another repo region updates the tab.
- [x] Renaming the remote repo updates the displayed remote name.
- [x] Detached HEAD shows "detached HEAD - no upstream".
- [x] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
Ahead/behind = commit-ID set difference over the ancestry of both tips. Reuse existing tab/UI helpers in `ui/UiControl.kt` and `App.kt`.

## Outcome
Added a Remote tab (ids remoteTabControl/remoteTab/remoteStatus/upstreamStatus/aheadBehindStatus/pushButton/fetchButton/pullButton/pullRebaseCheckbox/remoteMessage); Clone button moved into it. `GitGraph.upstreamStatus()` computes upstream and ahead/behind; `updateRemoteTab` in App.kt refreshes the tab via a 300 ms poll (covers commit, checkout, rename, restore, active-repo change). Build succeeds; behavioural criteria (tab opens, no remote, clone display, ahead count, detached HEAD, rename) are NOT browser-verified and remain unticked for the conductor's smoke test. Push/Fetch/Pull buttons and rebase checkbox are disabled placeholders; remoteMessage is an empty area.

Conductor browser smoke test (2026-11-23): Remote tab opens; fresh repo shows no remote / no upstream / disabled buttons; clone shows origin: my-repo, main -> origin/main, ahead 0 then ahead 1 after a commit; switching regions and renaming the remote update the tab; detached HEAD shows 'detached HEAD - no upstream'. A Konva isDragging TypeError on canvas pointermove is pre-existing (also on develop without this task).
