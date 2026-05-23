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

## Open questions
- What encoding format? (base64 JSON, compact binary, custom DSL)
- Should the app ship with a preset scenario library (cherry-pick demo, rebase demo, conflict demo)?
- Should the URL update live as the trainer manipulates the graph, or only on explicit "Share" action?
