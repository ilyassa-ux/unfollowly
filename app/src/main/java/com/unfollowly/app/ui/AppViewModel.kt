package com.unfollowly.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.unfollowly.app.data.InstagramExportParser
import com.unfollowly.app.data.SnapshotStore
import com.unfollowly.app.model.Insights
import com.unfollowly.app.model.Snapshot
import com.unfollowly.app.model.compare

data class UiState(
    val snapshots: List<Snapshot> = emptyList(),
    val importing: Boolean = false,
    val message: String? = null
) {
    val latest get() = snapshots.firstOrNull()
    val previous get() = snapshots.getOrNull(1)
    val insights: Insights get() = latest?.compare(previous) ?: Insights()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SnapshotStore(application)
    var state = androidx.compose.runtime.mutableStateOf(UiState(store.load()))
        private set

    fun import(uri: Uri) {
        state.value = state.value.copy(importing = true, message = null)
        runCatching {
            val resolver = getApplication<Application>().contentResolver
            val name = resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { if (it.moveToFirst()) it.getString(0) else null } ?: "instagram-export.zip"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Could not read the selected file")
            InstagramExportParser.parse(name, bytes)
        }.onSuccess {
            store.save(it)
            state.value = UiState(store.load(), message = "Import complete — your insights are ready.")
        }.onFailure {
            state.value = state.value.copy(importing = false, message = it.message ?: "Import failed")
        }
    }

    fun clear() { store.clear(); state.value = UiState(message = "All local data was deleted.") }
    fun dismissMessage() { state.value = state.value.copy(message = null) }
}
