package components

enum class EdgeKind { BRANCH_OFF, MERGE }

/**
 * Pure path computation for a commit edge (no Konva types, so it can be tested without a browser).
 * [start] is the parent's upper dock point, [end] the child's lower dock point (end is above start).
 * Returns a flat x,y list: 4 numbers for a straight line, otherwise Konva `bezier` points
 * (start, c1, c2, p1, c3, c4, p2, ...) with vertical tangents at both ends.
 *
 * Branch-off: bend within [commitDistance] above the parent, then straight up in the child's lane.
 * Merge: straight up in the start (side branch) lane, then bend within [commitDistance] below the child.
 */
fun commitEdgePoints(start: Point, end: Point, kind: EdgeKind, commitDistance: Int): List<Int> {
    if (start.x == end.x) return listOf(start.x, start.y, end.x, end.y)
    val bend = minOf(commitDistance, start.y - end.y).coerceAtLeast(0)
    val half = bend / 2
    return if (kind == EdgeKind.BRANCH_OFF) {
        val bendEndY = start.y - bend
        val bendPart = listOf(start.x, start.y, start.x, start.y - half, end.x, start.y - half, end.x, bendEndY)
        if (bendEndY == end.y) bendPart else bendPart + listOf(end.x, bendEndY, end.x, end.y, end.x, end.y)
    } else {
        val bendStartY = end.y + bend
        val bendPart = listOf(start.x, bendStartY, start.x, end.y + half, end.x, end.y + half, end.x, end.y)
        if (bendStartY == start.y) bendPart
        else listOf(start.x, start.y, start.x, bendStartY, start.x, bendStartY) + bendPart
    }
}
