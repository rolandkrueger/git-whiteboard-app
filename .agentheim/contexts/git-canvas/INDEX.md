# git-canvas — Index

Catalog of everything in this bounded context: tasks by status, ADRs scoped to this BC,
research touching this BC, and concept synthesis pages.

> Updated by: `model` (tasks), `work` (BC-scoped ADRs, concept page links), `research` (BC-scoped reports).

---

## Tasks by status

<!-- task-counts:start -->
- **Backlog:** 4
- **Todo:** 0
- **Doing:** 1
- **Done:** 28
<!-- task-counts:end -->

### Todo
<!-- todo-list:start -->
<!-- todo-list:end -->

### Doing
<!-- doing-list:start -->
- **git-canvas-cdy8k** — Free branch names - replace first-letter uniqueness with Git ref-name rules and per-branch short codes (1-2 chars) for commit IDs that fit the commit circle (feature) — `doing/git-canvas-cdy8k-branch-short-codes-and-git-ref-names.md`
<!-- doing-list:end -->

### Done (most recent first)
<!-- done-list:start -->
- **git-canvas-pjz9w** — Check out remote-tracking branches like real Git - origin/<name> gives a detached HEAD, <name> without a local branch creates a local tracking branch (DWIM) (feature) — `done/git-canvas-pjz9w-checkout-remote-branches.md`
- **git-canvas-sxqsj** — Draw commit edges GitKraken-style - straight vertical runs in a lane with a short rounded bend where the edge changes lanes (feature) — `done/git-canvas-sxqsj-gitkraken-style-commit-edges.md`
- **git-canvas-kngx5** — Double the font size of repo header labels and the origin connector label; repo name and connector label bold (feature) — `done/git-canvas-kngx5-larger-repo-header-labels.md`
- **git-canvas-nymhr** — Rebase and pull (rebase) stop mid-way on a conflict until Continue or Abort (feature) — `done/git-canvas-nymhr-rebase-conflict-pause.md`
- **git-canvas-xt4ch** — Draw label connectors as thin blue Bezier-curved arrows pointing toward the commit, so they no longer look like commit edges (feature) — `done/git-canvas-xt4ch-label-connector-arrows.md`
- **git-canvas-q38zx** — Raise the small info texts in the control panel (Bulma help / is-size-7) to the standard body font size (feature) — `done/git-canvas-q38zx-control-panel-text-size.md`
- **git-canvas-r3dq7** — Render remote-tracking labels in dark red instead of light grey, so they are readable and match the common red convention for remote branches (feature) — `done/git-canvas-r3dq7-remote-label-dark-red.md`
- **git-canvas-c35ww** — Merge and pull (merge) stop on a conflict until Continue or Abort (feature) — `done/git-canvas-c35ww-merge-conflict-pause.md`
- **git-canvas-mrfrh** — Conflict marker on commits (set, render, share, transfer) (feature) — `done/git-canvas-mrfrh-conflict-demonstration.md`
- **git-canvas-vj6gr** — Sync the user manual with the current app (chore) — `done/git-canvas-vj6gr-sync-user-manual.md`
- **git-canvas-wcbec** — Re-layout repo regions when they grow into each other (feature) — `done/git-canvas-wcbec-relayout-repos-on-growth.md`
- **git-canvas-nywqs** — Grey out the remote repo's HEAD label to hint at a bare repository (feature) — `done/git-canvas-nywqs-remote-as-bare-repo.md`
- **git-canvas-f2ced** — Highlight the remote repo and connect clones to their origin (feature) — `done/git-canvas-f2ced-highlight-remote-repo.md`
- **git-canvas-dxt9c** — Place clones below their origin in a triangle layout (feature) — `done/git-canvas-dxt9c-clone-placement-below-origin.md`
- **git-canvas-a95rx** — HEAD label stays on the previous commit after restoring a snapshot or cloning (bug) — `done/git-canvas-a95rx-head-label-stuck-after-restore.md`
- **git-canvas-bdecz** — Remote operations documentation and doc sync (feature) — `done/git-canvas-bdecz-remote-operations-documentation.md`
- **git-canvas-ktja3** — Pull with rebase option (feature) — `done/git-canvas-ktja3-pull-with-rebase.md`
- **git-canvas-sz7p8** — Pull as fetch plus merge (fast-forward or merge commit) (feature) — `done/git-canvas-sz7p8-pull-fetch-and-merge.md`
- **git-canvas-rx1y3** — Fetch updates tracking labels and brings in missing commits (feature) — `done/git-canvas-rx1y3-fetch-remote-tracking-update.md`
- **git-canvas-w3ndg** — Push with fast-forward and visible rejection (feature) — `done/git-canvas-w3ndg-push-with-fast-forward-and-rejection.md`
- **git-canvas-r6szj** — Remote tab in control panel with remote status (feature) — `done/git-canvas-r6szj-remote-tab-in-control-panel.md`
- **git-canvas-wv6va** — Clone repository with origin remote (feature) — `done/git-canvas-wv6va-clone-repository-with-origin-remote.md`
- **git-canvas-b5f0r** — Remote-tracking label model and rendering (feature) — `done/git-canvas-b5f0r-remote-tracking-label-model-and-rendering.md`
- **git-canvas-j2m8z** — Shared commit ID allocator across repos (feature) — `done/git-canvas-j2m8z-shared-commit-id-allocator.md`
- **git-canvas-004** — User-configurable initial repository name — `done/git-canvas-004-user-configurable-initial-repository-name.md`
- **git-canvas-003** — RepoRegion auto-resize — frame must grow with graph content — `done/git-canvas-003-repo-region-auto-resize.md`
- **git-canvas-002** — Initial 'main' commit for all new graphs — fix detached HEAD regression — `done/git-canvas-002-initial-main-commit-for-new-graphs.md`
- **git-canvas-001** — Multi-repo canvas layout strategy (decision) — `done/git-canvas-001-multi-repo-layout-decision.md`
<!-- done-list:end -->

