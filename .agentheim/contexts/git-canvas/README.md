# git-canvas

## Purpose
Manages Git graph state and visual rendering on a Fabric.js canvas. Every Git operation the trainer can perform — from adding a commit to rebasing a branch to pushing between repos — lives here. This is the core of the app.

## Classification
core — this is the unique value the tool provides; everything else is support infrastructure.

## Actors
- **Git Trainer** — operates the control panel to build and manipulate graphs live during a session

## Ubiquitous language
- **Commit** — a node in the graph with a simplified ID (e.g. m1, f2) and a color tied to its originating branch
- **Swimlane** — the horizontal axis position; separates parallel branch histories visually
- **Line position** — the vertical axis position; represents commit order in time
- **HEAD** — the pointer showing the current checkout state; can be attached to a branch (normal) or floating on a commit (detached)
- **Branch label** — a named, movable pointer attached to a commit node
- **Tag label** — an immutable named pointer attached to a commit node
- **Detached HEAD** — state where HEAD points directly at a commit, not a branch
- **Lost commit** — a commit no longer reachable from any branch or tag (visible in reflog, removable by GC)
- **Merge commit** — a commit with two parent lines
- **Remote-tracking branch** — a label representing the last known state of a branch on a remote repo (e.g. origin/main)
- **Local repo** — one complete graph representing a developer's local clone
- **Remote repo** — one complete graph representing the upstream repository
- **Multi-repo canvas** — a canvas view showing two or more repos simultaneously

## Aggregates
- **GitGraph** — owns one repo's full commit history, branch/tag set, HEAD state, and swimlane layout; enforces branch name uniqueness by first letter

## Key events
- CommitAdded
- BranchCreated / BranchDeleted / BranchReset
- TagCreated / TagDeleted
- CheckedOut
- Merged
- Rebased
- CommitAmended
- LostCommitsToggled
- GarbageCollected
- PushedToRemote
- PulledFromRemote

## Key commands
- AddCommit, AmendCommit, CherryPick
- AddBranch, DeleteBranch, ResetBranch, CheckoutBranch
- AddTag, DeleteTag, CheckoutTag
- CheckoutCommit
- Merge, Rebase
- Push, Pull, Clone
- RunGarbageCollection, ToggleLostCommits

## Relationships with other contexts
- **Upstream of:** scenario — supplies the graph state shape that scenario serializes
- **Served by:** infrastructure — build and hosting pipeline

## Open questions
- Multi-repo canvas layout: side-by-side panels vs. floating regions vs. single shared canvas?
- How many simultaneous repos should be supported on one canvas?
- Should remote-tracking branch labels be visually distinct from local branch labels?
