# Git Whiteboard App — Claude Guidance

## Project Summary

A stateless Kotlin/JS browser application for drawing and manipulating Git commit graphs. Intended for Git training and demos. No server persistence; the JVM server (`Server.kt`) is for local development only.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.x, Kotlin Multiplatform (JS + JVM targets) |
| Canvas rendering | Konva.js 10.3 via handwritten Kotlin external bindings (ADR-0003) |
| CSS | Bulma framework + Font Awesome |
| Local dev server | Ktor 3.x on Netty (JVM target) |
| Build | Gradle with Kotlin wrapper (`sh gradlew`) |
| Docs | Asciidoctor → HTML5 |
| JDK | 17 (via Gradle toolchains) |

## Repository Layout

```
src/jsMain/kotlin/
  App.kt                    # DOM wiring: buttons → GitGraph calls
  Main.kt                   # Entry point, calls startApp()
  graph/
    GitGraph.kt             # Core graph state + Git semantics + Konva rendering (tightly coupled)
    Commit.kt               # Commit node: position, branches, rendering
    Branches.kt             # Branch/Tag/Head abstractions
    BranchColors.kt         # Color cycling for branches
    CommitIdAllocator.kt    # App-wide commit ID numbering shared by all repos (ADR-0004)
    RemoteOps.kt            # Push/fetch/pull orchestration between a local repo and its remote (ADR-0005)
  components/               # Konva-rendered visual elements (circles, lines, labels, RepoRegion)
  konvajs/                  # Kotlin external bindings for Konva.js (handwritten)
  scenario/                 # Graph snapshot serialization for shareable URLs (ADR-0002)
  config/GitGraphConfiguration.kt  # Layout constants (offsets, distances)
  ui/                       # DOM helpers and dialogs

src/commonMain/resources/index.html   # Static HTML control panel shell
src/jvmMain/kotlin/Server.kt          # Ktor dev server (serves built JS assets)
src/docs/asciidoc/manual.adoc         # User manual source
try-it-out/                           # Prebuilt static version for quick demo
```

## Build Commands

```bash
# Default build (JS webpack + manual)
sh gradlew

# Full verification
sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all

# Run local dev server → http://localhost:9090
sh gradlew run

# Update JS dependency lock file after npm changes
sh gradlew kotlinUpgradeYarnLock
```

Use `sh gradlew` (not `./gradlew`) unless the executable bit is confirmed set.

## Architecture Notes

- **`GitGraph`** combines graph state, Git-like operations, and Konva rendering in one class. Visual behavior and graph semantics are tightly coupled — change it carefully.
- Konva.js is accessed via handwritten external bindings (`konvajs/`). When upgrading Konva, verify the JavaScript module export shape in the browser, not just Kotlin compilation.
- Multi-repo: one Konva `Stage` (global pan/zoom) holds one `Layer` per repo; each `GitGraph` draws inside a draggable `RepoRegion`. Up to 3 repos; clicking a region makes it the active repo that receives control-panel commands (ADR-0001).
- The app initializes directly from `Main.kt` → `App.kt` → DOM. Do not reintroduce React to mount the canvas.
- Swimlane layout: commits are positioned on a 2D grid. `swimlane` = horizontal axis, `linePosition` = vertical axis. `GitGraph` manages `globalSwimlaneCounter` and `globalCommitNumber`.
- `CommitIdAllocator` hands out commit IDs (`<branch short code><n>`, ADR-0007) for all repos, so the same ID in two repos means the same commit. `globalCommitNumber` is only the vertical layout slot, not an ID source.
- `RemoteOps` (`graph/RemoteOps.kt`) orchestrates push, fetch and pull between a local `GitGraph` and the one it was cloned from; remote-tracking labels (`origin/<name>`) are `RemoteBranch`es held apart from local branches.
- Branch names follow Git ref-name rules (`branchNameProblem`); each local branch has a 1-2 char short code (`Branch.shortCode`) used as commit-ID prefix (ADR-0007).

## Testing

No unit tests exist. For any non-trivial change, run the full verification build. For UI or graph behavior changes, also start the server and smoke-test:

- Page title is `Git Whiteboard App`
- Control panel renders
- Initial canvas shows repo `my-repo` with commit `m1`, branch `main`, and `HEAD`
- Adding a commit updates the graph
- Adding a second repo and switching the active repo works
- Browser console has no new runtime errors

Known non-blocking: Gradle/Kotlin tooling warnings, Webpack bundle size warnings, Netty startup warnings, Asciidoctor/JRuby warnings.

## Style Rules

- **Project language is English.** All source code, comments, and instruction files must be in English. Translate any German input before writing to the repository.
- Follow existing Kotlin style; keep changes narrowly scoped.
- Prefer ASCII in source files unless the file already uses non-ASCII.
- Avoid broad refactors while fixing narrow behavior.
- Keep comments sparse: explain tricky graph/rendering interactions, not obvious assignments.
- Use `textContent`/`classList` (DOM-safe APIs) for new UI code.

## Git Safety

- Check `git status --short` before editing and before summarizing work.
- Do not commit generated `build/` artifacts.
- Do not use destructive git commands unless explicitly requested.
- If a local dev server is started for verification, stop it before finishing.
- `kotlin-js-store/yarn.lock` must stay in sync; update it via `kotlinUpgradeYarnLock` when npm deps change.

## Live Demo

https://git-whiteboard.netlify.app/