### Backlog
<!-- backlog-list:start -->
- **git-canvas-snjqw** — Add interactive rebase (rebase -i) - squash/fixup, drop, reorder commits — `backlog/git-canvas-snjqw-interactive-rebase.md`
- **git-canvas-m7z8d** — Add merge --squash - a single commit without a second parent — `backlog/git-canvas-m7z8d-merge-squash.md`
- **git-canvas-vbxva** — Add force push, both plain --force and --force-with-lease — `backlog/git-canvas-vbxva-force-push-with-and-without-lease.md`
- **git-canvas-maf17** — Add git revert - a new commit that undoes an earlier one — `backlog/git-canvas-maf17-revert.md`
<!-- backlog-list:end -->

## ADRs scoped to this BC

<!-- adr-local:start -->
- **0007** — Branch short codes for commit IDs and Git ref-name rules instead of first-letter uniqueness — `knowledge/decisions/0007-branch-short-codes-and-git-ref-name-rules.md`
- **0006** — Conflict demonstration without file contents: a commit-level conflict marker; merge/pull and rebase stop on a conflict until Continue or Abort — `knowledge/decisions/0006-conflict-demonstration-without-file-contents.md`
- **0001** — Multi-repo canvas layout strategy — `knowledge/decisions/0001-multi-repo-canvas-layout.md`
- **0004** — Shared commit ID allocator across repos — `knowledge/decisions/0004-shared-commit-id-allocator.md`
- **0005** — Remote operations model — `knowledge/decisions/0005-remote-operations-model.md`
<!-- adr-local:end -->

## Research touching this BC

<!-- research-local:start -->
<!-- research-local:end -->

## Concepts

<!-- concepts:start -->
<!-- concepts:end -->

## Pointers

- BC README (ubiquitous language, invariants): `README.md`
