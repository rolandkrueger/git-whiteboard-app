package scenario

import kotlinx.browser.window

/**
 * Serializes a MultiRepoSnapshot to a JSON string using JSON.stringify via dynamic
 * objects, and deserializes back. This avoids adding kotlinx-serialization to the
 * build while remaining type-safe through the snapshot data classes.
 */
object ScenarioSerializer {

    fun encode(snapshot: MultiRepoSnapshot): String {
        val obj = snapshotToDynamic(snapshot)
        return JSON.stringify(obj)
    }

    fun decode(json: String): MultiRepoSnapshot? {
        return try {
            val obj = JSON.parse<dynamic>(json)
            dynamicToSnapshot(obj)
        } catch (e: Throwable) {
            console.warn("ScenarioSerializer.decode failed: ${e.message}")
            null
        }
    }

    // ---- dynamic → data class conversions ----

    private fun dynamicToSnapshot(obj: dynamic): MultiRepoSnapshot? {
        if (obj == null) return null
        val version = jsIntOrNull(obj.version) ?: return null
        if (version != 1) return null

        val reposArr = obj.repos ?: return null
        val repos = mutableListOf<RepoSnapshot>()
        val length = reposArr.length as Int
        for (i in 0 until length) {
            repos.add(dynamicToRepoSnapshot(reposArr[i]) ?: return null)
        }
        return MultiRepoSnapshot(version = version, repos = repos)
    }

    private fun dynamicToRepoSnapshot(obj: dynamic): RepoSnapshot? {
        if (obj == null) return null
        val repoName = obj.repoName as? String ?: return null
        val xOffset = (obj.xOffset as? Int) ?: jsIntOrNull(obj.xOffset) ?: return null
        val yOffset = (obj.yOffset as? Int) ?: jsIntOrNull(obj.yOffset) ?: return null
        val globalCommitNumber = (obj.globalCommitNumber as? Int) ?: jsIntOrNull(obj.globalCommitNumber) ?: return null
        val globalSwimlaneCounter = (obj.globalSwimlaneCounter as? Int) ?: jsIntOrNull(obj.globalSwimlaneCounter) ?: return null

        val commitsArr = obj.commits ?: return null
        val commits = mutableListOf<CommitSnapshot>()
        val commitsLen = commitsArr.length as Int
        for (i in 0 until commitsLen) {
            commits.add(dynamicToCommitSnapshot(commitsArr[i]) ?: return null)
        }

        val branchesArr = obj.branches ?: return null
        val branches = mutableListOf<BranchSnapshot>()
        val branchesLen = branchesArr.length as Int
        for (i in 0 until branchesLen) {
            branches.add(dynamicToBranchSnapshot(branchesArr[i]) ?: return null)
        }

        val tagsArr = obj.tags ?: return null
        val tags = mutableListOf<TagSnapshot>()
        val tagsLen = tagsArr.length as Int
        for (i in 0 until tagsLen) {
            tags.add(dynamicToTagSnapshot(tagsArr[i]) ?: return null)
        }

        val head = dynamicToHeadSnapshot(obj.head) ?: return null

        return RepoSnapshot(
            repoName = repoName,
            xOffset = xOffset,
            yOffset = yOffset,
            globalCommitNumber = globalCommitNumber,
            globalSwimlaneCounter = globalSwimlaneCounter,
            commits = commits,
            branches = branches,
            tags = tags,
            head = head
        )
    }

    private fun dynamicToCommitSnapshot(obj: dynamic): CommitSnapshot? {
        if (obj == null) return null
        val id = obj.id as? String ?: return null
        val linePosition = (obj.linePosition as? Int) ?: jsIntOrNull(obj.linePosition) ?: return null
        val swimlane = (obj.swimlane as? Int) ?: jsIntOrNull(obj.swimlane) ?: return null
        val parentId = obj.parentId as? String
        val mergedParentId = obj.mergedParentId as? String
        val commitColor = obj.commitColor as? String ?: ""
        val isLostInReflog = obj.isLostInReflog as? Boolean ?: false
        return CommitSnapshot(id, linePosition, swimlane, parentId, mergedParentId, commitColor, isLostInReflog)
    }

    private fun dynamicToBranchSnapshot(obj: dynamic): BranchSnapshot? {
        if (obj == null) return null
        val id = obj.id as? String ?: return null
        val swimlane = (obj.swimlane as? Int) ?: jsIntOrNull(obj.swimlane) ?: return null
        val targetCommitId = obj.targetCommitId as? String ?: return null
        val commitColor = obj.commitColor as? String ?: ""
        return BranchSnapshot(id, swimlane, targetCommitId, commitColor)
    }

    private fun dynamicToTagSnapshot(obj: dynamic): TagSnapshot? {
        if (obj == null) return null
        val id = obj.id as? String ?: return null
        val targetCommitId = obj.targetCommitId as? String ?: return null
        return TagSnapshot(id, targetCommitId)
    }

    private fun dynamicToHeadSnapshot(obj: dynamic): HeadSnapshot? {
        if (obj == null) return null
        val targetBranchId = obj.targetBranchId as? String
        val detachedCommitId = obj.detachedCommitId as? String
        return HeadSnapshot(targetBranchId, detachedCommitId)
    }

