package graph

class BranchColors {

    companion object {
        private val palette = arrayOf(
            "#A081EF",
            "#E39695",
            "#C1AE7C",
            "#067BC2",
            "#ecc30b",
            "#f37748",
            "#FF9FE5",
            "#861657",
            "#2B50AA",
            "#B3679B",
            "#84BCDA",
            "#F7B538",
            "#53A548",
            "#EEE82C",
            "#5DA271",
            "#2EC4B6",
            "#19381F"
            )

        private var colorIndex = 0

        val HEAD_COLOR = "#FF0F17"

        fun nextColor() = palette[colorIndex++ % palette.size]
    }
}