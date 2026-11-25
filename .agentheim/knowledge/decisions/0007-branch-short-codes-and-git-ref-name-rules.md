---
id: "0007"
title: Branch short codes for commit IDs and Git ref-name rules instead of first-letter uniqueness
status: accepted
date: 2026-11-25
scope: git-canvas
related_tasks: [git-canvas-cdy8k, git-canvas-pjz9w]
related_research: []
---

# ADR 0007: Branch short codes and Git ref-name rules

## Status

Accepted. Amends ADR 0004 (commit ID format); the shared allocator itself is unchanged.

## Context

Commit IDs are `<first letter of the branch's last name segment><allocator number>`. To keep
the letter meaningful, `GitGraph.isBranchNameValid` forbids two branches with the same first
letter and any name starting with `H` (reserved for commits made on a detached HEAD). The rule
was a crutch for readable IDs, but it blocks realistic branch names: `feature` and `fix`,
`feature/login` and `bugfix/logging`, or `hotfix` cannot coexist. Since ADR 0004 the
allocator number is app-wide and unique, so the letter is no longer needed for ID uniqueness -
it is only a reading aid.

## Decision

- **Short code per branch.** Every local branch carries a short code of 1-2 characters
  (`[a-z0-9]`, lowercase). A new commit ID is `<short code><allocator number>`. Short codes are
  unique within one repo.
- **Auto-suggested, user-editable.** When a branch is created via the control panel, the
  trainer may enter a short code; left empty, it is derived from the name: first letter of the
  last segment (`feature/login` -> `l`), if taken and the name has a namespace, the first letters of
  the first and last segment (`fl`), then the first two letters of the last segment (`lo`), then the first letter
  plus a digit. Branches created without the panel (DWIM checkout, clone, scenario restore of
  old snapshots) use the same derivation.
- **Detached HEAD keeps `H`.** Commits on a detached HEAD keep the uppercase prefix `H`; since
  branch short codes are always lowercase, there is no clash and branch names may start with
  `h`/`H`.
- **Branch names follow Git's ref-name rules.** The full name must be unique; `HEAD` is
  forbidden; Git's `check-ref-format` rules apply (no whitespace or control chars, no `..`,
  none of `~ ^ : ? * [ \`, no `@{`, not starting with `-` or `/`, not ending with `/`, `.` or
  `.lock`, no segment starting with `.`, no `//`); `x` and `x/y` cannot exist at the same time
  (file/directory conflict). A refused name is reported with the concrete reason. The current
  silent replacement of spaces with `_` is dropped in favour of the rule message.
- **IDs must fit the commit circle.** The ID label is shrunk to fit inside the commit circle
  (diameter 50 px) for long IDs such as `fl123*`, down to a minimum readable font size.
- The short code is stored in the branch snapshot; snapshots without it get the derived code.

## Alternatives rejected

- Keep the first letter, drop only the uniqueness check: `f3`/`f7` could belong to `feature`
  or `fix`; only the colour tells them apart, which is weak on a projector.
- Plain numbers (`c12`): simplest, but the ID no longer hints at the branch.

## Consequences

- Existing commit IDs and snapshots stay valid; only newly created branches may get two-letter
  codes.
- The Add branch UI gains an optional short-code field.
- The first-letter explanation in the user manual and the git-canvas README must be rewritten.
