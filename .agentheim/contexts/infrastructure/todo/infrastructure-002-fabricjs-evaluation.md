---
id: infrastructure-002
title: Migrate canvas foundation from Fabric.js to Konva.js
type: decision
status: todo
bc: infrastructure
depends_on: []
blocks: [infrastructure-001]
tags: [canvas, fabricjs, konvajs, technology-choice, migration]
related_adrs: []
related_research: [canvas-library-evaluation-2026-05-23]
prior_art: []
---

# infrastructure-002: Migrate canvas foundation from Fabric.js to Konva.js

## Why
Fabric.js 5.x (current) is effectively abandoned — the core team will not maintain v5 alongside v7, and a security CVE was fixed in v7 with no v5 backport. Migrating to Fabric.js v7 would require a near-complete rewrite of the handwritten Kotlin bindings anyway (TypeScript rewrite, renamed core types, Group system overhaul, prototype → ES6 class inheritance). Given that the bindings need to be rewritten regardless, Konva.js is the better migration target: its Stage → Layer → Group → Shape hierarchy maps directly to the multi-repo canvas model, infinite pan/zoom is first-party, and it ships well-maintained TypeScript definitions usable by Dukat.

Research report: `knowledge/research/canvas-library-evaluation-2026-05-23.md`

## What
Validate Konva.js as the replacement via a Dukat spike, then write an ADR confirming the migration decision. No production code changes in this task — the spike and ADR are the deliverables.

## Acceptance criteria
- [ ] Dukat spike: run Dukat (or `dukat`) against Konva's bundled `index-types.d.ts` and assess the quality of the generated Kotlin stubs — specifically: are inheritance overloads handled cleanly, how much manual cleanup is needed, are event handler types usable?
- [ ] Spike findings documented in the ADR's Notes section (not a separate file)
- [ ] ADR committed to `knowledge/decisions/` confirming migration to Konva.js
- [ ] ADR covers: why Fabric.js v5 is rejected, why Fabric.js v7 upgrade is not preferred over Konva migration, Konva.js fit for the multi-repo canvas model, Kotlin/JS binding strategy (Dukat-generated stubs + manual cleanup)
- [ ] No production code changes — Fabric.js removal and Konva.js integration are subsequent tasks

## Notes
Konva.js v10.3.0 (April 2026), 14,500 GitHub stars, ships bundled TypeScript definitions.

The existing handwritten Fabric.js bindings live in `src/jsMain/kotlin/fabricjs/`. They will be replaced — not migrated — once this ADR is committed and `infrastructure-001` adopts Konva.

PixiJS was evaluated and rejected: WebGL-first, game-oriented, overkill for a small interactive graph with dozens of nodes.
