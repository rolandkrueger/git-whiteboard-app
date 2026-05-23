# infrastructure

## Purpose
Owns globally-true technical concerns: Gradle/Kotlin/JS build pipeline, local Ktor dev server, static hosting configuration, and cross-cutting runtime decisions. BC-local infrastructure (Fabric.js canvas adapters, component factories, rendering utilities) stays inside the originating BC (git-canvas).

## Classification
generic — build and deployment plumbing; no domain logic lives here.

## Actors
- None at runtime (automated pipeline)
- Developer (build and deploy operations)

## Ubiquitous language
- **Bundle** — the webpack-produced JS artifact deployed to the browser
- **Dev server** — the Ktor/Netty JVM server used locally during development (not deployed)
- **Static hosting** — the production deployment target (currently Netlify); serves the bundle and index.html
- **Yarn lock** — `kotlin-js-store/yarn.lock`, the npm dependency lockfile; must be kept in sync via `kotlinUpgradeYarnLock`

## Aggregates
None — infrastructure is a pipeline, not a domain model.

## Key events
None domain-relevant.

## Key commands
- `sh gradlew` — default build (JS bundle + manual)
- `sh gradlew run` — start local dev server at http://localhost:9090
- `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` — full verification build
- `sh gradlew kotlinUpgradeYarnLock` — sync yarn.lock after npm dependency changes

## Relationships with other contexts
- **Serves:** git-canvas and scenario — their code is compiled and bundled here

## Open questions
- None at project start; will accumulate as cross-cutting concerns surface.
