# scenario

## Purpose
Serializes git-canvas graph states into URL-encoded strings and deserializes them back. Enables trainers to prepare starting scenarios and share them as links — no server required.

## Classification
supporting — valuable time-saver for trainers who teach the same topics repeatedly, but the app functions fully without it.

## Actors
- **Git Trainer** — generates scenario URLs before a session; opens them at the start of a demo

## Ubiquitous language
- **Scenario** — a named initial graph state, ready to load as the starting point of a demo
- **Snapshot** — the serialized representation of a GitGraph state at a point in time
- **URL encoding** — the mechanism for embedding a snapshot into a browser URL (query param or fragment)
- **Preset** — a scenario bundled with the app (no URL needed), selectable from a menu
- **Starting state** — the graph as it appears the moment a scenario URL is opened

## Aggregates
- **Scenario** — a snapshot + optional name; loaded once on app start if a URL param is present

## Key events
- ScenarioLoaded
- ScenarioEncoded

## Key commands
- LoadScenarioFromUrl
- EncodeCurrentStateAsUrl

## Relationships with other contexts
- **Conformist to:** git-canvas — the state shape is owned by git-canvas; scenario adapts to it

## Encoding format (decided — ADR 0002, implemented in scenario-002)

JSON + gzip + base64url in the URL fragment (`#state=<base64url>`). The fragment is never sent to a server. On startup the app checks for `#state=`; if present it deserializes and restores the graph instead of the default blank canvas. A "Share" button in the control panel triggers serialization, writes the encoded state to the address bar, and copies the full URL to the clipboard.

### Implementation shape

- `src/jsMain/kotlin/scenario/GraphSnapshot.kt` — data classes: `MultiRepoSnapshot`, `RepoSnapshot`, `CommitSnapshot`, `BranchSnapshot`, `TagSnapshot`, `HeadSnapshot`
- `src/jsMain/kotlin/scenario/ScenarioSerializer.kt` — `ScenarioSerializer` object (encode/decode via `JSON.stringify/parse` on dynamic objects); `compressToBase64Url()` and `decompressFromBase64Url()` (gzip via browser `CompressionStream`/`DecompressionStream` APIs, returning Promises)
- `graph/GitGraph.kt` — `getSnapshot(): RepoSnapshot` and `restoreFromSnapshot(snapshot: RepoSnapshot)` methods
- `App.kt` — Share button handler; startup hash detection and async restore path
- `index.html` — Share button in control panel header

## Open questions
- Should the app ship with a preset scenario library (cherry-pick demo, rebase demo, conflict demo)?
- Should the URL update live as the trainer manipulates the graph, or only on explicit "Share" action? (Current answer: explicit "Share" button, no live update.)
