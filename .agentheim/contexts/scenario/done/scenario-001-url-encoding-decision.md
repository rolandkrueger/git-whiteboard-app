---
id: scenario-001
title: URL encoding format for graph snapshots
type: decision
status: done
completed: 2026-05-23
bc: scenario
depends_on: []
---

# scenario-001: URL encoding format for graph snapshots

## Goal
Decide how a git-canvas graph state (or set of states for multi-repo) is serialized into a URL so that trainers can share or reopen starting scenarios via a link.

## Context
The app is stateless — no server, no database. The only persistence mechanism available is the browser URL (query param or fragment). The serialized format must encode enough state to reconstruct the visible graph faithfully, while staying within URL length limits and remaining shareable as a plain link.

## Acceptance criteria
- An ADR is committed to `knowledge/decisions/` documenting the chosen encoding format
- The ADR covers: what state is encoded (commits, branches, tags, HEAD, multi-repo structure), the encoding mechanism, URL placement, and how the app detects and loads a scenario on startup
- No code change required — this is a design decision only

## Decision (confirmed in refinement session 2026-05-23)

**Chosen approach: Option A — JSON + gzip + base64url in the URL fragment**

**Rationale:**
- Typical scenarios contain only a few commits and branches per repo — URL length is not a concern in practice
- The URL is always app-generated; hand-editability offers no value, so the verbosity of JSON is not a drawback
- Fragment placement (`#state=…`) means the encoded state is never sent to a server
- Gzip compression keeps payloads compact for free; no downside
- If a pathological scenario exceeds browser URL limits, there is no special fallback — the trainer is expected to keep scenarios reasonable

**What is encoded:**
- For each repo: all commits (id, swimlane, linePosition, parent, mergedParent, color), all branches/tags (name, commit, swimlane, color), HEAD state (attached/detached, target)
- For multi-repo: which repos exist, their labels (origin, alice, bob), and their canvas positions

**URL placement:** Fragment (`#state=<base64url>`) — never sent to a server, no backend routing concerns.

**UI:**
A button in the control panel serializes the current canvas state and writes it to the browser URL (updating the address bar) so the trainer can copy and share the link. No confirmation dialog required.

**Startup behavior:**
On app initialization, the app checks for the `#state=` fragment. If present, it deserializes the payload and initializes the graph(s) from that state instead of the default blank canvas.

**Length handling:** None. If the URL exceeds browser limits, it simply does not work. No error message or fallback is required.

## Rejected options

**Option B: Compact custom DSL** — Rejected; hand-editability is not needed, and a bespoke parser adds maintenance cost with no benefit given small scenario sizes.

**Option C: Compressed binary (msgpack or similar) + base64url** — Rejected; URL length is not a concern, and binary formats are harder to debug. Option A is sufficient.

## Outcome

ADR 0001 written to `.agentheim/knowledge/decisions/0002-url-encoding-format-for-graph-snapshots.md`. The ADR documents the chosen encoding (JSON + gzip + base64url in `#state=` fragment), the state shape to serialize, the startup detection behavior, and the two rejected alternatives. The scenario BC README was updated to resolve the open encoding-format question.
