---
id: git-canvas-cdy8k
title: Free branch names - replace first-letter uniqueness with Git ref-name rules and per-branch short codes (1-2 chars) for commit IDs that fit the commit circle
status: todo
type: feature
context: git-canvas
created: 2026-11-25
completed:
depends_on: []
blocks: []
tags: [branches, commit-id, naming, namespaces]
related_adrs: [0007, 0004]
related_research: []
prior_art: [git-canvas-j2m8z, git-canvas-pjz9w]
---

## Why
Branch names are currently restricted by a first-letter rule (`isBranchNameValid`): no two
branches may start with the same letter, and no name may start with `H`. It was a crutch for
readable commit IDs, but since ADR 0004 the allocator number alone keeps IDs unique. The rule
blocks realistic names - especially with namespaces (`feature/login` next to
`bugfix/logging`, `feature` next to `fix`, any `hotfix`). The DWIM checkout of remote branches
(git-canvas-pjz9w, implemented in parallel under the old rule) also runs into it.

## What
See ADR 0007 for the decision. In domain terms:

- **Branch short code** - every local branch has a 1-2 character lowercase short code
  (`[a-z0-9]`), unique within the repo. New commit IDs are `<short code><allocator number>`
  (`GitGraph.calcCommitId`). The detached-HEAD prefix stays uppercase `H`.
- **Add branch** gets an optional "Short code" input next to the name. Empty -> derived:
  first letter of the last segment; if taken and the name is namespaced, first letters of first
  and last segment; then
  first two letters of the last segment; then first letter plus digit. Branches created
  without the panel (clone, DWIM checkout, restore of an old snapshot) use the same derivation.
- **Branch-name rules like real Git** replace the first-letter check: unique full name, `HEAD`
  forbidden, `check-ref-format` rules, no `x` together with `x/y`. Refusals show a message
  dialog with the concrete reason. Spaces are no longer silently replaced by `_`.
- **Commit-ID label fits the circle** - the ID text inside the commit circle shrinks so that
  IDs up to e.g. `fl123**` stay inside the circle.
- **Snapshot** - `BranchSnapshot` gains the short code; old snapshots without it still load.

## Acceptance criteria
- [ ] Branches `feature`, `fix`, `feature/login`, `bugfix/logging` and `hotfix` can all exist in one repo at the same time.
- [ ] Creating a branch with an explicitly entered short code (e.g. `feature/login` with `fl`) makes its next commit ID `fl<n>`.
- [ ] Creating a branch with an empty short code derives it as described (`feature` -> `f`; then `fix` -> `fi`; `feature/login` -> `l`); the derived code is unique within the repo.
- [ ] A short code that is already used in the repo, longer than 2 chars, or containing characters outside `[a-z0-9]` is refused with a message dialog (entered uppercase letters are lowercased).
- [ ] Commits made on a detached HEAD still get IDs `H<n>`; a branch named `hotfix` gets lowercase `h...` IDs.
- [ ] These names are refused with a message naming the reason: a duplicate full name, `HEAD`, `a b`, `a..b`, `a~b`, `-a`, `a/`, `a.lock`, `a/.b`; and `x/y` while `x` exists (and vice versa).
- [ ] Commit-ID text stays inside the commit circle for IDs up to 7 characters (e.g. `fl123**`), shrinking the font as needed but not below 10 px; short IDs like `m1` keep today's 16 px.
- [ ] Share/restore keeps each branch's short code; a snapshot created before this change still loads and its branches get derived short codes; existing commit IDs are unchanged.
- [ ] Clone copies each branch's short code to the clone's branches.
- [ ] The DWIM checkout from git-canvas-pjz9w (`<name>` without a local branch, `origin/<name>` exists) validates the new local branch with the new rules instead of the first-letter rule (e.g. DWIM `fix` works while `feature` exists; DWIM `feature/login` is refused with a reason while a local `feature` exists) and gives it a derived short code.
- [ ] The git-canvas README (GitGraph description, Commit IDs, new term "Branch short code") and the user manual section on commit IDs (`src/docs/asciidoc/manual.adoc`, first-letter paragraph) describe the new rules; ADR 0004 gets a pointer to ADR 0007.
- [ ] Full verification build passes (`sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`) and the CLAUDE.md smoke test shows `m1` on `main` and no new console errors.

## Notes
- `isBranchNameValid` is in `GitGraph.kt` (~line 945); the add-branch handler and its old
  message are in `App.kt` (~line 723); `calcCommitId` is at `GitGraph.kt` ~line 912.
- `CommitIdLabel` (`components/CommitIdLabel.kt`) renders the ID text with a white box in
  Courier New 16 px; the commit radius is 25 (`GitGraphConfiguration.commitRadius`).
- The snapshot format lives in `scenario/` (ADR 0002); adding an optional field keeps old URLs
  valid - check whether the snapshot version needs a bump.
- Remote-tracking labels (`RemoteBranch`) do not need a short code - no commits are created on
  them.
