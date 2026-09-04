package com.unfollowly.app.data

import com.unfollowly.app.model.Snapshot
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object InstagramExportParser {
    private const val MAX_RELATIONSHIP_FILE_BYTES = 32 * 1024 * 1024

    fun parse(name: String, input: InputStream): Snapshot {
        val followers = linkedSetOf<String>()
        val following = linkedSetOf<String>()

        if (name.endsWith(".zip", true)) {
            parseZip(input, followers, following)
        } else {
            parseRelationshipFile(name, input.readLimited(), followers, following)
        }

        require(followers.isNotEmpty() || following.isNotEmpty()) {
            "No follower data found. Export your Instagram information as JSON, then choose the ZIP file."
        }
        return Snapshot(System.currentTimeMillis(), followers, following)
    }

    fun parse(name: String, bytes: ByteArray): Snapshot =
        parse(name, ByteArrayInputStream(bytes))

    private fun parseZip(
        input: InputStream,
        followers: MutableSet<String>,
        following: MutableSet<String>
    ) {
        ZipInputStream(input.buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && isRelationshipFile(entry.name)) {
                    parseRelationshipFile(entry.name, zip.readLimited(), followers, following)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    private fun isRelationshipFile(path: String): Boolean {
        val normalized = path.lowercase()
        return normalized.endsWith(".json") && (
            normalized.contains("followers_") ||
                normalized.endsWith("/followers.json") ||
                normalized == "followers.json" ||
                (normalized.contains("following") && !normalized.contains("hashtag"))
            )
    }

    private fun parseRelationshipFile(
        path: String,
        content: ByteArray,
        followers: MutableSet<String>,
        following: MutableSet<String>
    ) {
        val normalized = path.lowercase()
        when {
            normalized.contains("followers_") ||
                normalized.endsWith("/followers.json") ||
                normalized == "followers.json" -> followers += usernames(content)
            normalized.contains("following") && !normalized.contains("hashtag") ->
                following += usernames(content)
        }
    }

    private fun InputStream.readLimited(): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        var total = 0
        while (true) {
            val count = read(buffer)
            if (count < 0) break
            total += count
            require(total <= MAX_RELATIONSHIP_FILE_BYTES) {
                "A follower data file is unexpectedly large or damaged."
            }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun usernames(bytes: ByteArray): Set<String> {
        val text = bytes.toString(Charsets.UTF_8).trim()
        val root: Any = if (text.startsWith("[")) JSONArray(text) else JSONObject(text)
        val result = linkedSetOf<String>()
        collect(root, result)
        return result
    }

    private fun collect(node: Any?, out: MutableSet<String>) {
        when (node) {
            is JSONObject -> {
                val data = node.optJSONArray("string_list_data")
                if (data != null) for (i in 0 until data.length()) {
                    data.optJSONObject(i)?.optString("value")?.trim()
                        ?.takeIf { it.isNotEmpty() }?.let(out::add)
                }
                val keys = node.keys()
                while (keys.hasNext()) collect(node.opt(keys.next()), out)
            }
            is JSONArray -> for (i in 0 until node.length()) collect(node.opt(i), out)
        }
    }
}
