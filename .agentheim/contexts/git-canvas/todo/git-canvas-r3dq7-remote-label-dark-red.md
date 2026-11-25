---
id: git-canvas-r3dq7
title: Render remote-tracking labels in dark red instead of light grey, so they are readable and match the common red convention for remote branches
status: todo
type: feature
context: git-canvas
created: 2026-11-25
completed:
depends_on: []
blocks: []
tags: [rendering, labels, readability, remote]
related_adrs: []
related_research: []
prior_art: [git-canvas-b5f0r]
---

## Why
Remote-tracking labels (`origin/<name>`) are currently drawn with light grey text (`#777`)
inside an unfilled dashed pill. On the canvas that is hard to read, especially in demos on a
projector. In addition, tools like `git log --graph --decorate` and most Git GUIs show remote
branches in red, so trainees already associate red with remote-tracking refs.

## What
`RemoteBranchLabel` (`src/jsMain/kotlin/components/CommitLabel.kt`) uses a single dark red
colour for both the label text and the dashed border, instead of grey text plus a border in
the branch colour.

- Colour: dark red `#B71C1C` (Material Red 900) - strong contrast on the white canvas.
- The pill stays unfilled and dashed, so remote-tracking labels remain visually distinct
  from filled local branch labels.
- The branch colour passed in from `RemoteBranch` (`commitColor`) is no longer used for the
  label; drop the now-unused parameter if nothing else needs it.

## Acceptance criteria
- [ ] `RemoteBranchLabel` renders its text in `#B71C1C` (no longer `#777`).
- [ ] `RemoteBranchLabel` renders its dashed border in `#B71C1C`, independent of the branch colour.
- [ ] The remote-tracking pill remains unfilled with a dashed border; local branch, tag and HEAD labels are unchanged.
- [ ] Full verification build passes (`sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`).
- [ ] After cloning a repo, `origin/main` is clearly readable in dark red on the canvas. [human-eye]

## Notes
- If the user manual describes remote-tracking labels as grey, update that wording.
- Pre-existing, out of scope: `CommitLabel.setMuted` sets a grey fill on the rectangle,
  which would also fill a dashed remote pill if it were ever muted. Leave as is unless it
  becomes visible.
- Independent of git-canvas-xt4ch (connector arrows); both touch label rendering but
  different elements.
