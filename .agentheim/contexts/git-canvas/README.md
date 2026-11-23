# git-canvas

## Purpose
Manages Git graph state and visual rendering on a Konva.js canvas. Every Git operation the trainer can perform — from adding a commit to rebasing a branch to pushing between repos — lives here. This is the core of the app.

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
- **Remote-tracking branch** — a label representing the last known state of a branch on a remote repo (e.g. origin/main). Rendered as an unfilled pill with a dashed border in the branch colour and grey text `origin/<name>`; held in its own list, not draggable, not checkout-able, ignored by branch-name uniqueness and pickers (ADR 0005)
- **Push** — transfers the commits the remote lacks (same IDs, receiver-chosen layout via `GitGraph.receiveCommits`) and moves the remote branch to the local tip; allowed only as fast-forward, otherwise rejected with `rejected: non-fast-forward - fetch first` and a brief red flash of the remote region. A branch without upstream gets `origin/<same name>` as upstream on first push. Orchestrated by `RemoteOps.push` (ADR 0005)
- **Fetch** — copies the commits missing locally from all branches of the remote (same `receiveCommits` primitive, placed on the lane of the tracking ref) and creates, moves or removes the `origin/<name>` labels to match; local branches and HEAD never move. Reports `Already up to date` when nothing changed. Orchestrated by `RemoteOps.fetch`
- **Local repo** — one complete graph representing a developer's local clone
- **Remote repo** — one complete graph representing the upstream repository
- **Clone** — creates a new repo region (max 3 total) copying all non-lost commits 1:1 (ID, colour, links, layout) and tags, an `origin/<name>` label per source branch and one checked-out local branch tracking its `origin/<name>`. The remote link is the source's stable repo id (`remoteRepoId`; snapshot `remoteRepoIndex`), name always "origin" (ADR 0005)
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

## Layout decisions

- **Multi-repo canvas layout:** Single shared infinite canvas (ADR 0001). All repos are free-floating regions on one Konva.js stage; the trainer positions them by dragging. The active repo is indicated by a highlighted region border. Pan/zoom is global. Up to 3 repos (one initial, two more via "Add repository").
- **Active repo selection:** Click anywhere in a repo region to make it active; all control-panel commands are routed to the active repo only.
- **Commit IDs:** One app-wide `CommitIdAllocator` (ADR 0004) numbers all new commits as `<branch first letter><n>`; the same ID in two repos means the same commit. Rebased copies keep `<id>*`. `globalCommitNumber` is only the per-graph vertical layout slot, not an ID source.

## Open questions

- (resolved) Remote-tracking labels are visually distinct: dashed, unfilled, grey text (ADR 0005).
