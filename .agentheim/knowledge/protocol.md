# Protocol

Chronological log of everything that happens in this project.
Newest entries on top.

---

## 2026-05-23 -- Research: Canvas library evaluation for multi-repo Kotlin/JS diagram app

**Type:** Research
**Requested by:** model
**Report:** knowledge/research/canvas-library-evaluation-2026-05-23.md
**Summary:**
- Fabric.js v5 is effectively abandoned; v7.4.0 (May 2026) is the current stable series, but migration from v5 is a near-complete rewrite due to TypeScript rewrite, renamed core types, and Group system overhaul.
- Konva.js (v10.3.0, April 2026) is the strongest alternative: first-class infinite canvas/pan-zoom, independent draggable groups with per-node events, 14.5k stars, bundled TypeScript definitions usable by Dukat.
- No production-grade Kotlin/JS bindings exist for Fabric.js, Konva.js, or PixiJS; Dukat can auto-generate stubs from bundled `.d.ts` files but manual cleanup is required, especially for inheritance-heavy APIs.

---

## 2026-05-23 -- Model / Refined: infrastructure-002 - Migrate canvas foundation from Fabric.js to Konva.js

**Type:** Model / Refine
**BC:** infrastructure
**Status after:** todo
**Summary:** Direction confirmed as Konva.js migration based on research findings. Fabric.js v5 abandoned (security CVE unpatched), v7 migration is a near-rewrite of Kotlin bindings regardless, Konva.js maps better to multi-repo canvas model and ships bundled TypeScript definitions. Task updated to: run Dukat spike against Konva's .d.ts, write ADR confirming migration. No production code in this task.

---

## 2026-05-23 -- Model / Refined: infrastructure-001 - Walking skeleton: multi-repo proof-of-concept

**Type:** Model / Refine
**BC:** infrastructure
**Status after:** todo
**Summary:** Updated acceptance criteria to match confirmed decisions: click-to-focus replaces radio buttons/dropdown; colored border replaces fade/desaturate for active repo indicator; fixed initial placement confirmed sufficient (drag-to-reposition deferred); explicit out-of-scope list added to keep the spike narrow.

---

## 2026-05-23 -- Model / Refined: scenario-001 - URL encoding format for graph snapshots

**Type:** Model / Refine
**BC:** scenario
**Status after:** todo
**Summary:** Confirmed Option A (JSON + gzip + base64url in URL fragment). Typical scenarios are small (few commits/branches per repo), so URL length is not a concern. URL always app-generated — no hand-editability needed. A control panel button serializes current state to the address bar. App detects fragment on startup and restores graph state. No length fallback required. Options B (custom DSL) and C (binary) rejected.

---

## 2026-05-23 -- Model / Refined: git-canvas-001 - Multi-repo canvas layout strategy

**Type:** Model / Refine
**BC:** git-canvas
**Status after:** todo
**Summary:** Confirmed Option C (shared infinite canvas) as the layout approach. Trainer positions repos freely by dragging; click to focus makes a repo active; active repo renders in full saturated colors, inactive repos are faded/desaturated. Pan/zoom is global. Repo labels (origin, alice, bob) are visible banners. Options A (fixed panels), B (tabs), and D (floating windows) explicitly rejected.

---

## 2026-05-23 -- Brainstorm: Git Whiteboard App vision

**Type:** Brainstorm
**Outcome:** vision created
**BCs identified:** git-canvas (core), scenario (supporting), infrastructure (generic)
**Summary:** Socratic session with the Git trainer. Established that the tool is used exclusively by trainers live during sessions, never prepared in advance. The primary missing feature is multi-repo visualization (1 remote + 2+ local clones) to enable push/pull/conflict demos. URL-encoded scenarios are the desired persistence model — no server. Git internals and realistic commit IDs are explicit non-goals.
**ADRs written:** none (decisions deferred to task queue)
**Foundation tasks emitted:** git-canvas-001 (multi-repo layout decision), scenario-001 (URL encoding format decision), infrastructure-001 (walking skeleton spike — depends on both decisions)

---
