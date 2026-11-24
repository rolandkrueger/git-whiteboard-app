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
- **Pull** — `fetch`, then either merges the upstream tracking ref (`origin/<name>`) into the current branch via `GitGraph.merge` (messages `Fast-forward`, `Merge made`, `Already up to date`) or, with the rebase checkbox set, rebases the current branch onto `origin/<name>` via `GitGraph.rebase` (messages `Rebased`, `Fast-forward`, `Already up to date`). Both `merge` and `rebase` accept tracking refs as source/onto, though these are never listed in dropdowns. Messages are prefixed by the fetch summary if the fetch changed something; refused on detached HEAD, without upstream, or with an unavailable remote. Orchestrated by `RemoteOps.pull`
- **Upstream** — the remote-tracking branch a local branch is linked to (snapshot `BranchSnapshot.upstream`); set by clone or by the first push of a branch. Push, pull and the Remote tab status use it
- **Ahead/behind** — number of commits the local branch has that its upstream lacks / vice versa; shown on the Remote tab
- **Local repo** — one complete graph representing a developer's local clone
- **Remote repo** — one complete graph representing the upstream repository. Derived, not stored: a repo is a remote repo exactly when another repo's `remoteRepoId` points at it. Its region gets a dashed border, a light tinted fill and a `REMOTE` pill in the header; each clone's header shows `origin -> <remote name>` and a line labelled `origin` connects the clone region to its origin region (drawn on a non-listening connector layer below the repo layers, redrawn on region growth, clone, rename and restore; `GitGraph.setRemoteLinks`, `RepoRegion.setRemoteLinks`, `refreshRemoteLinks` in App.kt). Its HEAD label is greyed out and semi-transparent because a remote is conceptually bare (visual only; all operations stay available; applied via `GitGraph.setRemoteLinks` and re-applied whenever the `Head` is re-created)
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
- Cloned
- PushedToRemote
- PushRejected
- Fetched
- PulledFromRemote

## Key commands
- AddCommit, AmendCommit, CherryPick
- AddBranch, DeleteBranch, ResetBranch, CheckoutBranch
- AddTag, DeleteTag, CheckoutTag
- CheckoutCommit
- Merge, Rebase
- Push, Fetch, Pull (optionally with rebase), Clone
- RunGarbageCollection, ToggleLostCommits

## Relationships with other contexts
- **Upstream of:** scenario — supplies the graph state shape that scenario serializes
- **Served by:** infrastructure — build and hosting pipeline

## Layout decisions

- **Multi-repo canvas layout:** Single shared infinite canvas (ADR 0001). All repos are free-floating regions on one Konva.js stage; the trainer positions them by dragging. The active repo is indicated by a highlighted region border. Pan/zoom is global. Up to 3 repos (one initial, two more via "Add repository").
- **Clone placement:** Clones are auto-placed below their origin (ADR 0001 amendment); two clones of one origin form a triangle (first clone shifts left, second sits right); a clone of a clone goes below its own origin. "Add repository" still places to the right. Both x and y offsets are persisted in snapshots.
- **Growth re-layout:** After any operation that grows a region, regions are automatically pushed apart (ADR 0001 amendment, git-canvas-wcbec): of a colliding pair only the lower / right region moves (down to `cloneVerticalGap` below the upper one, or right to `regionHorizontalClearance` beside the left one), never up or left, cascading until nothing conflicts. The moved graph is rebuilt instantly at the new offsets (snapshot + restore); the resolution also runs once after a Share-URL restore.
- **Active repo selection:** Click anywhere in a repo region to make it active; all control-panel commands are routed to the active repo only.
- **Commit IDs:** One app-wide `CommitIdAllocator` (ADR 0004) numbers all new commits as `<branch first letter><n>`; the same ID in two repos means the same commit. Rebased copies keep `<id>*`. `globalCommitNumber` is only the per-graph vertical layout slot, not an ID source.

## Open questions

None.
