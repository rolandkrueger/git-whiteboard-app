---
id: infrastructure-007
title: Update README — multi-repo features, Konva.js migration, agentic development section
status: done
type: chore
context: infrastructure
created: 2026-05-25
completed: 2026-05-25
commit:
depends_on: []
blocks: []
tags: [readme, documentation, konvajs, multi-repo, agentic-development]
related_adrs: ["0001", "0003"]
related_research: []
prior_art: []
---

## Why

The project README still describes the original single-repo Fabric.js app. Since then, several significant changes have shipped and the README now actively misleads readers about the tech stack, feature set, and development methodology.

## What

Update `README.md` in the repository root with:

1. Updated feature list reflecting multi-repo capabilities
2. Corrected tech stack (Konva.js replaces Fabric.js; kotlin-react removed)
3. Corrected build command (`sh gradlew` not `./gradlew`)
4. New **Agentic Development** section explaining that further development is driven by Claude Code + the agentheim plugin

## Acceptance criteria

- [ ] Feature list updated to include: multiple repositories on a shared canvas (up to 3), "Add Repository" button for adding repos dynamically during a session, Share button (URL fragment encoding and restore of full session state).
- [ ] The sentence "More advanced features, such as working remotely with push, fetch, and pull, will be supported in a later version" is updated or removed — multi-repo canvas is now present, even if push/pull semantics are not yet implemented.
- [ ] Tech stack list in the Implementation section removes `fabric.js` and `kotlin-react`; adds `Konva.js` with a link to `https://konvajs.org/`.
- [ ] Build command updated from `./gradlew` to `sh gradlew` (both in "How to build and run" and any other occurrence).
- [ ] A new **Agentic Development** section is added (after the Implementation section) explaining that further development is carried out using agentic coding with Claude Code and the agentheim plugin, with links to both.
- [ ] Default branch name in any example output or description updated from `master` to `main`.
- [ ] No other prose is changed beyond what the criteria above require.

## Outcome

Updated `README.md` to reflect current state: added multi-repo features (multiple repos, Add Repository button, Share URL) to the feature list; replaced the "More advanced features" placeholder sentence; corrected build commands from `./gradlew` to `sh gradlew`; replaced `fabric.js` and `kotlin-react` with `Konva.js` in the tech stack list; added an Agentic Development section linking to Claude Code and the agentheim plugin.

Key file: `/Users/rkrueger/Projects/_Privat/git-whiteboard-app/README.md`

## Notes

### Tech stack list (current → updated)

Remove:
- `kotlin-react` (no longer used)
- `fabric.js Javascript HTML5 canvas library`

Add:
- `[Konva.js canvas library](https://konvajs.org/)` — replaces Fabric.js

Keep:
- Kotlin/JS, Ktor, Bulma, Font Awesome, Asciidoctor

### Agentic Development section

The section should explain that from a certain point (after the initial Konva.js migration), all further feature development and bug fixes are implemented using agentic coding with:

- **[Claude Code](https://claude.ai/code)** — Anthropic's AI coding agent (CLI + IDE extensions)
- **[agentheim plugin](https://github.com/heimeshoff/Agentheim)** — the agentheim domain-driven development plugin for Claude Code, which provides the model/work/brainstorm skill set used to manage the backlog and run parallel worker agents

### Screenshot

The existing screenshot (`docs/img/screenshot1.png`) still shows the old single-repo Fabric.js UI. If a new screenshot is available, replace it; if not, leave the existing one in place — do not remove it. Do not generate or fetch a screenshot.
