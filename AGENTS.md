# AGENTS.md

## Project Overview

Git Whiteboard App is a stateless Kotlin/JS web application for drawing and manipulating Git commit graphs in the browser. It is intended for Git training, demos, and lectures where a dynamic visual graph is more useful than slides or a physical whiteboard.

The core UI is a static HTML control panel plus a Fabric.js canvas. Kotlin/JS initializes the canvas and wires DOM controls to graph operations. A small Ktor JVM server exists for local development and serving the built static assets.

## Repository Layout

- `src/jsMain/kotlin/` contains the browser application.
- `src/jsMain/kotlin/graph/` contains the Git graph model and operation logic.
- `src/jsMain/kotlin/components/` contains canvas-rendered visual elements.
- `src/jsMain/kotlin/fabricjs/` contains local Kotlin external bindings for Fabric.js.
- `src/jsMain/kotlin/ui/` contains small DOM helper utilities and dialogs.
- `src/commonMain/resources/index.html` contains the static control panel and page shell.
- `src/jvmMain/kotlin/Server.kt` contains the local Ktor development server.
- `src/docs/asciidoc/manual.adoc` is the user manual source.
- `try-it-out/` contains a prebuilt static version.
- `kotlin-js-store/yarn.lock` is the Kotlin/JS npm lock file and should be kept in sync with JS dependency changes.

## Build And Run

Use the Gradle wrapper through `sh gradlew` unless the executable bit is known to be set.

- Build production JS and manual:
  ```bash
  sh gradlew
  ```
- Full verification build:
  ```bash
  sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all
  ```
- Run the local server:
  ```bash
  sh gradlew run
  ```
  Then open `http://localhost:9090`.

The project targets JDK 17 through Gradle toolchains.

## Development Notes

- Prefer Kotlin/JS and existing project patterns over introducing new frontend frameworks.
- Keep the app stateless and client-side. Do not add server persistence unless explicitly requested.
- `GitGraph` currently combines graph state, Git-like operations, and Fabric rendering. Be careful when changing it: visual behavior and graph semantics are tightly coupled.
- Fabric.js is used through handwritten external bindings. If upgrading Fabric, check the JavaScript module export shape in the browser, not only Kotlin compilation.
- The app currently uses direct DOM initialization from `Main.kt`/`App.kt`; do not reintroduce React just to mount the canvas.
- Keep generated build output out of commits. The `build/` directory is ignored.
- Keep `kotlin-js-store/yarn.lock` updated when npm dependencies change. Use:
  ```bash
  sh gradlew kotlinUpgradeYarnLock
  ```

## Testing And Verification

There are currently no dedicated unit tests. For nontrivial changes, at minimum run:

```bash
sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all
```

For UI or graph behavior changes, also run `sh gradlew run` and smoke-test the browser app:

- page title is `Git Whiteboard App`
- the control panel renders
- the Fabric canvas renders the initial `m1` commit, `master`, and `HEAD`
- adding a commit updates the graph visually
- browser console has no new runtime errors

Known non-blocking warnings may appear from Gradle/Kotlin tooling, Webpack bundle size, Netty, or Asciidoctor/JRuby. Treat new compiler errors, runtime JavaScript errors, or missing generated assets as blockers.

## Style Guidelines

- German may be used as the conversation language, but the project language is exclusively English. Before editing any instruction file, specification, or similar project guidance, translate German user-provided data into concise English and write only the English version to the repository.
- Use the existing Kotlin style and keep changes narrowly scoped.
- Prefer ASCII text in source files unless the existing file clearly uses non-ASCII.
- Avoid broad refactors while fixing narrow behavior.
- Keep comments sparse and useful; explain tricky graph/rendering interactions rather than obvious assignments.
- Use DOM-safe APIs such as `textContent`/`classList` for new UI code where possible.

## Git And Safety

- Do not overwrite user changes. Check `git status --short` before editing and before summarizing work.
- Do not commit generated `build/` artifacts.
- Do not use destructive git commands unless explicitly requested.
- If a local server is started for verification, stop it before finishing.
