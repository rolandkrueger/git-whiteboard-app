# Protocol

Chronological log of everything that happens in this project.
Newest entries on top.

---

## 2026-05-23 -- Task verified and completed: scenario-002 - Share button — encode and restore graph state via URL fragment

**Type:** Work / Task completion
**Task:** scenario-002 - Share button — encode and restore graph state via URL fragment
**Summary:** Implemented end-to-end URL-based scenario sharing: Share button serializes multi-repo graph state to JSON, compresses with gzip, base64url-encodes, writes to URL fragment and copies to clipboard; on startup detects #state= fragment and asynchronously restores both graphs.
**Verification:** PASS (iteration 2 — iteration 1 fixed two bugs: version field jsIntOrNull coercion, return pump() in decompressFromBase64Url)
**Commit:** TBD
**Files changed:** 5
**Tests added:** 0
**ADRs written:** none

---

## 2026-05-23 -- Verification failed: scenario-002 - Share button — encode and restore graph state via URL fragment

**Type:** Work / Verification failure
**Task:** scenario-002 - Share button — encode and restore graph state via URL fragment
**Iteration:** 1 of 3
**Reasons:** ScenarioSerializer.decode always returns null — version field uses as? Int instead of jsIntOrNull (JSON.parse returns Double not Int); decompressFromBase64Url missing return pump() in recursive loop causing Promise to never settle on mid-stream failure
**Iteration hint:** likely-fixable
**Next:** re-dispatched worker

---

## 2026-05-23 -- Batch started: [scenario-002]

**Type:** Work / Batch start
**Tasks:** scenario-002 - Share button — encode and restore graph state via URL fragment
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Model / Captured: scenario-002 - Share button — encode and restore graph state via URL fragment

**Type:** Model / Capture
**BC:** scenario
**Filed to:** todo
**Summary:** Implements the EncodeCurrentStateAsUrl + LoadScenarioFromUrl commands from ADR-0002: a Share button serializes multi-repo graph state to JSON, compresses with gzip, base64url-encodes, and writes to the URL fragment; on startup, the app detects and restores from that fragment instead of rendering the default blank canvas.

---

## 2026-05-23 -- Work session ended

**Type:** Work / Session end
**Completed:** 1 (first-try PASS: 1 [infrastructure-005])
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 1 (4a4cc52)

---

## 2026-05-23 -- Task verified and completed: infrastructure-005 - Canvas drag-to-pan broken after Konva.js migration

**Type:** Work / Task completion
**Task:** infrastructure-005 - Canvas drag-to-pan broken after Konva.js migration
**Summary:** Restored drag-to-pan by replacing Konva synthetic stage events with native DOM events on the container; also added mouseleave guard and moved mouseup to window to handle release outside the canvas.
**Verification:** PASS (iteration 1)
**Commit:** 4a4cc52
**Files changed:** 1
**Tests added:** 0
**ADRs written:** none

---

## 2026-05-23 -- Batch started: [infrastructure-005]

**Type:** Work / Batch start
**Tasks:** infrastructure-005 - Canvas drag-to-pan broken after Konva.js migration
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Model / Captured: infrastructure-005 - Canvas drag-to-pan broken after Konva.js migration

**Type:** Model / Capture
**BC:** infrastructure
**Filed to:** todo
**Summary:** Regression from infrastructure-004 — canvas drag-to-pan stopped working after the Konva.js migration; Ctrl+scroll zoom is unaffected. Filed directly to todo as a concrete, observable regression with clear acceptance criteria.

---

## 2026-05-23 -- Work session ended

**Type:** Work / Session end
**Completed:** 2 (first-try PASS: 2 [infrastructure-003, infrastructure-004])
**Bounced:** 1 (infrastructure-003, env-bounce — Bash denied in first session; re-dispatched successfully)
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 2 (4959555, 09e7f74)

---

## 2026-05-23 -- Task verified and completed: infrastructure-004 - Konva.js integration: replace Fabric.js bindings and rewrite rendering

**Type:** Work / Task completion
**Task:** infrastructure-004 - Konva.js integration: replace Fabric.js bindings and rewrite rendering
**Summary:** Replaced Fabric.js v5 with Konva.js v10.3.0. Deleted fabricjs/ (3 files). Created konvajs/ package (8 files). Rewrote Renderable, all components/, graph/, and App.kt to use Layer. Pan/zoom via Stage transforms; click-to-focus via getPointerPosition(). Production webpack build passes.
**Verification:** PASS (iteration 1 — verifier false-positive overridden: flagged orchestrator-owned INDEX.md/protocol.md changes as worker violations; verifier confirmed build passes and all code correct)
**Commit:** 09e7f74
**Files changed:** 23
**ADRs written:** none

---

## 2026-05-23 -- Batch started: [infrastructure-004]

**Type:** Work / Batch start
**Tasks:** infrastructure-004 - Konva.js integration: replace Fabric.js bindings and rewrite rendering
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Task verified and completed: infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation

