---
id: infrastructure-005
title: Canvas drag-to-pan broken after Konva.js migration
status: done
type: bug
context: infrastructure
created: 2026-05-23
completed: 2026-05-23
commit:
depends_on: [infrastructure-004]
blocks: []
tags: [konva, pan, interaction, regression]
related_adrs: ["0003"]
related_research: [canvas-library-evaluation-2026-05-23]
prior_art: [infrastructure-004, infrastructure-001]
---

## Why

Regression introduced by infrastructure-004 (commit 09e7f74). After the Fabric.js → Konva.js migration, dragging the canvas to pan the viewport no longer works. Zooming (Ctrl+scroll) is still functional.

Trainers pan the canvas constantly during live sessions to navigate between repo regions. This is a blocking regression for practical use.

## What

Restore drag-to-pan: pressing and holding the mouse button on the canvas background, then moving the mouse, should translate the stage viewport. On release the drag stops.

The pan is currently implemented in `App.kt` via `stage.on("mousedown/mousemove/mouseup")` with an `isDragging` flag and `stage.x()/y()` translation. That code path is not working.

## Acceptance criteria

- [x] Pressing and dragging on the canvas background moves the viewport in the direction of the drag
- [x] Releasing the mouse ends the drag (no sticky pan state)
- [x] Drag started inside the canvas and released outside the canvas does not leave a stuck `isDragging = true` state
- [x] Ctrl+scroll zoom still works after this fix
- [x] Click-to-focus repo selection still works after this fix
- [x] Production webpack build passes cleanly

## Notes

- `App.kt` registers `stage.on("mousedown")`, `stage.on("mousemove")`, `stage.on("mouseup")` with a shared `isDragging` Boolean closure variable
- The `mouseup` handler is registered after `initGraph()` calls — verify event registration order is not relevant to Konva's internal listener ordering
- The `mouseup` listener is on the stage only (not `window`) — releasing the mouse outside the canvas will not clear `isDragging`; consider registering `mouseup` on the container or on `window` via a native DOM listener
- Konva's built-in drag support (`stage.draggable(true)`) is an alternative to the manual event approach — evaluate whether switching to it is simpler and equally compatible with the click-to-focus repo selection logic in `mouseup`
- If using `stage.draggable(true)`, the `dragstart`/`dragend` events can replace the current `mousedown`/`mouseup` logic; the click-to-focus detection may need to move to a separate `click` event on the stage
- ADR-0001 describes the multi-repo canvas layout and notes that pan/zoom is global (i.e., the stage, not per-layer)

## Outcome

Replaced Konva synthetic event handlers (`stage.on("mousedown/mousemove/mouseup")`) with native DOM event listeners on `stage.getContainer()`. Konva synthetic events do not fire reliably when shapes with `listening: true` intercept pointer events before they can bubble to the stage.

Changes in `App.kt`:
- `mousedown` / `mousemove` — now native DOM listeners on the container
- `mouseleave` — new listener on the container clears `isDragging` to prevent sticky pan when pointer exits canvas
- `mouseup` — moved to `window.addEventListener` so release outside the canvas boundary also terminates drag; click-to-focus coordinate conversion switched from `stage.getPointerPosition()` to `container.getBoundingClientRect()` + manual stage transform
- Wheel zoom unchanged (was already a native listener on the container)

Production webpack build passes. Key file: `src/jsMain/kotlin/App.kt`.
