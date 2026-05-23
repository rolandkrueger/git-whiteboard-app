---
id: "0001"
title: Multi-repo canvas layout strategy
status: accepted
date: 2026-05-23
scope: git-canvas
related_tasks: [git-canvas-001]
related_research: []
---

# 0001: Multi-repo canvas layout strategy

## Context

The app currently renders exactly one GitGraph on a single Fabric.js canvas. Supporting remote Git operations — push, pull, clone — requires showing at minimum one remote repo and two local clones simultaneously on the same screen so trainers can narrate both sides of the operation without switching views.

The chosen layout strategy shapes all downstream multi-repo work: how repos are created and named, how the control panel determines which repo is "active," and how pan/zoom behaves.

Four options were considered.

## Decision

**Option C — Single shared infinite canvas** is adopted.

All repos live on one Fabric.js canvas. The trainer positions repo regions freely by dragging their content. The app enforces no fixed panel boundaries.

### Spatial arrangement

Repos are free-floating regions on the shared canvas. Placement is entirely at the trainer's discretion. A clearly visible label banner (e.g. "origin", "alice", "bob") sits above each repo region so the audience can always identify which repo is which.

### Active repo selection

Clicking anywhere inside a repo region makes it the active repo. All subsequent commands from the control panel (add commit, create branch, checkout, etc.) are routed to the active repo only. Only one repo is active at a time.

### Active/inactive visual indicator

- Active repo: rendered in full saturated colors (normal, unchanged appearance).
- Inactive repos: faded or desaturated, clearly subordinate.

This gives the trainer an immediate at-a-glance signal under presenter pressure with no need to consult a separate UI widget.

### Pan and zoom

Global — pan and zoom apply to the whole shared canvas. Independent per-repo zoom is not required and would add disproportionate implementation complexity.

## Rationale

- Both repos must always be simultaneously visible. Switching tabs or panels during a push/pull demo destroys the "see both sides" teaching moment that is the core purpose of this feature.
- Two repos is the dominant use case; the shared-canvas approach handles a third repo (e.g. a second developer clone) gracefully without fixed-panel cramping.
- The trainer explicitly needs spatial control over repo arrangement to suit the audience's reading direction and the specific operation being demonstrated.
- Existing Fabric.js pan/zoom (Ctrl+scroll, drag) already provides shared-canvas navigation with no changes required. No new pan/zoom infrastructure is needed.

## Rejected options

**Option A: Fixed side-by-side panels.**
Rejected. Fixed positions deny the trainer control over spatial arrangement. Two fixed halves also become cramped when a third repo is needed.

**Option B: Tabbed repos.**
Rejected. Tabs make only one repo visible at a time. This directly contradicts the teaching goal of showing push/pull as a two-sided operation.

**Option D: Floating resizable windows (within the canvas).**
Rejected. Fabric.js is not a window manager. Implementing draggable, resizable window chrome inside a canvas element adds substantial complexity that Option C already avoids — free dragging of repo content achieves the same positioning freedom without synthetic window chrome.

## Consequences

- The GitGraph aggregate must gain a repo identity (name/label) and an active/inactive rendering mode.
- A click-detection mechanism must route canvas clicks to the correct GitGraph and signal "this repo is now active."
- The control panel must always reflect which repo is currently active so the trainer does not accidentally operate the wrong repo.
- Visual desaturation of inactive repos must be implemented consistently across all Fabric.js components (commit nodes, branch labels, edges).
- No pan/zoom changes are required.
