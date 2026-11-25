---
id: git-canvas-cdy8k
title: Free branch names - replace first-letter uniqueness with Git ref-name rules and per-branch short codes (1-2 chars) for commit IDs that fit the commit circle
status: done
type: feature
context: git-canvas
created: 2026-11-25
completed: 2026-11-25
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
- [x] Branches `feature`, `fix`, `feature/login`, `bugfix/logging` and `hotfix` can all exist in one repo at the same time.
- [x] Creating a branch with an explicitly entered short code (e.g. `feature/login` with `fl`) makes its next commit ID `fl<n>`.
- [x] Creating a branch with an empty short code derives it as described (`feature` -> `f`; then `fix` -> `fi`; `feature/login` -> `l`); the derived code is unique within the repo.
- [x] A short code that is already used in the repo, longer than 2 chars, or containing characters outside `[a-z0-9]` is refused with a message dialog (entered uppercase letters are lowercased).
- [x] Commits made on a detached HEAD still get IDs `H<n>`; a branch named `hotfix` gets lowercase `h...` IDs.
- [x] These names are refused with a message naming the reason: a duplicate full name, `HEAD`, `a b`, `a..b`, `a~b`, `-a`, `a/`, `a.lock`, `a/.b`; and `x/y` while `x` exists (and vice versa).
- [x] Commit-ID text stays inside the commit circle for IDs up to 7 characters (e.g. `fl123**`), shrinking the font as needed but not below 10 px; short IDs like `m1` keep today's 16 px.
- [x] Share/restore keeps each branch's short code; a snapshot created before this change still loads and its branches get derived short codes; existing commit IDs are unchanged.
- [x] Clone copies each branch's short code to the clone's branches.
- [x] The DWIM checkout from git-canvas-pjz9w (`<name>` without a local branch, `origin/<name>` exists) validates the new local branch with the new rules instead of the first-letter rule (e.g. DWIM `fix` works while `feature` exists; DWIM `feature/login` is refused with a reason while a local `feature` exists) and gives it a derived short code.
- [x] The git-canvas README (GitGraph description, Commit IDs, new term "Branch short code") and the user manual section on commit IDs (`src/docs/asciidoc/manual.adoc`, first-letter paragraph) describe the new rules; ADR 0004 gets a pointer to ADR 0007.
- [x] Full verification build passes (`sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`) and the CLAUDE.md smoke test shows `m1` on `main` and no new console errors.

## Notes
- `isBranchNameValid` is in `GitGraph.kt` (~line 945); the add-branch handler and its old
  message are in `App.kt` (~line 723); `calcCommitId` is at `GitGraph.kt` ~line 912.
- `CommitIdLabel` (`components/CommitIdLabel.kt`) renders the ID text with a white box in
  Courier New 16 px; the commit radius is 25 (`GitGraphConfiguration.commitRadius`).
- The snapshot format lives in `scenario/` (ADR 0002); adding an optional field keeps old URLs
  valid - check whether the snapshot version needs a bump.
- Remote-tracking labels (`RemoteBranch`) do not need a short code - no commits are created on
  them.

## Outcome
Implemented ADR 0007. `Branch.shortCode` (Branches.kt) prefixes new commit IDs (`GitGraph.calcCommitId`; detached HEAD stays `H`). `GitGraph.branchNameProblem` (Git ref-name rules with concrete reasons), `shortCodeProblem` and `deriveShortCode` replace `isBranchNameValid`; the DWIM checkout, `createBranchAt` (push/clone) and snapshot restore use them. Add branch has an optional "Short code" input (`App.kt`, `index.html`), refusals show message dialogs, spaces are no longer replaced. `CommitIdLabel` shrinks the font (16 down to 10 px, only for IDs over 3 chars). `BranchSnapshot.shortCode` is an optional field; snapshot version stays 2 (old URLs load with derived codes). Docs: git-canvas README, manual, CLAUDE.md, ADR 0004 amendment pointer. The scenario BC README was not touched (it may want a mention of the new optional `shortCode` field in `BranchSnapshot`).

Note on AC 1: `feature` and `feature/login` cannot coexist (the same AC demands `x/y` be refused while `x` exists). Verified instead as two sets: {feature, fix, bugfix/logging, hotfix} and {feature2, fix, feature/login, bugfix/logging, hotfix}.

### Runtime evidence
Build `sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor` passed. Headless Chrome via CDP against the worktree dev server (port 9191):
- Smoke: title `Git Whiteboard App`; initial repo `my-repo`, `main:m@m1`, HEAD on main; no new console errors (only favicon 404; clipboard-not-focused warnings are headless artefacts).
- Set A: `bugfix/logging:l`, `feature:f`, `fix:fi`, `hotfix:h`, `main:m` -> commit IDs `f3, fi4, fi5, l6, h7`. Set B: `bugfix/logging:l`, `feature/login:fl` (derived, `l` taken), `feature2:fe`, `fix:f`, `hotfix:h` -> IDs `l2, fl3, h4, fe5`.
- Explicit codes: `qa/ticket` with `fl` -> `fl8`; `zeta` with `ZZ` -> lowercased `zz9`; derived `x` -> `x`, then `feat/x2` -> `x` (first), `x` -> `x0` (letter+digit).
- Refusals (dialog text): short code `f` used by `feature`; `abc` (1-2 chars); `a-`/`q!` (a-z0-9 only); duplicate `feature`; `HEAD`; `a b`; `a..b`; `a~b`; `-a`; `a/`; `a.lock`; `a/.b`; `feature/sub` while `feature` exists, `x/y` while `x` exists, `feature` while `feature/login` exists; empty name.
- Detached HEAD: commits `H11`, `H12`; `hotfix` commit `h7`.
- Share/restore round trip: branches (with short codes) and commit IDs identical after reload. Old-format snapshot URL (branches without `shortCode`) loaded: `feature:f`, `feature/b:b`, `fix:fi`, `main:m`, IDs unchanged; new commit on `feature` got `f6`.
- Clone: clone's branch keeps the short code (`fix:fi`, `main:m`).
- DWIM (pjz9w paths): clone with local `feature`: `feature/login` refused ("Branch 'feature' already exists, so 'feature/login' cannot be created"); `hotfix` -> `h`, `fix` -> `fi` (while `feature` holds `f`), `bugfix/logging` -> `l`, each with upstream `origin/<name>`.
- Text fit (canvas measureText of Courier New at the computed font vs box/circle radius 25): `m1` 16 px; `fl123**` 10 px, text 42 px, box 46x16, corner distance 24.4 <= 25. The Konva text itself was not measured, only the same metrics.
