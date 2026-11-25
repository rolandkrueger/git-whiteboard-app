---
id: "0004"
title: Shared commit ID allocator across repos (same ID == same commit)
status: accepted
date: 2026-11-23
scope: git-canvas
related_tasks: [git-canvas-j2m8z, git-canvas-wv6va, git-canvas-w3ndg, git-canvas-bdecz, git-canvas-cdy8k]
related_research: []
---

# ADR 0004: Shared commit ID allocator across repos

## Status

Accepted

## Context

Remote operations (clone/push/fetch/pull) move commits between repos on the shared canvas. The audience must be able to recognise a commit in two repos as the same commit. Today a commit ID is `<first letter of branch><branch.counter>` (`calcCommitId`), so every repo mints colliding IDs (`m2` in two repos means two different commits). Separately, `GitGraph.globalCommitNumber` is NOT an ID source: it is the per-graph `linePosition` (vertical layout slot) counter.

## Decision

- Introduce one app-wide `CommitIdAllocator` (single instance owned by `App.kt`, injected into every `GitGraph`). `next()` returns a monotonically increasing integer. A new commit ID is `<first letter of branch><allocator.next()>`; the per-branch `counter` is no longer used for IDs. The very first commit of the first repo remains `m1`; initial commits of later blank repos take `m<next>`.
- Suffix conventions (`*` for rebased copies) are unchanged; rebased copies keep today's `<oldId>*` naming (no allocator number consumed). Because the old ID is reused with `*`, rebasing the same commit in two repos can still yield the same `*` ID; this is accepted for now.
- `globalCommitNumber` (linePosition) stays per graph and is renamed `nextLinePosition` only if the worker finds it cheap; otherwise leave the name.
- Identity rule: two commits with the same ID in different repos ARE the same commit (same parent chain, same colour). Transferring commits copies them with their ID.
- The allocator's next value is part of the scenario snapshot at `MultiRepoSnapshot` level (snapshot version 2).

## Alternatives rejected

- Keep per-repo counters and prefix with repo name (`origin/m2`): noisy labels, defeats "same commit" visual.
- Hash-like IDs: contradicts the simplified ID teaching style.

## Consequences

- Single-repo users see the same IDs as before only until a second repo exists; IDs inside one repo are still increasing, but no longer per-branch contiguous (`f2` may be followed by `f5`). Accepted.
- Snapshot restore must restore the allocator, otherwise new commits could collide with restored ones.

## Amendment

The ID prefix is no longer the branch's first letter but its per-branch short code; see ADR 0007. The allocator itself is unchanged.
