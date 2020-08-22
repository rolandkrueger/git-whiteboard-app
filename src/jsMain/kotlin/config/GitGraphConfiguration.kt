package config

import fabricjs.Point
import kotlin.browser.window

class GitGraphConfiguration {
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