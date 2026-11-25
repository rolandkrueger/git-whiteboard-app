---
id: git-canvas-q38zx
title: Raise the small info texts in the control panel (Bulma help / is-size-7) to the standard body font size
status: doing
type: feature
context: git-canvas
created: 2026-11-25
completed:
depends_on: []
blocks: []
tags: [control-panel, readability, ui]
related_adrs: []
related_research: []
prior_art: [git-canvas-r6szj]
---

## Why
Info and status texts in the control panel (e.g. the `Fast-forward` result message on the
Remote tab, `Maximum of 3 repositories reached.`, the remote status lines) are rendered at
Bulma's smallest size (0.75rem) and are hard to read, especially in trainings and on a
projector. They should use the same standard size as the rest of the panel text.

## What
Every control-panel text that is currently sized like the `Fast-forward` message is set to
the standard body font size (Bulma's default, 1rem). In `src/commonMain/resources/index.html`
these are the elements using Bulma's `help` class or `is-size-7`:

- `#activeRepoLabel` ("Active: <repo>") — `is-size-7`
- `label[for=repoNameInput]` ("Repository name") — `label is-size-7`
- `#repoNameError`, `#addRepoNameError` — `help is-danger`
- Remote tab status lines (Remote / Upstream / Ahead/behind) — `is-size-7`
- `#cloneRepoHint` ("Maximum of 3 repositories reached.") — `help`
- The `rebase` checkbox label — `checkbox is-size-7`
- `#remoteMessage` (e.g. "Fast-forward", "Already up to date", push rejection) — `help`

Preferred approach: remove `is-size-7` from these elements, and override Bulma's `.help`
font size to the standard size in the existing `<style>` block (keeping `.help`'s colour
modifiers such as `is-danger` / `is-success` intact), so dynamically set messages pick it up
too. Any element created or re-classed from Kotlin with these classes must follow the same
rule.

Out of scope: buttons (e.g. the `is-small` share button), tab titles, headings, and the
canvas (see git-canvas-kngx5 for canvas label sizes).

## Acceptance criteria
- [ ] No element inside the control panel in `index.html` carries `is-size-7` any more.
- [ ] `.help` texts in the control panel compute to the same font size as regular body text (1rem), verified in the browser (e.g. `getComputedStyle(#remoteMessage).fontSize` equals the body paragraph size).
- [ ] Colour modifiers on help texts still work (e.g. a push rejection / name error still renders in its error colour, `Fast-forward` in its success colour).
- [ ] Full verification build passes: `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`.
- [ ] The control panel still looks tidy at the larger text size — no awkward wrapping or crowding on the General and Remote tabs. [human-eye]

## Notes
- Builder request (2026-11-25), with screenshot of the Remote tab showing the small
  `Fast-forward` message.
- The bold blue `Active: <repo>` label is included because it uses the same `is-size-7`
  size; keep its colour and weight.
