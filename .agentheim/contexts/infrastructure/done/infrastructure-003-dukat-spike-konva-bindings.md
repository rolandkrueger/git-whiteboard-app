---
id: infrastructure-003
title: Dukat spike — assess Konva.js TypeScript binding generation
status: done
type: spike
context: infrastructure
created: 2026-05-23
completed: 2026-05-23
commit:
depends_on: []
blocks: [infrastructure-004]
tags: [konva, dukat, kotlin-js, bindings]
related_adrs: ["0003"]
related_research: [canvas-library-evaluation-2026-05-23]
prior_art: [infrastructure-002]
---

## Why

ADR-0003 adopted Konva.js as the replacement canvas library but flagged the Dukat spike as environment-blocked: `dukat` was not available in the development environment at the time of writing. The binding approach for the integration task (infrastructure-004) depends on knowing how much of Konva's TypeScript definitions Dukat can process usably and how much requires manual cleanup.

This spike unblocks infrastructure-004 by answering that question concretely.

## What

Run `dukat` against Konva's bundled TypeScript definitions (`konva/src/index-types.d.ts`) and evaluate the generated Kotlin external declarations. Produce a short written assessment (appended to this task's Outcome section) covering: what generated cleanly, what fell back to `dynamic`, and which declarations need manual rewriting. Do not write production bindings in this task.

## Acceptance criteria

