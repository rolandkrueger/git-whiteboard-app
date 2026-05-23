---
topic: Canvas library evaluation for multi-repo Kotlin/JS diagram app
date: 2026-05-23
requested_by: model
related_tasks: [infrastructure-002]
---

# Research: Canvas Library Evaluation for Multi-Repo Kotlin/JS Diagram App

## Question

The Git Whiteboard App currently uses Fabric.js 5.x with handwritten Kotlin/JS external bindings. Before building multi-repo rendering (2–3 independent Git commit graph instances on a shared infinite canvas), we need to know: (1) should we stay on Fabric.js or migrate to Fabric.js v6/v7, (2) are Konva.js or PixiJS better fits for this use case, and (3) what is the Kotlin/JS interop effort for each option?

## Summary

- **Fabric.js v5 is effectively abandoned.** The core team stated: "development moved on to v6 and beyond and does not have time to keep updated 2 versions." v5 receives no new features; v7.4.0 (released May 2026) is the current stable series [1][2].
- **Fabric.js v6/v7 is a near-complete rewrite** relative to v5: TypeScript rewrite, ES6 class hierarchy, renamed core types (`fabric.Object` → `FabricObject`), promises replacing callbacks, and a Group system rewrite. Migration effort is high. The handwritten v5 Kotlin bindings would require substantial rework [3][4].
- **Konva.js is the strongest alternative for this use case.** It has first-class infinite canvas / pan-zoom, built-in per-group drag-and-drop, independent click events per node, a Stage → Layer → Group → Shape hierarchy that maps cleanly to "multiple repo instances on one canvas," 14,500 GitHub stars, and an active v10.3.0 release (April 2026) [5][6][7].
- **PixiJS (44,500 stars, v8.18.1 stable) is optimized for WebGL-accelerated games** and is a poor fit: its event/interaction model is secondary, and the rendering model is harder to express via Kotlin external declarations [8].
- **No production-grade Kotlin/JS bindings exist for any of these libraries.** Dukat can auto-generate stubs from TypeScript `.d.ts` files (Konva ships `index-types.d.ts`, Fabric v6+ ships built-in types, PixiJS ships types), but generated code needs manual cleanup, especially around inheritance hierarchies [9][10].
- **Kotlin/JS interop with Fabric.js class inheritance is a known friction point.** Kotlin/JS external declarations cannot override overloaded external functions or functions with default parameters; Fabric's deep prototype/class hierarchy makes this painful in practice. Konva's architecture is flatter and less inheritance-dependent [9].

## Findings

### Angle 1: Fabric.js Current State and v5/v6/v7 Status

**Fabric.js v5 maintenance status.** The Fabric.js team's own documentation states that v5 "is the last major version of Fabric written in ES5" and that "development moved on to v6 and beyond." No new features will be backported to v5. The project does not maintain two active branches simultaneously [2]. The project uses `fabric5.fabricjs.com` as the legacy v5 documentation domain, signaling it is archived.

**Fabric.js v6 and v7 stability.** v6 reached production-ready status and was quickly followed by v7.0.0, released December 22, 2024. The v7.0.0 release notes state: "Fabric 7.0 is basically Fabric 6.0 — there is no large major change that should stop you from upgrading" [4]. v7.4.0 was released May 18, 2026, as the current stable release [1]. v7 is therefore the actively maintained, production-ready version.

**Breaking changes v5 → v6.** The migration is extensive [3]:
- Full TypeScript rewrite; remove external `@types/fabric` dependency (types now bundled).
- Import syntax changed: `import { fabric } from 'fabric'` → `import { Canvas, Rect } from 'fabric'`.
- Major class renames: `fabric.Object` → `FabricObject`, `fabric.Text` → `FabricText`, `fabric.Image` → `FabricImage`.
- ES6 class system replaces custom prototype-based inheritance. `setOptions()` → `set()`, `setCoords()` removed, `setBackgroundImage()` → property assignment.
- Callbacks replaced by Promises (e.g., `loadSVGFromString`).
- Animation API changed (`animate()` → `_animate()` with manual render triggers).
- Event coordinate shapes changed (`pointer` became canvas-relative; `absolutePointer` added).
- Group system completely rewritten.
- Method chaining deprecated.

**Breaking changes v6 → v7** [4]:
- `originX`/`originY` default changed from `'left'`/`'top'` to `'center'` — the most impactful positional change.
- `fireMiddleClick`, `fireRightClick`, `stopContextMenu` now default to `true`.
- Deprecated methods removed: `getCenter()`, `getPointer()`, `setWidth()`, `setHeight()`.
- Node.js minimum raised to 20.

**Multi-group rendering on a single Fabric canvas.** Fabric's model supports multiple independent `fabric.Group` instances added to a single canvas via `canvas.add()`. Each group can be dragged, clicked, and positioned independently. There are no architectural barriers to the multi-repo use case within Fabric v5's model. The v6 Group rewrite is described as "ever more powerful," but this is vendor marketing [3][11].

### Angle 2: Alternatives — Konva.js, PixiJS, Vanilla Canvas 2D

