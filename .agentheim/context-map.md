# Context Map

## Contexts

### git-canvas
- **Purpose:** Manages the Git graph model and its visual rendering on a Fabric.js canvas. All Git operations (commit, branch, merge, rebase, checkout, reset, push, pull) live here, as does the canvas layout logic (swimlanes, line positions, label placement).
- **Core language:** commit, branch, tag, HEAD, swimlane, line position, merge, rebase, detached HEAD, lost commit, reflog, remote-tracking branch
- **Classification:** core — this is the primary value the tool delivers; no other tool does this in the same way for trainers
- **Key actors:** Git Trainer (via control panel)

### scenario
- **Purpose:** Serializes and deserializes graph states to and from URL-encoded strings. Enables trainers to prepare and share starting scenarios without a server.
- **Core language:** scenario, snapshot, URL encoding, preset, starting state
- **Classification:** supporting — valuable but not differentiating; the app works without it
- **Key actors:** Git Trainer (prepares scenarios before a session)

### infrastructure
- **Purpose:** Owns globally-true technical concerns: build pipeline, local dev server, static hosting, and any cross-cutting runtime decisions. BC-local infrastructure (rendering adapters, component factories) stays inside the originating BC.
- **Core language:** build, bundle, deploy, dev server, static asset
- **Classification:** generic
- **Key actors:** none (automated)

## Relationships

- **git-canvas → scenario:** git-canvas is upstream. The scenario BC reads a git-canvas snapshot and restores it; it conforms to git-canvas's state model. Relationship: *Customer-supplier* (git-canvas supplies the state shape; scenario consumes it).
- **git-canvas → infrastructure:** git-canvas is served by the infrastructure build and hosting pipeline. Conformist relationship.
- **scenario → infrastructure:** scenario output (URL) is a client-side artifact; no server infrastructure required. Separate ways for runtime; shared build pipeline.
