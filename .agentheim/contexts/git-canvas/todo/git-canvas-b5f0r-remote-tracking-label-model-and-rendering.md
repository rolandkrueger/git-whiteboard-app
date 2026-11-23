---
id: git-canvas-b5f0r
title: Remote-tracking label model and rendering
status: todo
type: feature
context: git-canvas
created: 2026-11-23
completed:
depends_on: [git-canvas-j2m8z]
blocks: [git-canvas-wv6va]
tags: [remote-tracking, rendering, scenario]
related_adrs: [0005]
related_research: []
prior_art: []
---

## Why
Clone, fetch and push all need `origin/<branch>` labels. Introducing the label kind first isolates model and rendering work and settles the README open question about their visual style.

## What
- Add a `RemoteBranch` ref type held in its own list in `GitGraph` (not in `branches`).
- Render it distinctly: dashed outline, unfilled, text `origin/<name>`, attached to a commit like branch labels.
- Not draggable, deletable or checkout-able; not offered in branch/merge/rebase pickers; ignored by first-letter uniqueness.
- Reposition it together with the other labels when its commit changes.
- Add `upstream: String?` to `Branch` (data only).
- Snapshot: add `remoteBranches` to `RepoSnapshot` and `upstream` to `BranchSnapshot`; decode with defaults.
- Internal API: `setRemoteBranch(name, commit)`.
- No user-facing trigger in this task. A temporary dev hook (console function or debug button) may be used for verification and must be removed before finishing.

## Acceptance criteria
- [ ] Via the temporary hook, `origin/main` appears on a commit with a dashed, unfilled style clearly distinct from the local `main` label. [human-eye]
- [ ] Two labels on one commit (`main` and `origin/main`) do not overlap. [human-eye]
- [ ] The `origin/main` label cannot be dragged and does not appear in branch/merge/rebase/delete dropdowns.
- [ ] A local branch named `origin` or starting with `o` is not blocked by tracking labels (uniqueness check ignores them).
- [ ] Share URL, reload: the tracking label is restored on the same commit.
- [ ] The temporary dev hook is removed from the code.
- [ ] `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all` succeeds and the browser console shows no new runtime errors.

## Notes
New label class in `graph/Branches.kt` or a sibling file; rendering through the existing `components/` helpers. Update the git-canvas README: resolve the open question on tracking-label style, add the dashed style to the ubiquitous language. See ADR 0005.