**Konva.js.**

Konva.js (v10.3.0, released April 30, 2026) is an HTML5 Canvas framework built around a DOM-like scene graph: `Stage → Layer → Group → Shape` [5][6]. Key properties:

- *Multi-instance / multi-group support:* Multiple independent `Konva.Group` objects on the same `Stage` and `Layer` are fully supported. Each group has its own coordinate space, can be set `draggable: true` independently, and receives its own event callbacks (`click`, `mousedown`, `dragstart`, etc.) [7]. Note: groups/layers cannot be shared across two different `Stage` instances — each Stage is tied to its own DOM container [12].
- *Infinite canvas / pan-zoom:* Konva has first-party sandbox documentation for an infinite canvas with scroll-to-zoom and drag-to-pan, covering both pointer-relative zoom and multi-touch pinch-to-zoom [13]. This maps directly to the "shared infinite canvas" requirement.
- *Event handling:* Konva maintains a hidden hit-graph canvas for high-performance event detection per shape. Click, hover, tap, and attribute-change events are all first-class [5].
- *Community health:* 14,500 GitHub stars, 1,100 forks, 49 releases, 17 open issues, most recent release April 30 2026 [6]. Written 97.4% in TypeScript; ships its own `index-types.d.ts` [14].
- *TypeScript/d.ts:* Bundled. Available at `konva/src/index-types.d.ts` in the repository [14].
- *Kotlin/JS prior art:* One dormant project (`kirk`, 2 commits, unmaintained) wraps Konva via React-Konva for Kotlin/JS, including `konva.d.ts` declarations [15]. No production-ready standalone Kotlin/JS Konva binding exists. Dukat can generate stubs from the bundled `.d.ts`.

**PixiJS.**

PixiJS (v8.18.1, released April 14, 2026) is a WebGL-first 2D renderer targeting games and high-frame-rate graphics [8].

- *Interactivity:* PixiJS v8 includes an interaction system, but it is secondary to rendering performance. For a diagram editor with draggable nodes, Konva's dedicated drag-and-drop API is substantially easier to use. Performance benchmarks show PixiJS at 60 fps vs Konva at 23 fps at 8,000 shapes, but Git commit graphs have at most dozens of nodes [16].
- *Multi-group support:* PixiJS uses a `Container` tree similar to Konva's groups; multiple independent containers on a single stage are supported.
- *Community health:* 44,500 GitHub stars; very large community. Primarily game developers. Ships full TypeScript declarations [8].
- *Kotlin/JS fit:* The WebGL-heavy architecture and game-oriented API make it harder to write idiomatic Kotlin/JS bindings. No existing Kotlin/JS wrapper known.

**Vanilla Canvas 2D.**

Vanilla Canvas 2D gives full control with no library overhead but requires hand-implementing hit testing, drag-and-drop, dirty-rect rendering, and zoom/pan from scratch. A HackerNews commenter specifically evaluated Konva, PixiJS, and native Canvas 2D and chose native Canvas — but the comment context was a game-like workload, not a diagram editor [17]. For this project's scope, building these primitives from scratch would be equivalent to writing a partial Konva clone.

### Angle 3: Kotlin/JS + Canvas Library Integration

**Kotlin/JS external declaration patterns.** Kotlin/JS uses the `external` modifier to describe JavaScript API shapes without generating JS code. The approach supports external classes, interfaces, companion objects (for static members), and `definedExternally` for default parameters. Key limitations [9]:
- Cannot override overloaded external functions.
- Cannot override external functions with default parameters.
- `is` checks on external interfaces are not allowed.
- External declarations cannot have bodies.
- Prototype mutation (reading/writing `.prototype.foo`) requires `js()` escape hatch with loss of type safety.

**Dukat auto-generation.** Dukat converts TypeScript `.d.ts` files to Kotlin external declarations automatically, either at build time or via a Gradle task. For npm dependencies with bundled TypeScript definitions (Konva: yes; Fabric.js v6+: yes; PixiJS: yes), this provides a starting point [10]. The output requires manual cleanup: generated code tends to produce verbose `dynamic` fallbacks for complex overloads and does not handle inheritance hierarchies cleanly.

**Fabric.js v5 Kotlin binding pain points.** The current project's handwritten Fabric.js v5 bindings are in `src/jsMain/kotlin/fabricjs/`. Fabric.js v5 used a custom prototype-based inheritance system (`fabric.util.createClass`), which clashes with Kotlin/JS's static type model. Developers must use `unsafeCast<T>()` when traversing the class hierarchy at runtime. The Kotlin/JS docs explicitly note that external declarations cannot capture JavaScript's mixed prototype/class member model [9]. Fabric v6/v7's move to ES6 classes partially alleviates this — external class declarations can `extend` each other in Kotlin/JS — but the `originX`/`originY` default changes and renamed core types in v6/v7 still require a near-complete rewrite of the existing bindings.

