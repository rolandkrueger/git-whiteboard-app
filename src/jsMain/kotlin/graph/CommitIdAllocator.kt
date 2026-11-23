package graph

/**
 * App-wide source of commit ID numbers (ADR 0004). One instance is shared by all repos so that
 * the same ID always denotes the same commit across repos.
 */
class CommitIdAllocator(private var last: Int = 0) {
    /** Returns the next, strictly increasing number. */
    fun next(): Int = ++last

    /** The highest number handed out so far (persisted in snapshots as the next value minus one). */
    fun peekLast(): Int = last

    /** Re-seeds the allocator so that the next number is at least [nextValue]. */
    fun seed(nextValue: Int) {
        last = maxOf(last, nextValue - 1)
    }

    companion object {
        /** Highest number found in any digit run of the given commit IDs (0 if none). */
        fun maxNumericSuffix(ids: Iterable<String>): Int =
            ids.flatMap { Regex("\\d+").findAll(it).map { m -> m.value.toIntOrNull() ?: 0 }.toList() }
                .maxOrNull() ?: 0
    }
}
