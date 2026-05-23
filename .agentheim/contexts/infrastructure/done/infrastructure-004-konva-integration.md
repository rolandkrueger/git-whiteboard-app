---
id: infrastructure-004
title: Konva.js integration — replace Fabric.js bindings and rewrite rendering
status: done
type: feature
context: infrastructure
created: 2026-05-23
completed: 2026-05-23
commit: 09e7f74
depends_on: [infrastructure-003]
blocks: []
tags: [konva, fabric, kotlin-js, bindings, rendering, migration]
related_adrs: ["0001", "0002", "0003"]
related_research: [canvas-library-evaluation-2026-05-23]
prior_art: [infrastructure-001, infrastructure-002]
---

## Why

ADR-0003 adopted Konva.js as the canvas foundation to replace Fabric.js v5, which is abandoned and carries an unpatched security CVE (CVE-2026-44311). The Fabric.js bindings and all rendering code must be replaced before any new canvas features (multi-repo push/pull semantics, URL-encoded scenario sharing) can be built on a sound foundation.

The walking skeleton (infrastructure-001) demonstrated the multi-repo layout using Fabric.js. This task replaces that foundation with Konva.js while keeping all behaviour from the walking skeleton intact.

## What

Replace the Fabric.js canvas layer with Konva.js throughout the application:

1. **npm dependency change** — remove `fabric` from `build.gradle.kts`; add `konva` at v10.x
2. **Kotlin/JS bindings** — write `src/jsMain/kotlin/konvajs/` using the Dukat spike output (infrastructure-003) as a starting point; bind `Stage`, `Layer`, `Group`, `Line`, `Circle`, `Text`, `Rect`, and the event/drag-drop surface
3. **Delete Fabric.js bindings** — remove `src/jsMain/kotlin/fabricjs/`
4. **Rewrite rendering code** — rewrite `GitGraph.kt`, `components/` (including `RepoRegion.kt`), and any other files referencing Fabric.js types to target Konva.js
5. **Regenerate yarn.lock** — run `sh gradlew kotlinUpgradeYarnLock` after the npm change
6. **Verify full build** — `sh gradlew jsBrowserProductionWebpack jvmJar` passes cleanly

## Acceptance criteria

- [ ] `src/jsMain/kotlin/fabricjs/` is deleted; no Fabric.js imports remain in the codebase
- [ ] `src/jsMain/kotlin/konvajs/` exists with Kotlin external declarations for: `Stage`, `Layer`, `Group`, `Line`, `Circle`, `Text`, `Rect`, and the event callback surface
- [ ] `build.gradle.kts` npm block: `fabric` removed, `konva` at v10.x added
- [ ] `kotlin-js-store/yarn.lock` regenerated via `kotlinUpgradeYarnLock`
- [ ] Production webpack build (`sh gradlew jsBrowserProductionWebpack`) passes with no new errors
- [ ] Application behaviour from the walking skeleton (infrastructure-001) is preserved:
  - Two repo regions ("origin" and "alice") render side-by-side on a shared canvas
  - Click-to-focus switches the active repo (colored border indicator)
  - All control panel operations route to the active repo
  - Initial graph (commit `m1`, branch `master`, `HEAD`) renders correctly for both repos
- [ ] Browser console has no new runtime errors on page load

## Notes

- Read infrastructure-003's Outcome section before designing the bindings — it will state whether to start from Dukat output, the `kirk` reference, or scratch
- ADR-0001 describes the multi-repo canvas layout (Stage → Layer per repo, Group per commit)
- ADR-0003 §"Kotlin/JS Binding Strategy" and §"Constraints" describe the intended binding shape and per-Stage DOM container constraint
- The `kirk` project (`guggens/kirk`) on GitHub is a reference for manual Konva.js Kotlin/JS bindings
- `RepoRegion.kt` currently uses `FabricRect` and `FabricText` — rewrite to Konva `Rect` and `Text` inside a `Layer`
- Konva `Stage` requires a DOM container `id`; the HTML may need a dedicated container per repo or a single container for the shared stage
- The BC README notes that `GitGraph` is currently tightly coupled (state + Git semantics + rendering). This task rewrites the rendering layer only — do not restructure the coupling unless it blocks the Konva integration
- After this task lands, the codebase is on a clean Konva.js foundation for all subsequent feature work

## Outcome

Fabric.js has been fully replaced by Konva.js v10.3.0. The fabricjs/ directory was deleted. A new `konvajs/` package was written with external declarations split across 8 files: `KonvaBindings.kt` (config interfaces, abstract base classes), `KonvaStage.kt`, `KonvaLayer.kt`, `KonvaGroup.kt`, `KonvaCircle.kt`, `KonvaRect.kt`, `KonvaLine.kt`, `KonvaText.kt` — each with a per-file `@JsModule` pointing to the corresponding `konva/lib/*.js` named export.

A Kotlin `data class Point` replaced Fabric's `fabric.Point` in `components/Point.kt`. The `Renderable` interface, all `components/`, `graph/`, `config/`, and `App.kt` were rewritten to use `Layer` instead of `FabricCanvas`. Pan/zoom uses Konva stage transforms; click-to-focus uses `stage.getPointerPosition()`. All `renderAll()` calls became `layer.batchDraw()`. All `canvas.sendToBack()` calls became `node.moveToBottom()`. Double-click events changed from `"mousedblclick"` to `"dblclick"`.

Production webpack build passes (`jsBrowserProductionWebpack`: `compiled successfully`). `yarn.lock` was regenerated via `kotlinUpgradeYarnLock`.

Key files: `src/jsMain/kotlin/konvajs/`, `src/jsMain/kotlin/App.kt`, `src/jsMain/kotlin/components/`, `src/jsMain/kotlin/graph/`, `build.gradle.kts`, `kotlin-js-store/yarn.lock`.
