---
id: git-canvas-nywqs
title: Grey out the remote repo's HEAD label to hint at a bare repository
status: doing
type: feature
context: git-canvas
created: 2026-11-24
completed:
depends_on: [git-canvas-f2ced]
blocks: []
tags: [remote, clone, head, rendering]
related_adrs: [0005]
related_research: []
prior_art: [git-canvas-wv6va, git-canvas-f2ced, git-canvas-a95rx]
---

## Why
A real remote (e.g. on GitHub) is a bare repository: nobody works in it, it has no working tree,
and its HEAD only names the default branch. Showing the remote's HEAD as prominently as a clone's
HEAD blurs that distinction for learners.

## What
Visual only, no change to Git semantics (decided 2026-11-24): while a repo is a remote repo
(derived, as in git-canvas-f2ced: another repo's `remoteRepoId` points at it), its HEAD label is
rendered greyed out and semi-transparent instead of the normal blue `HeadLabel`. The label still
exists and still moves exactly as today, including when a push updates the remote's checked-out
branch (ADR 0005, decision 6). All operations (commit, checkout, branch, merge, rebase, reset)
stay available on the remote so trainers can still simulate "someone else pushed".

## Acceptance criteria
- [ ] A repo that is not a remote repo renders its HEAD label exactly as today (blue fill, white text).
- [ ] As soon as a repo becomes a remote repo (first clone created), its HEAD label switches to a grey, semi-transparent style; clones' HEAD labels keep the normal style.
- [ ] The greyed style is applied through the same refresh path as the other remote cues (`refreshRemoteLinks` / `setRemoteLinks`), so it is also correct after a Share URL restore, after the first clone is rebuilt by the triangle layout, and for the middle repo of a clone-of-a-clone chain.
- [ ] The greyed HEAD label follows the remote's HEAD on every change: a commit made directly in the remote, a checkout in the remote, and a push to the remote's checked-out branch all move the greyed label with no stale normal-style label left behind.
- [ ] The greyed style persists through `setActive(true/false)` and the push-rejection flash; when the remote repo is inactive, its HEAD label is faded consistently with the rest of the inactive region.
- [ ] Commit, checkout, branch, merge, rebase and reset remain available and working on the remote repo (no new restriction or warning).
- [ ] Manual (remote / clone section) and git-canvas README ubiquitous language ("Remote repo", "HEAD") mention that the remote's HEAD is greyed out because a remote is conceptually bare.
- [ ] Full verification build passes (`sh gradlew jsBrowserProductionWebpack jvmJar asciidoctor --warning-mode all`); no new browser console errors.
- [ ] On a projector-sized view, the remote's HEAD reads as secondary compared to the clones' HEADs. [human-eye]

## Notes
- Rejected for now (builder decision 2026-11-24): blocking or warning on working-tree operations
  in the remote, a per-repo "bare" toggle, and hiding or renaming its HEAD. Trainers need to commit
  in the remote to simulate a colleague's push.
- `HeadLabel` lives in `components/CommitLabel.kt`; `Head` in `graph/Branches.kt`. The remote flag
  already reaches `GitGraph.setRemoteLinks(isRemote, originName)` (git-canvas-f2ced); extend that
  path rather than recomputing the remote role elsewhere.
- HEAD label re-creation after restore/clone was fixed in git-canvas-a95rx; make sure the greyed
  style survives a re-created or moved label (apply it whenever the label is (re)built, not only on
  toggle).
