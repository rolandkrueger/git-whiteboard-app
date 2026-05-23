---
id: infrastructure-001
title: "Walking skeleton: multi-repo proof-of-concept"
type: spike
status: done
bc: infrastructure
depends_on: [git-canvas-001, scenario-001, infrastructure-002]
completed: 2026-05-23
---

# infrastructure-001: Walking skeleton — multi-repo proof-of-concept

## Goal
Prove that two GitGraph instances can exist simultaneously on a single Fabric.js canvas, that control panel operations target the active repo only, and that the shared-canvas layout strategy (from git-canvas-001) is viable. This is the first moment code changes for the multi-repo feature.

## Context
The existing app creates exactly one GitGraph on startup and wires all control panel buttons to it. Nothing in the current architecture prevents multiple GitGraph instances, but the canvas layout, control panel routing, and swimlane counter isolation for multiple repos have not been tested. This spike de-risks those assumptions before any feature work begins.

The layout strategy (git-canvas-001) is now confirmed as Option C: shared infinite canvas, click-to-focus active repo selection, active repo shown in full color, inactive repos visually subordinated.

## Acceptance criteria
- Two GitGraph instances render on the Fabric canvas without visual collision, placed at fixed initial positions
- Each instance starts with its own initial commit (e.g. `m1` in repo "origin", `m1` in repo "alice")
- Clicking on a repo region makes it the active repo (click-to-focus)
- The active repo is indicated by a colored border around its region; the inactive repo has no border or a neutral one
- Adding a commit via the control panel adds it to the active repo only
- The app builds and runs via `sh gradlew run` with no new runtime errors in the browser console
- No persistence, no push/pull semantics required — just two independent graphs coexisting

## Out of scope for this spike
- Drag-to-reposition repo regions — fixed initial placement is sufficient to prove the concept
- Faded/desaturated inactive repos — a colored border is the visual signal for this spike; full desaturation is deferred to follow-on work
- Repo labels/banners — nice to have but not required to prove the canvas model
- URL encoding / scenario loading — handled separately by scenario-001
- More than two repos — two is enough to validate the architecture

## Notes
This spike is intentionally feature-thin and architecture-thick. It is the first prototype and the baseline for all subsequent multi-repo work. Do not implement push/pull logic here — prove the canvas model first.

## Outcome
Implemented multi-repo proof-of-concept on the existing Fabric.js canvas (Approach 1 from ADR-0003 note).

**Key changes:**
- `GitGraphConfiguration` made instantiable with `xOffset`/`yOffset` to position each repo's graph independently. Constants remain in the companion object; per-instance `leftOffset`/`bottomOffset` add the offset on top.
- `Commit` accepts a `config: GitGraphConfiguration` instance so commit circles render at the correct per-repo position.
- `GitGraph` gains `repoName`, `config`, `isActive` (with visual feedback via `RepoRegion`), `containsPoint()` for click hit-testing, and `clearGraph()` for the reset button.
- New `RepoRegion` component (`components/RepoRegion.kt`) draws a rounded border rect and repo name label. Blue border = active; grey = inactive.
- `App.kt` creates two `GitGraph` instances (`origin` at default position, `alice` offset 800px right). Canvas `mouse:up` distinguishes clicks from drags and routes repo activation. All control panel buttons target `activeGraph`.
- `index.html` gains an `activeRepoLabel` element showing the current active repo.

Build: `sh gradlew jsBrowserProductionWebpack` passes cleanly.
