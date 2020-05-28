package config

import fabricjs.Point

class GitGraphConfiguration {
    companion object {
        val leftOffset = 150
        val commitDistance = 70
        val bottomOffset = 440
        val swimlaneDistance = 80
        val commitRadius = 25
        val labelHeight = 24


        /**
         * Offset of a commit label (branch, tag, or HEAD label) to its commit circle
         */
        val labelOffset = Point(20, -1 * commitRadius + labelHeight / 2)

        val labelYOffxet = labelHeight + 5
    }
}