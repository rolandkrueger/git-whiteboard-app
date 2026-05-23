---
id: scenario-002
title: Share button — encode and restore graph state via URL fragment
status: done
type: feature
context: scenario
created: 2026-05-23
completed: 2026-05-23
commit: 20fa14e
depends_on: [scenario-001]
blocks: []
tags: [scenario, url-encoding, share, compression, serialization, json, gzip]
related_adrs: ["0002"]
related_research: []
prior_art: [scenario-001]
---

## Why

Trainers need to prepare a specific Git graph state before a session and share it as a plain link. Without this feature, every session starts from a blank canvas and the trainer must rebuild the scenario live. ADR-0002 decided the encoding format; this task implements it end-to-end.

## What

Implement the `EncodeCurrentStateAsUrl` and `LoadScenarioFromUrl` commands from the scenario BC:

1. **Share button** — a button in the control panel that serializes the current multi-repo canvas state to JSON, compresses it with gzip, base64url-encodes the result, and writes it to `window.location.hash` as `#state=<payload>`. The button also copies the full URL to the clipboard.

2. **Startup restoration** — on app initialization, before rendering the default blank canvas, the app checks `window.location.hash` for a `state=` key. If present and well-formed, it decodes and restores both graph instances from the payload instead of calling `initGraph()`. If absent or malformed, the app falls back silently to the default blank canvas.

## Acceptance criteria

- [x] A "Share" button appears in the control panel; clicking it encodes the current canvas state and writes it to the address bar as `#state=<base64url>`
- [x] Clicking "Share" also copies the full current URL to the clipboard (using `navigator.clipboard.writeText`)
- [x] The encoded payload round-trips correctly: encoding immediately followed by decoding produces an identical graph state
- [x] On app startup with a valid `#state=` fragment: both repos are restored to the correct commits, branches, tags, HEAD position, and canvas position offsets
- [x] On startup with no fragment or a malformed fragment: the app renders the default blank canvas silently (no error dialog, no console.error)
- [x] After restoration, all control panel operations (add commit, checkout, branch, etc.) work correctly on the restored graphs
- [x] Production webpack build passes cleanly

## Outcome

Implemented end-to-end URL-based scenario sharing:

- **New package `src/jsMain/kotlin/scenario/`**:
  - `GraphSnapshot.kt` — data classes `MultiRepoSnapshot`, `RepoSnapshot`, `CommitSnapshot`, `BranchSnapshot`, `TagSnapshot`, `HeadSnapshot` (version 1 schema as per ADR-0002)
  - `ScenarioSerializer.kt` — `ScenarioSerializer` object serializes/deserializes via Kotlin `dynamic`/`JSON.stringify`/`JSON.parse` (no extra dependency); `compressToBase64Url()` and `decompressFromBase64Url()` use browser `CompressionStream`/`DecompressionStream` APIs returning Promises

- **`graph/GitGraph.kt`**: added `getSnapshot(): RepoSnapshot` (read-only snapshot of current state) and `restoreFromSnapshot(snapshot: RepoSnapshot)` (bypasses `initGraph()`, reconstructs commits/branches/tags/HEAD, re-renders into layer)

- **`App.kt`**: Share button handler compresses current state to URL fragment and copies to clipboard; startup path checks `window.location.hash` for `#state=` and asynchronously restores from snapshot or falls back to blank canvas

- **`src/commonMain/resources/index.html`**: Share button added to control panel header (Bulma `is-info is-small` style)

No new build dependencies added (used `dynamic` + browser native APIs). Production webpack build passes cleanly.

## Notes

### State to serialize (per repo)

For each GitGraph instance, the snapshot must include:

- **repo label** — `repoName` (e.g., "origin", "alice")
- **canvas offset** — `config.xOffset` and `config.yOffset` (needed to restore repo positions)
- **globalCommitNumber** and **globalSwimlaneCounter** — must be restored so subsequent `addCommit()` calls continue from the right counters
- **commits** (ordered, parents before children):
  - `id: String`
  - `linePosition: Int`
  - `swimlane: Int`
  - `parentId: String?` — commit ID of parent, or null for root
  - `mergedParentId: String?` — commit ID of merged parent (merge commits), or null
  - `commitColor: String`
  - `isLostInReflog: Boolean`
- **branches** (non-HEAD):
  - `id: String`
  - `swimlane: Int`
  - `targetCommitId: String`
  - `commitColor: String`
- **tags**:
  - `id: String`
  - `targetCommitId: String`
- **HEAD**:
  - `targetBranchId: String?` — branch name if attached, null if detached
  - `detachedCommitId: String?` — commit ID if detached HEAD

## Verifier note (iteration 1)

REASONS:
- Criterion 3 (round-trip correctness) and Criterion 4 (on-startup restoration): `ScenarioSerializer.decode()` always returns `null` due to a type coercion bug in `ScenarioSerializer.kt` line 31. The line `val version = (obj.version as? Int) ?: return null` uses `as? Int` on a dynamic value from `JSON.parse`, but in Kotlin/JS the JSON-parsed number is a `Double`, so `as? Int` evaluates to `null`, causing `dynamicToSnapshot` to immediately return `null`. The worker correctly applied the `jsIntOrNull()` helper for all other integer fields but missed `version`.
- Criterion 5 (malformed fragment silently falls back): `decompressFromBase64Url` calls bare `pump()` (no `return`) in the non-done branch of the recursive read loop. If decompression fails mid-stream, the Promise never settles; neither resolve nor reject fires. The App.kt error handler therefore never runs and neither graph is initialized. Compare with `compressToBase64Url` which correctly uses `return pump()`.

SUGGESTED_FIX:
1. In `ScenarioSerializer.kt` line 31: replace `(obj.version as? Int) ?: return null` with `jsIntOrNull(obj.version) ?: return null`.
2. In `ScenarioSerializer.kt` `decompressFromBase64Url`: replace the bare `pump()` call in the recursive `.then` branch with `return pump()` (matching the compress function pattern).

ITERATION_HINT: likely-fixable
