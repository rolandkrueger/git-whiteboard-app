# Git Whiteboard App — Claude Guidance

## Project Summary

A stateless Kotlin/JS browser application for drawing and manipulating Git commit graphs. Intended for Git training and demos. No server persistence; the JVM server (`Server.kt`) is for local development only.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.x, Kotlin Multiplatform (JS + JVM targets) |
| Canvas rendering | Fabric.js 5.3 via handwritten Kotlin external bindings |
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
    GitGraph.kt             # Core graph state + Git semantics + Fabric rendering (tightly coupled)
    Commit.kt               # Commit node: position, branches, rendering
    Branches.kt             # Branch/Tag/Head abstractions
    BranchColors.kt         # Color cycling for branches
  components/               # Fabric-rendered visual elements (circles, lines, labels)
  fabricjs/                 # Kotlin external bindings for Fabric.js (handwritten)
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

- **`GitGraph`** combines graph state, Git-like operations, and Fabric canvas rendering in one class. Visual behavior and graph semantics are tightly coupled — change it carefully.
- Fabric.js is accessed via handwritten external bindings (`fabricjs/`). When upgrading Fabric, verify the JavaScript module export shape in the browser, not just Kotlin compilation.
- The app initializes directly from `Main.kt` → `App.kt` → DOM. Do not reintroduce React to mount the canvas.
- Swimlane layout: commits are positioned on a 2D grid. `swimlane` = horizontal axis, `linePosition` = vertical axis. `GitGraph` manages `globalSwimlaneCounter` and `globalCommitNumber`.
- Branch name uniqueness is enforced by first-letter uniqueness (see `isBranchNameValid`).

## Testing

No unit tests exist. For any non-trivial change, run the full verification build. For UI or graph behavior changes, also start the server and smoke-test:

- Page title is `Git Whiteboard App`
- Control panel renders
- Initial canvas shows commit `m1`, branch `master`, and `HEAD`
- Adding a commit updates the graph
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
