---
id: "0003"
title: Migrate canvas foundation from Fabric.js to Konva.js
status: accepted
date: 2026-05-23
scope: infrastructure
related_tasks: [infrastructure-002]
related_research: [canvas-library-evaluation-2026-05-23]
---

# ADR-0003: Migrate canvas foundation from Fabric.js to Konva.js

## Context

The Git Whiteboard App currently renders commit graphs on an HTML5 canvas using Fabric.js 5.x. The Kotlin/JS bindings are handwritten (~601 lines across three files in `src/jsMain/kotlin/fabricjs/`). The project is preparing to support multi-repo rendering: 2–3 independent Git commit graph instances on a shared infinite canvas (see ADR-0001).

This decision evaluates whether to:
1. Stay on Fabric.js v5
2. Upgrade to Fabric.js v6/v7
3. Migrate to Konva.js
4. Migrate to PixiJS

Research report `canvas-library-evaluation-2026-05-23` informed this decision.

## Decision

**Migrate to Konva.js v10.x.** Fabric.js (any version) is rejected as the forward foundation. PixiJS is rejected. Konva.js is adopted as the canvas rendering library for all future work.

## Rationale

### Why Fabric.js v5 is rejected

Fabric.js v5 is effectively abandoned. The Fabric.js team stated publicly that "development moved on to v6 and beyond and does not have time to keep updated 2 versions." The v7.4.0 release (May 2026) included a security fix for CVE-2026-44311 with no v5 backport. Running unmaintained library code with an unpatched security CVE is not acceptable for a project intended for live deployment.

### Why Fabric.js v7 upgrade is not preferred over Konva migration

Migrating from Fabric.js v5 to v7 is a near-complete rewrite at the binding layer regardless of the canvas library. The v5-to-v6 migration is extensive:

- Full TypeScript rewrite; import syntax changed; core types renamed (`fabric.Object` → `FabricObject`, `fabric.Text` → `FabricText`, `fabric.Image` → `FabricImage`)
- ES6 class system replaces custom prototype-based inheritance; `setOptions()` → `set()`, method chaining deprecated
- Callbacks replaced by Promises; animation API changed; event coordinate shapes changed
- Group system completely rewritten
- v6-to-v7 adds further positional defaults change (`originX`/`originY` defaults flipped)

Given that the Kotlin bindings must be substantially rewritten regardless, the question becomes: *rewrite bindings for a library still carrying Fabric's architectural complexity, or rewrite bindings for a library with a cleaner API?* Konva's architecture is a better fit (see below).

### Why Konva.js is the right target

Konva.js v10.3.0 (April 2026) offers:

- **Scene graph that maps directly to multi-repo canvas model.** `Stage → Layer → Group → Shape` maps cleanly to "one infinite canvas / one layer per repo instance / groups for commits and labels." Each `Group` can be set `draggable: true` independently and receives its own event callbacks.
- **First-party infinite canvas / pan-zoom.** Konva ships official sandbox documentation for scroll-zoom and drag-pan — the exact capability needed for multi-repo canvas (ADR-0001).
- **Flatter inheritance hierarchy.** Konva's class surface area is smaller and more regular than Fabric's. A handful of core classes (`Stage`, `Layer`, `Group`, shape subclasses) with a consistent config-object constructor pattern (`new Konva.Rect({ x, y, width, height, draggable })`) maps cleanly to Kotlin external classes with `definedExternally` parameters and external interfaces for config objects.
- **Bundled TypeScript definitions.** `konva/src/index-types.d.ts` provides a Dukat input source. The dormant `kirk` project (`guggens/kirk`) provides a reference for manual binding structure.
- **Active maintenance.** 14,500 GitHub stars, v10.3.0 released April 2026, 97.4% TypeScript codebase.

### Why PixiJS is rejected

PixiJS is WebGL-first and game-oriented. For a diagram editor with at most dozens of nodes, the WebGL rendering path is unnecessary complexity. PixiJS's interaction/drag-and-drop API is secondary to its rendering engine, making idiomatic Kotlin/JS bindings harder to write. No existing Kotlin/JS wrapper exists.

## Kotlin/JS Binding Strategy

The existing Fabric.js bindings in `src/jsMain/kotlin/fabricjs/` will be **replaced entirely**, not migrated. The replacement will be written in `src/jsMain/kotlin/konvajs/`.

