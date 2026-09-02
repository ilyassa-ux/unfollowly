package com.unfollowly.app.data

import com.unfollowly.app.model.Snapshot
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object InstagramExportParser {
    fun parse(name: String, bytes: ByteArray): Snapshot {
        val files = if (name.endsWith(".zip", true)) unzip(bytes) else mapOf(name to bytes)
        val followers = linkedSetOf<String>()
        val following = linkedSetOf<String>()
        files.forEach { (path, content) ->
            if (!path.endsWith(".json", true)) return@forEach
            val normalized = path.lowercase()
            when {
                normalized.contains("followers_") || normalized.endsWith("followers.json") ->
                    followers += usernames(content)
                normalized.contains("following") && !normalized.contains("hashtag") ->
                    following += usernames(content)
            }
        }
        require(followers.isNotEmpty() || following.isNotEmpty()) {
            "No follower data found. Export your Instagram information as JSON, then choose the ZIP file."
        }
        return Snapshot(System.currentTimeMillis(), followers, following)
    }

    private fun unzip(bytes: ByteArray): Map<String, ByteArray> = buildMap {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".json", true)) put(entry.name, zip.readBytes())
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
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
                    data.optJSONObject(i)?.optString("value")?.trim()?.takeIf { it.isNotEmpty() }?.let(out::add)
                }
                val keys = node.keys()
                while (keys.hasNext()) collect(node.opt(keys.next()), out)
            }
            is JSONArray -> for (i in 0 until node.length()) collect(node.opt(i), out)
        }
    }
}