    // ---- data class → dynamic conversions ----

    private fun snapshotToDynamic(snapshot: MultiRepoSnapshot): dynamic {
        val obj: dynamic = js("({})")
        obj.version = snapshot.version
        obj.repos = snapshot.repos.map { repoSnapshotToDynamic(it) }.toJsArray()
        return obj
    }

    private fun repoSnapshotToDynamic(repo: RepoSnapshot): dynamic {
        val obj: dynamic = js("({})")
        obj.repoName = repo.repoName
        obj.xOffset = repo.xOffset
        obj.yOffset = repo.yOffset
        obj.globalCommitNumber = repo.globalCommitNumber
        obj.globalSwimlaneCounter = repo.globalSwimlaneCounter
        obj.commits = repo.commits.map { commitSnapshotToDynamic(it) }.toJsArray()
        obj.branches = repo.branches.map { branchSnapshotToDynamic(it) }.toJsArray()
        obj.tags = repo.tags.map { tagSnapshotToDynamic(it) }.toJsArray()
        obj.head = headSnapshotToDynamic(repo.head)
        return obj
    }

    private fun commitSnapshotToDynamic(commit: CommitSnapshot): dynamic {
        val obj: dynamic = js("({})")
        obj.id = commit.id
        obj.linePosition = commit.linePosition
        obj.swimlane = commit.swimlane
        obj.parentId = commit.parentId
        obj.mergedParentId = commit.mergedParentId
        obj.commitColor = commit.commitColor
        obj.isLostInReflog = commit.isLostInReflog
        return obj
    }

    private fun branchSnapshotToDynamic(branch: BranchSnapshot): dynamic {
        val obj: dynamic = js("({})")
        obj.id = branch.id
        obj.swimlane = branch.swimlane
        obj.targetCommitId = branch.targetCommitId
        obj.commitColor = branch.commitColor
        return obj
    }

    private fun tagSnapshotToDynamic(tag: TagSnapshot): dynamic {
        val obj: dynamic = js("({})")
        obj.id = tag.id
        obj.targetCommitId = tag.targetCommitId
        return obj
    }

    private fun headSnapshotToDynamic(head: HeadSnapshot): dynamic {
        val obj: dynamic = js("({})")
        obj.targetBranchId = head.targetBranchId
        obj.detachedCommitId = head.detachedCommitId
        return obj
    }

    // Kotlin/JS: JSON.parse gives dynamic where numeric fields may be Double not Int
    private fun jsIntOrNull(value: dynamic): Int? {
        if (value == null) return null
        return try {
            (value as Double).toInt()
        } catch (e: Throwable) {
            null
        }
    }

    private fun <T> List<T>.toJsArray(): dynamic {
        val arr: dynamic = js("([])")
        this.forEachIndexed { i, item -> arr[i] = item }
        return arr
    }
}

// ---- Compression utilities (browser API, returns Promise<String>) ----

/**
 * Compresses a string with gzip and returns a base64url-encoded Promise<String>.
 */
fun compressToBase64Url(json: String): dynamic = js("""
    (function(data) {
        return new Promise(function(resolve, reject) {
            try {
                var bytes = new TextEncoder().encode(data);
                var cs = new CompressionStream('gzip');
                var writer = cs.writable.getWriter();
                var reader = cs.readable.getReader();
                var chunks = [];
                writer.write(bytes);
                writer.close();
                function pump() {
                    return reader.read().then(function(result) {
                        if (result.done) {
                            var total = chunks.reduce(function(s, c) { return s + c.length; }, 0);
                            var arr = new Uint8Array(total);
                            var off = 0;
                            chunks.forEach(function(c) { arr.set(c, off); off += c.length; });
                            var binary = '';
                            arr.forEach(function(b) { binary += String.fromCharCode(b); });
                            return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
                        }
                        chunks.push(result.value);
                        return pump();
                    });
                }
                resolve(pump());
            } catch(e) { reject(e); }
        });
    })(json)
""")

/**
 * Decompresses a base64url+gzip string and returns the original JSON as a Promise<String>.
 */
fun decompressFromBase64Url(encoded: String): dynamic = js("""
    (function(b64) {
        return new Promise(function(resolve, reject) {
            try {
                var padded = b64.replace(/-/g, '+').replace(/_/g, '/');
                var pad = (4 - padded.length % 4) % 4;
                var b64p = padded + '==='.slice(0, pad);
                var binary = atob(b64p);
                var bytes = new Uint8Array(binary.length);
                for (var i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
                var ds = new DecompressionStream('gzip');
                var writer = ds.writable.getWriter();
                var reader = ds.readable.getReader();
                var chunks = [];
                writer.write(bytes);
                writer.close();
                function pump() {
                    return reader.read().then(function(result) {
                        if (result.done) {
                            var total = chunks.reduce(function(s, c) { return s + c.length; }, 0);
                            var arr = new Uint8Array(total);
                            var off = 0;
                            chunks.forEach(function(c) { arr.set(c, off); off += c.length; });
                            resolve(new TextDecoder().decode(arr));
                        } else {
                            chunks.push(result.value);
                            return pump();
                        }
                    });
                }
                pump();
            } catch(e) { reject(e); }
        });
    })(encoded)
""")
