# Vision: Git Whiteboard App

## Purpose
A stateless, browser-based visual tool for Git trainers to draw and manipulate Git commit graphs live during training sessions. The trainer operates the tool; the audience sees the result projected. It replaces the whiteboard without losing the whiteboard's immediacy.

## Users
**Git Trainer** — the sole operator. Uses the tool during live training sessions to illustrate Git concepts by building and manipulating graphs on the fly. Needs to react instantly to audience questions without preparation overhead.

The audience is a consumer only — they see the projected result but never interact with the tool.

## The problem
Explaining Git dynamics on a whiteboard is slow and messy: drawing commit nodes takes time, erasing and redrawing branch labels breaks the flow, and space runs out quickly. Slides are static. Live Git on the command line requires many irrelevant setup steps. Neither option lets a trainer show "now HEAD moves here" with a single click.

For remote operations (push, pull, clone, conflict resolution), the problem is even sharper: there is no existing tool that lets a trainer show two or three repos evolving simultaneously without switching context or losing the audience.

## What success looks like
- A trainer can go from zero to a rebase demo in under 30 seconds
- Push/pull/clone between one remote and two local clones is visually obvious on screen
- Conflict resolution scenarios can be demonstrated without leaving the tool
- A URL-encoded starting scenario can be prepared once and opened in seconds at the start of a session

## Non-goals
- No server-side persistence — the tool stays stateless and self-contained
- No simulation of Git internals (object model, pack files, refs storage)
- No multi-user collaboration — one trainer, one session
- No realistic commit IDs — simplified IDs (m1, f2, …) are intentional
- No production Git operations — the tool is a teaching aid, not a Git client

## Ubiquitous language (seed)
- **Graph** — a visual representation of one Git repository's commit history
- **Commit** — a node in the graph with a simplified ID and optional branch/tag labels
- **Swimlane** — horizontal axis position, used to separate parallel branch histories
- **Line position** — vertical axis position, representing commit order in time
- **HEAD** — the pointer label showing the current checkout state
- **Branch label** — a named pointer attached to a commit node
- **Tag label** — an immutable named pointer attached to a commit node
- **Local repo** — a graph representing a developer's local clone
- **Remote repo** — a graph representing the upstream repository (origin)
- **Scenario** — a named initial graph state, optionally encoded in a URL, used as a starting point for a demo

## Open questions
- How many local repos should be displayable simultaneously? (Current thinking: at least 2 + 1 remote)
- What is the canvas layout strategy for multi-repo: side-by-side panels, floating windows, or a single shared canvas with labelled regions?
- What is the URL encoding format for scenarios? (binary, JSON, custom DSL?)
- Should scenarios be bundled with the app (preset library) or purely user-generated via URL?