**Type:** Work / Task completion
**Task:** infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation
**Summary:** Dukat 0.5.8-rc.4 confirmed via npx; run against Konva v10.3.0 per-file .d.ts. All 7 scoped types produce structurally correct external classes; main degradation is GetSet mapped type → Any for shape property accessors. Recommendation: start from Dukat output with medium-effort mechanical cleanup (~15-20 props, write RectConfig, rename T$0 interfaces, prune internals).
**Verification:** PASS (iteration 1)
**Commit:** 4959555
**Files changed:** 1
**ADRs written:** none

---

## 2026-05-23 -- Batch started: [infrastructure-003]

**Type:** Work / Batch start
**Tasks:** infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Task bounced: infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation

**Type:** Work / Task bounced
**Task:** infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation
**Reason:** Bash tool was denied in this session. Spike requires shell access to install dukat, run npm install konva, and invoke dukat against the type definitions. Worker note added to task file.
**Moved to:** backlog

---

## 2026-05-23 -- Batch started: [infrastructure-003]

**Type:** Work / Batch start
**Tasks:** infrastructure-003 - Dukat spike: assess Konva.js TypeScript binding generation
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Model / Captured: infrastructure-003, infrastructure-004 - Konva.js migration tasks

**Type:** Model / Capture
**BC:** infrastructure
**Filed to:** todo
**Summary:** Two implementation tasks capturing the Konva.js migration work mandated by ADR-0003. infrastructure-003 (Dukat spike) must run first to assess binding generation quality; infrastructure-004 (full integration) depends on it and replaces all Fabric.js bindings and rendering code. Both tasks include prior art links to infrastructure-001 and infrastructure-002 and cross-reference ADRs 0001/0002/0003.

---

## 2026-05-23 -- Work session ended

**Type:** Work / Session end
**Completed:** 4 (first-try PASS: 3 [git-canvas-001, scenario-001, infrastructure-001], skipped: 1 [infrastructure-002])
**Bounced:** 0
**Failed:** 0
**Escalated after verification:** 0
**Commits:** 0 (deferred — user chose to skip commits this session)

---

## 2026-05-23 -- Task verified and completed: infrastructure-001 - Walking skeleton: multi-repo proof-of-concept

**Type:** Work / Task completion
**Task:** infrastructure-001 - Walking skeleton: multi-repo proof-of-concept
**Summary:** Two GitGraph instances ("origin" and "alice") coexist on shared Fabric.js canvas at fixed positions 800px apart. Click-to-focus activation with colored border indicator. All control panel operations route to active repo. Production webpack build passes cleanly.
**Verification:** PASS (iteration 1)
**Files changed:** 6
**ADRs written:** none

---

## 2026-05-23 -- Batch started: [infrastructure-001]

**Type:** Work / Batch start
**Tasks:** infrastructure-001 - Walking skeleton: multi-repo proof-of-concept
**Parallel:** no (1 worker)

---

## 2026-05-23 -- Task completed (verification skipped): infrastructure-002 - Migrate canvas foundation from Fabric.js to Konva.js

**Type:** Work / Task completion
**Task:** infrastructure-002 - Migrate canvas foundation from Fabric.js to Konva.js
**Summary:** ADR 0003 written confirming Konva.js migration. Fabric.js v5 rejected (abandoned, unpatched CVE); Fabric.js v7 not preferred (near-complete binding rewrite); Konva.js v10.x adopted. Dukat spike documented as environment-blocked.
**Verification:** SKIPPED — decision-only task, single ADR file
**Files changed:** 1
**ADRs written:** 0003-migrate-canvas-foundation-to-konvajs.md

---

## 2026-05-23 -- Task verified and completed: scenario-001 - URL encoding format for graph snapshots

**Type:** Work / Task completion
**Task:** scenario-001 - URL encoding format for graph snapshots
**Summary:** ADR 0002 written adopting JSON + gzip + base64url in URL fragment. BC README updated. Consequences section added after verifier flagged its absence; ADR numbering fixed after parallel-worker rename.
**Verification:** PASS (iteration 1, orchestrator-fixed numbering)
**Files changed:** 2
**ADRs written:** 0002-url-encoding-format-for-graph-snapshots.md

---

## 2026-05-23 -- Task verified and completed: git-canvas-001 - Multi-repo canvas layout strategy

**Type:** Work / Task completion
**Task:** git-canvas-001 - Multi-repo canvas layout strategy
**Summary:** ADR 0001 written adopting shared infinite canvas with click-to-focus and faded inactive repos. BC README updated.
**Verification:** PASS (iteration 1)
**Files changed:** 2
**ADRs written:** 0001-multi-repo-canvas-layout.md

---

## 2026-05-23 -- Batch started: [git-canvas-001, scenario-001, infrastructure-002]

**Type:** Work / Batch start
**Tasks:** git-canvas-001 - Multi-repo canvas layout strategy, scenario-001 - URL encoding format for graph snapshots, infrastructure-002 - Migrate canvas foundation from Fabric.js to Konva.js
**Parallel:** yes (3 workers)

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
