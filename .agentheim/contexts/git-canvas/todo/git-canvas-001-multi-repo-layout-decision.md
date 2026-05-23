---
id: git-canvas-001
title: Multi-repo canvas layout strategy
type: decision
status: todo
bc: git-canvas
depends_on: []
---

# git-canvas-001: Multi-repo canvas layout strategy

## Goal
Decide how two or more Git repo graphs are displayed simultaneously on the Fabric.js canvas so that a trainer can see and operate all repos at a glance during a live session.

## Context
The current app renders exactly one GitGraph on a single Fabric.js canvas. Supporting remote operations (push, pull, clone) requires visualizing at minimum one remote repo + two local clones simultaneously. The chosen layout strategy shapes the entire multi-repo feature: how repos are created, how the control panel switches between them, and how pan/zoom works.

## Acceptance criteria
- An ADR is committed to `knowledge/decisions/` documenting the chosen layout approach
- The ADR covers: how repos are spatially arranged, how the active repo is indicated, how the trainer switches between repos, and how zoom/pan interacts with multi-repo view
- No code change required — this is a design decision only

## Decision (confirmed in refinement session 2026-05-23)

**Chosen approach: Option C — Single shared infinite canvas**

All repos live on one Fabric.js canvas. The trainer positions repo regions freely by dragging. No fixed panel boundaries are enforced by the app.

**Rationale:**
- Both repos must always be simultaneously visible — trainer never switches context while showing push/pull
- Two repos is the dominant case (occasionally three); crampedness of fixed panels is not a concern for the 2-repo case, but free placement handles the 3-repo case gracefully
- Trainer explicitly wants control over spatial arrangement (not app-fixed positions)
- Existing pan/zoom (Ctrl+scroll, drag) already supports a shared infinite canvas with no changes needed

**Active repo selection: click to focus**
Clicking anywhere on a repo region makes it the active repo. All subsequent operations (add commit, create branch, etc.) apply to the active repo only.

**Active/inactive visual indicator:**
- Active repo: rendered in full saturated colors (normal appearance)
- Inactive repos: faded or desaturated, clearly subordinate visually
- This gives the trainer an at-a-glance signal under presenter pressure without requiring a separate UI element

**Repo labeling:**
Each repo region has a clearly visible label banner (e.g. "origin", "alice", "bob") so the audience always knows which repo they are looking at.

**Pan/zoom:**
Global — applies to the whole shared canvas. Independent per-repo zoom is not required.

## Rejected options

**Option A: Fixed side-by-side panels** — Rejected because the trainer needs free control over spatial arrangement; fixed positions prevent this.

**Option B: Tabbed repos** — Rejected because both repos must be simultaneously visible; tabs break the "see both sides" teaching moment during push/pull.

**Option D: Floating resizable windows** — Rejected; Fabric.js is not a window manager and the implementation complexity is unjustified given Option C already provides free positioning.
