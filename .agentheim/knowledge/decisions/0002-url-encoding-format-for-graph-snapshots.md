---
id: "0002"
title: URL encoding format for graph snapshots
status: accepted
date: 2026-05-23
scope: scenario
related_tasks: [scenario-001]
related_research: []
---

# ADR 0002: URL encoding format for graph snapshots

## Status

Accepted

## Context

The Git Whiteboard App is stateless — no server, no database. The only persistence mechanism available is the browser URL. Trainers need a way to prepare a specific graph state before a session and share or re-open it as a plain link.

The serialized format must:
- Encode enough state to reconstruct the visible graph faithfully
- Stay within browser URL length limits (practical ceiling: ~2000–8000 characters depending on browser)
- Remain shareable as a plain link

Typical scenarios are small: a few commits and branches per repo. The URL is always app-generated; it is never hand-authored or edited by trainers.

Three options were considered:

**Option A — JSON + gzip + base64url in the URL fragment**
Serialize the graph state as JSON, compress with gzip (browser-native API), encode as base64url, place in the URL fragment as `#state=<base64url>`.

**Option B — Compact custom DSL**
Invent a terse, hand-editable text format specific to this domain.

**Option C — Compressed binary (msgpack or similar) + base64url**
Use a binary serialization format instead of JSON before compressing and encoding.

## Decision

**Option A is chosen: JSON + gzip + base64url in the URL fragment.**

## Rationale

- Typical scenarios contain only a few commits and branches per repo. URL length is not a concern in practice.
- The URL is always app-generated; hand-editability offers no value. JSON verbosity is not a drawback.
- Fragment placement (`#state=…`) means the encoded state is never sent to a server, avoiding backend routing or analytics concerns.
- Gzip compression (via the browser's `CompressionStream` API) keeps payloads compact with no additional dependencies.
- JSON is easy to inspect in a browser console during development and debugging.
- No library dependency beyond what every modern browser already ships.

## What is encoded

For each repo:
- All commits: id, swimlane, linePosition, parent commit id, mergedParent commit id (if any), display color
- All branches and tags: name, target commit id, swimlane, display color
- HEAD state: attached or detached, target (branch name or commit id)

For multi-repo scenarios:
- Which repos exist
- Their labels (e.g., origin, alice, bob)
- Their canvas position offsets

## URL placement

Fragment: `#state=<base64url>`

The fragment is never sent to a server. There are no backend routing concerns. The `#state=` prefix makes the parameter easily identifiable if other fragment keys are added later.

## UI

A single button in the control panel (label: "Share" or "Copy link") serializes the current canvas state, writes it to `window.location.hash`, and may optionally copy it to the clipboard. No confirmation dialog is required.

## Startup behavior

On app initialization, before rendering the default blank canvas, the app checks `window.location.hash` for a `state=` key. If present, it decodes and decompresses the payload and initializes the graph(s) from that state instead of the default.

If the fragment is absent or malformed, the app falls back to the default blank canvas silently (no error dialog).

## Length handling

None. If the URL exceeds browser limits, the link simply does not work. No error message or fallback is required. Trainers are expected to keep scenarios to a reasonable size.

## Rejected options

**Option B: Compact custom DSL** — Rejected. Hand-editability is not a requirement. A bespoke parser adds ongoing maintenance cost with no benefit given that scenario sizes are small.

**Option C: Compressed binary (msgpack or similar) + base64url** — Rejected. URL length is not a concern, and binary formats are harder to debug without tooling. Option A is sufficient and simpler.

## Consequences

- The app gains a dependency on `CompressionStream` / `DecompressionStream` (available in all modern browsers since 2022; no polyfill needed for the target audience).
- The URL fragment is the authoritative state carrier; the app must check `window.location.hash` on every startup before rendering the default canvas.
- URL length is unconstrained — if a trainer creates a pathologically large scenario the link will silently fail. This is accepted.
- The state schema (commit shape, branch shape, HEAD encoding) must remain stable or include a version field; schema changes are breaking for stored URLs.
- Multi-repo canvas positions must be included in the serialized state so that repo layout is preserved when a scenario is reopened.