- [ ] `dukat` is installed and confirmed runnable (globally via `npm install -g dukat` or via Kotlin Gradle plugin's `dukat` task)
- [ ] `dukat` is run against the Konva package's type definitions file (path: `node_modules/konva/src/index-types.d.ts` once the dependency is added, or a locally installed copy)
- [ ] The generated `.kt` output is reviewed for: `Stage`, `Layer`, `Group`, `Line`, `Circle`, `Text`, `Rect`, and the event/drag-drop surface
- [ ] An Outcome section is written in this task file summarising: which types generated cleanly, which fell back to `dynamic`, and the estimated manual cleanup effort
- [ ] A recommendation is stated: start from Dukat output, start from the `kirk` reference project, or write bindings from scratch

## Notes

- ADR-0003 §"Kotlin/JS Binding Strategy" describes the intended binding approach and known Dukat limitations
- The `kirk` project (`guggens/kirk`) on GitHub provides a reference for manual Konva.js Kotlin/JS bindings — useful if Dukat output is poor
- Konva npm package must be added to `build.gradle.kts` before `node_modules/konva` is available; alternatively install it separately for the spike only (`npm install konva` in a scratch directory)
- The full Konva type file is at `konva/src/index-types.d.ts` inside the package
- Dukat installation: `npm install -g dukat` or check if the Kotlin Gradle plugin exposes it via `sh gradlew generateExternals`
- This task produces no production code — only the assessment. infrastructure-004 does the actual bindings.

## Outcome

### Environment

- Dukat is available via `npx` (version **0.5.8-rc.4**). It is not installed globally; the binary resolves from the npx cache at `~/.npm/_npx/`. The Kotlin Gradle plugin exposes no `dukat` or `generateExternals` task for this project.
- Konva v10.3.0 was installed standalone in `/tmp/konva-spike/` via `npm install konva@10` for the spike.
- The Konva package ships type definitions in `lib/*.d.ts` and `lib/shapes/*.d.ts` (2,475 lines total). The top-level `lib/index.d.ts` is a one-line re-export only; the canonical entry is `lib/_FullInternals.d.ts` which imports every shape. Note: Konva v10's `.d.ts` files use `.ts` extensions in their import paths (e.g., `import { Konva } from './_FullInternals.ts'`), which causes Dukat to fail silently when run against `index.d.ts` directly. Individual per-type `.d.ts` files process successfully.

### Dukat output assessment per type

**Stage** (`Stage.d.ts`, 64 lines source → 97 lines generated)
- `StageConfig` interface generates cleanly as `external interface StageConfig : ContainerConfig`.
- The `container` property falls back to `dynamic /* HTMLDivElement? | String? */` due to the union type. Manual fix: split into two `jsName` overloads or widen to `dynamic`.
- Private/internal methods (`_validateAdd`, `_bindContentEvents`, etc.) are included in output — requires pruning. The public API surface is usable.
- `getPointerPosition()` returns `Vector2d?` correctly. `add(layer: Layer, vararg rest: Any)` generates correctly.

**Layer** (`Layer.d.ts`, 68 lines source → 83 lines generated)
- `LayerConfig` interface generates cleanly with all three Boolean optional properties.
- Anonymous object types for `setSize` parameter become `external interface T$0 { var width: Any; var height: Any }` — usable but requires rename.
- The class itself generates well. `destroy()`, `batchDraw()`, `getStage()`, `add()` all produce clean Kotlin.

**Group** (`Group.d.ts`, 10 lines source → 20 lines generated)
- `GroupConfig : ContainerConfig` generates as a clean empty external interface.
- `Group : Container<dynamic>` — the union `Group | Shape` resolves to `dynamic`. For the app's usage (`group.add(circle)`) this is workable; a manual binding would tighten it to `Node`.

**Circle** (`Circle.d.ts`, 17 lines source → 29 lines generated)
- `CircleConfig : ShapeConfig` with `radius: Number?` generates cleanly.
- The generated class has `open var radius: Any` (from the TypeScript `GetSet<number, this>` mapped type) — this is the main `dynamic` degradation for shapes. Manual fix: add `fun radius(): Number` and `fun radius(v: Number): Circle` overloads.

**Rect** (`Rect.d.ts`, 11 lines source → 19 lines generated)
- `RectConfig` is expressed as a TypeScript intersection type (`ShapeConfig & { cornerRadius?: ... }`); Dukat emits the class with `Shape<ShapeConfig /* ShapeConfig & T$0 */>` and loses the `RectConfig` interface entirely. The `cornerRadius` property also becomes `Any`.
- Manual fix required: write `RectConfig` as a Kotlin external interface extending `ShapeConfig` with `cornerRadius`.

**Line** (`Line.d.ts`, 30 lines source → 50 lines generated)
- `LineConfig` generates cleanly. The `points` property falls back to `dynamic /* Array<Number>? | Int8Array? | ... */` due to the multi-typed array union — acceptable; callers will always pass `Array<Double>`.
- `Line<Config : LineConfig>` generates correctly as a generic external class with constructor.
- `closed`, `bezier`, `tension`, `points` properties all generate as `Any` (from `GetSet`). Same pattern as Circle.

**Text** (`Text.d.ts`, 92 lines source → 139 lines generated)
- `TextConfig` generates fully with all 17 optional properties (text, fontFamily, fontSize, fontStyle, align, verticalAlign, padding, lineHeight, wrap, ellipsis, etc.). All clean.
- Anonymous inline return types for `textArr` become anonymous `T$0` interfaces. Rename needed.
- Text-specific properties (`radius`, `fontFamily`, etc.) fall to `Any` via `GetSet`. Same pattern.
- `setText(text: String)` generates correctly typed. `getTextWidth()` and `getTextHeight()` return `Number`.

**Node base class** (`Node.d.ts`, generated 418 lines)
- `NodeConfig` external interface generates completely and cleanly (~30 properties, all typed correctly).
- `KonvaEventObject<EventType, This>` generates as a clean external interface.
- `KonvaEventListener<This, EventType>` becomes a Kotlin `typealias`.
- The `on()` event handler: `fun <K : Nothing?> on(evtStr: K, handler: KonvaEventListener<Node<Config>, Any>)` — the TypeScript constraint `K extends keyof NodeEventMap` degrades to `K : Nothing?` because `NodeEventMap` uses an index signature. This means event strings are unchecked at compile time. Acceptable for this project (event names like `"click"`, `"dragstart"` are short constants).
- `off()` and `fire()` generate cleanly.
- `draggable` in `NodeConfig` is `Boolean?` — correct.
- `DragAndDrop` module: `DD` singleton object generates as `external val DD: ...` with an anonymous object type. Usable for `DD.isDragging` checks.

### Summary of dynamic fallbacks

| Type | Clean | `dynamic`/`Any` degradation | Source |
|---|---|---|---|
| Stage | Config interface, most methods | `container` union `HTMLDivElement\|String` | TypeScript union type |
| Layer | Config interface, most methods | `T$0` anonymous param type | Inline object type |
| Group | Config interface, class | `Container<dynamic>` child union | `Group \| Shape` union |
| Circle | Config interface | `radius: Any` (getter/setter) | `GetSet<N,this>` mapped type |
| Rect | Class | Entire `RectConfig` missing; `cornerRadius: Any` | Intersection type |
| Line | Config interface, class | `points: dynamic` (typed array union); `closed/bezier/tension/points: Any` | Union + `GetSet` |
| Text | Config interface, class | `textArr: T$0`; string props `= Any` | `GetSet` + inline object |
| Node | `NodeConfig`, event interfaces | `on()` key type `K : Nothing?`; `target: dynamic` | Index-signature map |

### Estimated manual cleanup effort: **medium**

Roughly 60-70% of the generated code is directly usable. The patterns requiring manual intervention are:
1. **`GetSet<T, this>` mapped type** — degrades every property accessor on shapes (Circle, Line, Text, Rect) to `Any`. Fix by writing `fun prop(): T` and `fun prop(v: T): ShapeType` overloads for each property the app uses. Approximately 15-20 properties total across the scoped types.
2. **`RectConfig` intersection type** — Dukat drops the config interface entirely. Write it by hand (10 lines).
3. **`Group | Shape` union on containers** — generates `Container<dynamic>`. Acceptable; no fix needed if the app only calls `add()` with known types.
4. **Anonymous `T$0`, `T$1` interface names** — rename to meaningful names (5-10 occurrences).
5. **Internal/private methods** — prune the generated output to the public API surface used by the app.

The underlying type definitions are well-structured. No fundamental incompatibility was found. All seven scope-required types (`Stage`, `Layer`, `Group`, `Line`, `Circle`, `Text`, `Rect`) produce structurally correct external classes and config interfaces. Cleanup is mechanical, not creative.

### Recommendation: **Start from Dukat output**

The generated stubs are above the 60% usability threshold. The correct approach for infrastructure-004:

1. Run Dukat per-file against each required shape's `.d.ts` (not the top-level `index.d.ts`, which fails silently due to `.ts` extension imports).
2. Merge the per-file output into a single `src/jsMain/kotlin/konvajs/` package.
3. Apply mechanical cleanup: rename `T$N` interfaces, replace `GetSet`-degraded `Any` properties with explicit getter/setter overloads for the specific properties the app uses, write `RectConfig` by hand, prune internal methods.
4. The `kirk` reference project is not required; the generated output combined with the Konva TypeScript definitions is a sufficient starting point.

### Acceptance criteria checklist

- [x] `dukat` confirmed runnable — `npx dukat` version **0.5.8-rc.4**
- [x] `dukat` run against Konva type definitions (per-file from `konva@10` standalone install)
- [x] Generated output reviewed for all seven scoped types plus event/drag surface
- [x] Outcome section written with `dynamic`/`Any` degradation analysis
- [x] Recommendation stated: **start from Dukat output**
