package com.unfollowly.app.data

import android.content.Context
import com.unfollowly.app.model.Snapshot
import org.json.JSONArray
import org.json.JSONObject

class SnapshotStore(context: Context) {
    private val prefs = context.getSharedPreferences("unfollowly_local", Context.MODE_PRIVATE)

    fun load(): List<Snapshot> = runCatching {
        val array = JSONArray(prefs.getString("snapshots", "[]"))
        (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            Snapshot(
                item.getLong("createdAt"),
                item.getJSONArray("followers").strings(),
                item.getJSONArray("following").strings()
            )
        }.sortedByDescending { it.createdAt }
    }.getOrDefault(emptyList())

    fun save(snapshot: Snapshot) {
        val updated = (listOf(snapshot) + load()).take(30)
        val array = JSONArray()
        updated.forEach { item ->
            array.put(JSONObject().apply {
                put("createdAt", item.createdAt)
                put("followers", JSONArray(item.followers.toList()))
                put("following", JSONArray(item.following.toList()))
            })
        }
        prefs.edit().putString("snapshots", array.toString()).apply()
    }

    fun clear() = prefs.edit().clear().apply()

    private fun JSONArray.strings() = (0 until length()).mapNotNull { optString(it).takeIf(String::isNotBlank) }.toSet()
}
