---
id: git-canvas-pjz9w
title: Check out remote-tracking branches like real Git - origin/<name> gives a detached HEAD, <name> without a local branch creates a local tracking branch (DWIM)
status: doing
type: feature
context: git-canvas
created: 2026-11-25
completed:
depends_on: []
blocks: []
tags: [checkout, remote, branches, detached-head]
related_adrs: [0005]
related_research: []
prior_art: [git-canvas-b5f0r, git-canvas-wv6va, git-canvas-rx1y3]
---

## Why
Local branches can be checked out via the control panel or by double-clicking their label.
Remote-tracking branches (`origin/<name>`) cannot be checked out at all (ADR 0005 made them
"not checkout-able"). That hides two classic Git training moments:

1. `git checkout origin/feature` puts you in **detached HEAD** - a common beginner surprise.
2. `git checkout feature` (no local `feature`, but `origin/feature` exists) silently
   **creates a local branch `feature` tracking `origin/feature`** and checks it out - the
   normal way to start working on a colleague's branch after a clone or fetch.

The app should behave exactly like real Git in both cases.

## What
Two checkout semantics, both routed through `GitGraph.checkout` so every entry point behaves
the same:

- **Remote-tracking ref** - checking out `origin/<name>` detaches HEAD onto the commit the
  tracking label points to. No branch is created, the tracking label does not move.
- **DWIM (do what I mean)** - checking out `<name>` when no local branch `<name>` exists but
  exactly the tracking ref `origin/<name>` does: create local branch `<name>` at that commit,
  set its upstream to `origin/<name>`, attach HEAD to it. If a local `<name>` exists, it wins
  (plain local checkout, as today).

Entry points:

- **Double-click on an `origin/<name>` label** -> remote-tracking checkout (detached HEAD),
  i.e. exactly `git checkout origin/<name>`. Refused while a conflict is pending, like the
  other label double-clicks.
- **"Check out branch" dropdown** (Refs tab) lists, in this order: local branches; remote-only
  branch names as plain `<name>` (no local branch of that name exists) -> DWIM; all tracking
  refs as `origin/<name>` -> detached HEAD. This mirrors what `git checkout <TAB>` offers.
  The delete / merge / rebase dropdowns stay unchanged (local branches only).
- **Free-text "Check out" input** (Commit tab) accepts the same names as `git checkout`:
  commit id, tag, local branch, `origin/<name>` (detached) and remote-only `<name>` (DWIM).

Demonstrating the regular case (DWIM) in a training: clone a repo whose remote has a branch
`feature`, then pick `feature` (not `origin/feature`) in the "Check out branch" dropdown, or
type `feature` into the "Check out" input. The new local `feature` label appears on the same
commit as `origin/feature`, HEAD is attached to it and the Remote tab shows
`feature -> origin/feature`.

## Acceptance criteria
- [ ] Double-clicking an `origin/<name>` label leaves HEAD detached on that label's commit; no local branch is created and the tracking label stays where it was.
- [ ] Double-clicking an `origin/<name>` label while a conflict is pending is refused with the existing `Resolve the conflict first: Continue or Abort` behaviour.
- [ ] The "Check out branch" dropdown lists local branches, then remote-only names as `<name>`, then all tracking refs as `origin/<name>`; the delete, merge and rebase dropdowns still list local branches only.
- [ ] Checking out `origin/<name>` via dropdown or free-text input gives the same detached-HEAD result as the double-click.
- [ ] Checking out `<name>` (dropdown or free-text) with no local `<name>` but an existing `origin/<name>` creates local branch `<name>` on the tracking ref's commit, with upstream `origin/<name>`, and attaches HEAD to it; the Remote tab then shows `<name> -> origin/<name>` with ahead 0, behind 0.
- [ ] Checking out `<name>` when a local `<name>` already exists checks out the local branch unchanged (no new branch, no upstream change), even if `origin/<name>` points elsewhere.
- [ ] If the DWIM branch would violate branch-name uniqueness (`isBranchNameValid`, first-letter rule), the checkout is refused with a message dialog naming the conflict, and the graph is unchanged.
- [ ] A local branch created by DWIM survives Share/restore with its upstream (it is an ordinary `Branch` in the snapshot); push and pull work on it without a further setup step.
- [ ] Adding a commit after a DWIM checkout moves only the new local branch; `origin/<name>` stays put and the Remote tab shows ahead 1.
- [ ] The git-canvas README (ubiquitous language: Remote-tracking branch, CheckoutBranch, new DWIM term) and the user manual (`src/docs/asciidoc/manual.adoc`) describe both checkout behaviours, and ADR 0005 gets an amendment noting that tracking refs are now checkout-able (detached) and appear in the checkout dropdown only.
- [ ] Full verification build passes (`sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`) and the CLAUDE.md smoke test shows no new console errors.

## Notes
- Remote-tracking labels are `RemoteBranch`es in their own list (`GitGraph.remoteBranches`),
  invisible to `findBranch`; `checkout` (`GitGraph.kt` ~line 557) currently falls through to
  tags/commits. Extend the lookup order to: local branch -> tag -> commit -> `origin/<name>`
  (detached) -> DWIM `<name>`. Check Git's actual precedence if a tag and a branch share a
  name - not expected in practice since the app keeps names distinct.
- The `RemoteBranchLabel` currently has no double-click handler; wire it like
  `labelCheckoutHandler` (`GitGraph.kt` ~line 95), also in the restore path (~line 1170).
- `updateBranchSelects` (`App.kt` ~line 815) feeds all four dropdowns from the same list;
  the checkout dropdown needs its own option list.
- Upstream setup can reuse `setBranchUpstream`; the DWIM branch gets a normal branch colour.
- The remote repo (bare, no tracking refs) is unaffected.