**Konva.js Kotlin/JS binding effort.** Konva's architecture is flatter and more regular than Fabric's: a handful of core classes (`Stage`, `Layer`, `Group`, `Shape` subclasses) with a consistent config-object constructor pattern (`new Konva.Rect({ x, y, width, height, draggable })`). This maps cleanly to Kotlin external classes with `definedExternally` parameters and external interfaces for config objects. The bundled `index-types.d.ts` provides a solid Dukat input. The `kirk` project's dormant `konva.d.ts` is a reference for manual binding structure [15]. Total binding effort is estimated as moderate: less total surface area than Fabric, and no custom inheritance utility to model.

**Existing Kotlin/JS wrappers.** The `Kotlin/js-externals` repository on GitHub contains externals for some browser APIs but no canvas library wrappers [10]. No maintained Kotlin/JS wrappers for Fabric.js, Konva.js, or PixiJS were found in the npm ecosystem or JetBrains repositories as of May 2026.

## Sources

1. [Releases — fabricjs/fabric.js (GitHub)](https://github.com/fabricjs/fabric.js/releases) — Release timeline; confirms v7.4.0 as latest stable, May 2026.
2. [fabric — npm package page](https://www.npmjs.com/package/fabric?activeTab=versions) — Version history and weekly download counts (~500K/week).
3. [Upgrading to Fabric.js 6.0 (Official Docs)](https://fabricjs.com/docs/upgrading/upgrading-to-fabric-60/) — Authoritative v5→v6 migration guide. Official source.
4. [Upgrading to Fabric.js 7.0 (Official Docs)](https://fabricjs.com/docs/upgrading/upgrading-to-fabric-70/) — v6→v7 changes; confirms v7 is a light increment over v6.
5. [Konva Framework Overview (Official Docs)](https://konvajs.org/docs/overview.html) — Architecture: Stage, Layer, Group, Shape; events; drag-and-drop.
6. [konvajs/konva (GitHub)](https://github.com/konvajs/konva) — Community health: 14.5k stars, v10.3.0 (April 2026), 97.4% TypeScript.
7. [Shape Groups — Konva.js Docs](https://konvajs.org/docs/groups_and_layers/Groups.html) — Independent group dragging and event handling confirmed.
8. [pixijs/pixijs Releases (GitHub)](https://github.com/pixijs/pixijs/releases) — v8.18.1 stable (April 2026); 44.5k stars.
9. [Use JavaScript code from Kotlin (Official Kotlin Docs)](https://kotlinlang.org/docs/js-interop.html) — External declarations patterns, limitations with inheritance and overloads.
10. [Kotlin/dukat (GitHub)](https://github.com/Kotlin/dukat) — Auto-generation of Kotlin external declarations from TypeScript `.d.ts`.
11. [V6: Group Rewrite — fabricjs/fabric.js Issue #7670](https://github.com/fabricjs/fabric.js/issues/7670) — Group system rewrite discussion; vendor context.
12. [Is it possible to share groups among separate stages? — konvajs/konva Issue #101](https://github.com/konvajs/konva/issues/101) — Confirms groups cannot be shared across Stages; workaround is cloning or shared state.
13. [Infinite Canvas with Zoom and Pan — Konva.js Docs](https://konvajs.org/docs/sandbox/Infinite_Canvas.html) — First-party infinite canvas sandbox with scroll-zoom and drag-pan.
14. [konva/src/index-types.d.ts (GitHub)](https://github.com/konvajs/konva/blob/master/src/index-types.d.ts) — Bundled TypeScript definitions; Dukat input source.
15. [guggens/kirk (GitHub)](https://github.com/guggens/kirk) — Dormant Kotlin/JS + React-Konva project; reference for konva.d.ts structure.
16. [Fabric.js vs Konva vs PixiJS — PkgPulse Blog 2026](https://www.pkgpulse.com/blog/fabricjs-vs-konva-vs-pixijs-canvas-2d-graphics-libraries-2026) — Performance benchmarks (8K shapes); use-case comparison. Note: commercial blog, treat as secondary source.
17. [HackerNews discussion on canvas engines](https://news.ycombinator.com/item?id=43413691) — First-person account of evaluating PixiJS, Konva, and native Canvas 2D; chose native for game workload.

## Open Questions

- **Dukat output quality for Konva v10.** Dukat's output quality for complex TypeScript generics and overloaded constructors is inconsistent. A spike generating Kotlin stubs from `konva/src/index-types.d.ts` is needed to assess how much manual cleanup is required before the bindings are usable.
- **Fabric.js v6/v7 multi-group rendering regressions.** The Group rewrite in v6 is described as a major change. No specific reports of multi-group rendering bugs were found, but this should be verified against the actual use case (overlapping groups, z-ordering, independent transform per group) in a spike.
- **PixiJS v8 Container/interaction model for diagrams.** PixiJS was not depth-tested for diagram-style interactivity. If the team already has WebGL expertise, a brief PixiJS spike may be warranted, but current evidence does not support it as the primary candidate.
- **Fabric.js v5 security posture.** The v7.4.0 release included a CVE fix (CVE-2026-44311). It is unknown whether v5 is affected and whether a patch will be issued given v5's unmaintained status. This is a risk for projects that cannot migrate.
