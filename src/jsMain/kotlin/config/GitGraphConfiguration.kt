package config

import components.Point
import kotlinx.browser.window

class GitGraphConfiguration(val xOffset: Int = 0, val yOffset: Int = 0) {

    // Per-instance spatial origin for this repo's graph
    val leftOffset: Int get() = Companion.leftOffset + xOffset
    val bottomOffset: Int get() = Companion.bottomOffset + yOffset

    val commitDistance: Int get() = Companion.commitDistance
    val swimlaneDistance: Int get() = Companion.swimlaneDistance
    val commitRadius: Int get() = Companion.commitRadius
    val labelHeight: Int get() = Companion.labelHeight
    val labelOffset: Point get() = Companion.labelOffset
    val labelYOffset: Int get() = Companion.labelYOffset

    companion object {
        val leftOffset = maxOf(550, window.innerWidth / 3)
        val bottomOffset = window.innerHeight / 2
        val commitDistance = 70
        val swimlaneDistance = 80
        val commitRadius = 25
        val labelHeight = 24

        /**
         * Offset of a commit label (branch, tag, or HEAD label) to its commit circle
         */
        val labelOffset = Point(20, -1 * commitRadius + labelHeight / 2)

        val labelYOffset = labelHeight + 5
    }
}
