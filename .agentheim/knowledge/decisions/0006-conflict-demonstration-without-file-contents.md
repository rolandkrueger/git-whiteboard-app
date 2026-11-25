---
id: "0006"
title: Conflict demonstration without file contents
status: accepted
date: 2026-11-25
scope: git-canvas
related_tasks: [git-canvas-mrfrh, git-canvas-c35ww, git-canvas-nymhr]
related_research: []
---

# ADR 0006: Conflict demonstration without file contents

## Status

Accepted

## Context

The vision asks that conflict scenarios can be demonstrated without leaving the tool. The app models
only the commit graph; it has no files, diffs or editor. A realistic conflict simulation (file
contents, conflict markers, a resolution editor) would turn the app into a Git client and conflicts
with the non-goals "no simulation of Git internals" and "not a Git client".

The trainer (builder decision 2026-11-25) does not want to show how a concrete conflict is resolved,
only what happens in the graph when a conflict occurs: when a conflict can happen at all, that a
merge or pull stops and waits for the user, and that a rebase stops in the middle with part of the
commits already replayed.

## Decision

1. **Conflict marker on commits.** A commit can carry a boolean marker "conflicting change"
   (meaning: it changes the shared spot). There is no notion of which file or line; all marked
   commits touch the same imaginary spot. The marker is set when creating a commit or toggled on an
   existing commit by id. It is rendered as a small, clearly visible warning badge on the commit.
2. **Conflict rule.** Combining two lines of history conflicts exactly when, counted from their
   merge base, each side contains at least one marked commit. A fast-forward never conflicts.
3. **Merge and pull (merge).** A conflicting merge does not create the merge commit. The repo enters
   a pending *merging* state: region badge `MERGING - CONFLICT`, the marked commits on both sides
   are highlighted. `Continue` creates the merge commit (unmarked) and ends the state; `Abort`
   leaves the graph exactly as before the merge.
4. **Rebase and pull (rebase).** A rebase replays commits one by one on a detached HEAD. Replaying a
   marked commit while the upstream side has a marked commit since the merge base stops the rebase:
   region badge `REBASING n/m - CONFLICT`, HEAD detached on the last replayed commit, the branch
   label still on the original tip, the commits still to replay visible as the original commits.
   `Continue` replays the stopped commit (the copy keeps the marker) and goes on until the next
   conflict or the end, where the branch moves to the new tip and HEAD re-attaches. `Abort`
   restores the state before the rebase (replayed copies become lost commits).
5. **While a conflict is pending** in a repo, every other graph operation on that repo is refused
   with the message "Resolve the conflict first: Continue or Abort", and `Share` is refused. Other
   repos are unaffected.
6. **Persistence.** The marker is part of the commit snapshot (`conflicting`, default false on
   decode, so older Share URLs still open) and travels with clone, push and fetch. The pending
   conflict state is not persisted.

## Alternatives rejected

- File-content model with real conflict markers and a resolution editor: out of scope (non-goals),
  and better shown live in a terminal or IDE.
- Marking pairs of commits ("conflicts with X"): more precise but slower to set up during a live
  session; one shared imaginary spot is enough to show the mechanics.
- Dropping conflict demonstration and the vision criterion: the graph-level mechanics (stopping,
  half-finished rebase) are hard to draw on a whiteboard and are where the tool adds value.

## Consequences

- Rebase changes from "replay everything at once" to a resumable, step-wise replay on a detached
  HEAD.
- `GitGraph` gains a per-repo pending-operation state; the control panel shows `Continue` / `Abort`
  only while it is active.
- `receiveCommits`, clone and snapshot code must carry the marker.