Binding approach:
1. **Dukat-generated stubs as a starting point** — run `dukat` against `konva/src/index-types.d.ts` to generate Kotlin external declarations. Use the output as a reference and starting scaffold, not as production code.
2. **Manual cleanup required** — Dukat's output for complex TypeScript generics and overloaded constructors is known to produce verbose `dynamic` fallbacks. The generated stubs will be reviewed and manually refined.
3. **Scope to what the app uses** — bind `Stage`, `Layer`, `Group`, `Line`, `Circle`, `Text`, `Rect`, and the event/drag-and-drop surface. Do not bind the full Konva API surface.
4. **Config-object pattern via external interfaces** — each Konva shape constructor accepts a config object; model these as Kotlin external interfaces with `val` properties marked `definedExternally`.

## Dukat Spike Findings

**Status: environment-blocked — manual verification required.**

Dukat was not available in the development environment at the time this ADR was written (`dukat not found`). The spike could not be run against `konva/src/index-types.d.ts` directly.

Based on the research report and known Dukat behavior:

- Dukat is confirmed capable of processing TypeScript `.d.ts` files; it is the standard tool for generating Kotlin/JS external declarations from npm packages.
- Konva ships well-maintained TypeScript definitions (`index-types.d.ts`) written in 97.4% TypeScript; the type definitions are first-party, not community-maintained, which reduces the risk of incomplete or incorrect type annotations.
- Known Dukat limitations: generated code for complex overloads falls back to `dynamic`; union types and TypeScript mapped types may not produce idiomatic Kotlin. Konva's config-object constructors (e.g., `new Konva.Rect(config: RectConfig)`) are a pattern Dukat handles well — each `*Config` interface becomes a Kotlin external interface.
- The dormant `kirk` project's `konva.d.ts` provides evidence that manual Kotlin/JS bindings for Konva are achievable and reasonably compact.
- **Required before integration task (infrastructure-001):** a developer must install Dukat locally (`npm install -g dukat` or via the Kotlin Gradle plugin's `dukat` task), run it against the installed Konva package's type definitions, and assess the output quality before committing to the binding approach. The binding task should begin with this manual verification step.

**Preliminary assessment (without running Dukat):** Konva's TypeScript definitions are well-structured and the API surface required by this project is small. Manual binding effort is estimated as moderate — comparable to or less than the existing Fabric.js v5 handwritten bindings (~601 lines). The key risk is union types for event names and shape-specific config object inheritance; these will require manual handling regardless of Dukat output.

## Constraints

- Groups and layers cannot be shared across two different `Stage` instances. Each Stage is tied to its own DOM container. If the multi-repo canvas requires a single DOM container, a single Stage with multiple Groups/Layers must be used. This is consistent with ADR-0001's multi-repo layout model.
- This ADR does not authorize production code changes. Fabric.js removal and Konva.js integration are subsequent tasks (`infrastructure-001`).

## Alternatives Considered

| Option | Verdict | Reason |
|---|---|---|
| Stay on Fabric.js v5 | Rejected | Unmaintained; unpatched security CVE (CVE-2026-44311) |
| Migrate to Fabric.js v7 | Rejected | Near-complete binding rewrite required anyway; no advantage over Konva migration |
| Migrate to Konva.js | **Accepted** | Best fit for multi-repo canvas model; active maintenance; flatter API; first-party TypeScript types |
| Migrate to PixiJS | Rejected | WebGL-first; overkill for diagram-scale workloads; game-oriented interaction model |
| Vanilla Canvas 2D | Rejected | Would require hand-implementing hit testing, drag-and-drop, and pan/zoom from scratch — equivalent to writing a partial Konva clone |

## Consequences

- The `src/jsMain/kotlin/fabricjs/` directory will be deleted as part of the Konva integration task.
- A new `src/jsMain/kotlin/konvajs/` directory will be created with Kotlin external declarations for the Konva API surface used by the app.
- The `build.gradle.kts` npm dependency block must be updated: remove `fabric`, add `konva` at v10.x.
- `kotlin-js-store/yarn.lock` must be regenerated via `sh gradlew kotlinUpgradeYarnLock` after the npm dependency change.
- All rendering code in `GitGraph.kt` and `src/jsMain/kotlin/components/` that references Fabric.js types must be rewritten to target Konva.
