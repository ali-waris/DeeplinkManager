package com.hc.deeplinkmanager.ui.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hc.deeplinkmanager.data.local.DeeplinkEntity
import com.hc.deeplinkmanager.data.local.TagEntity
import com.hc.deeplinkmanager.data.repo.DeeplinkRepository
import com.hc.deeplinkmanager.data.repo.TagRepository
import com.hc.deeplinkmanager.di.IoDispatcher
import com.hc.deeplinkmanager.util.CsvExporter
import com.hc.deeplinkmanager.util.CsvImporter
import com.hc.deeplinkmanager.util.ExportResult
import com.hc.deeplinkmanager.util.ExportStorage
import com.hc.deeplinkmanager.util.ImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val deeplinkRepo: DeeplinkRepository,
    private val tagRepo: TagRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) : ViewModel() {

    private val ceh = CoroutineExceptionHandler { _, throwable ->
        _internal.update { it.copy(transientMessage = throwable.message ?: "Something went wrong") }
    }

    private val _filter = MutableStateFlow<TagFilter>(TagFilter.All)
    private val _internal = MutableStateFlow(MainUiState())

    private val deeplinksFlow = _filter.flatMapLatest { f ->
        when (f) {
            TagFilter.All -> deeplinkRepo.observeAll()
            is TagFilter.Tag -> deeplinkRepo.observeByTag(f.tagId)
        }
    }

    val uiState = combine(
        _internal,
        _filter,
        tagRepo.observeAll(),
        deeplinksFlow
    ) { internal, filter, tags, deeplinks ->
        internal.copy(
            tags = tags,
            deeplinks = deeplinks,
            selectedFilter = filter
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    fun selectFilter(filter: TagFilter) {
        _filter.value = filter
    }

    fun openAddSheet() {
        val tagId = (_filter.value as? TagFilter.Tag)?.tagId ?: TagEntity.UNGROUPED_ID
        _internal.update { it.copy(sheet = SheetState.Add(tagId), sheetUrlError = null) }
    }

    fun openEditSheet(deeplink: DeeplinkEntity) {
        _internal.update { it.copy(sheet = SheetState.Edit(deeplink), sheetUrlError = null) }
    }

    fun dismissSheet() {
        _internal.update { it.copy(sheet = SheetState.Hidden, sheetUrlError = null) }
    }

    fun clearSheetUrlError() {
        if (_internal.value.sheetUrlError != null) {
            _internal.update { it.copy(sheetUrlError = null) }
        }
    }

    fun save(name: String, url: String, tagId: Long) {
        val sheet = _internal.value.sheet
        val trimmedName = name.trim()
        val trimmedUrl = url.trim()
        if (trimmedName.isEmpty() || trimmedUrl.isEmpty()) return
        viewModelScope.launch(io + ceh) {
            // Duplicate-URL guard: another deeplink must not already use the same URL.
            val existingId = deeplinkRepo.findIdByUrl(trimmedUrl)
            val editingId = (sheet as? SheetState.Edit)?.deeplink?.id ?: 0L
            if (existingId != null && existingId != editingId) {
                _internal.update { it.copy(sheetUrlError = "A deeplink with this URL already exists") }
                return@launch
            }
            val entity = when (sheet) {
                is SheetState.Edit -> sheet.deeplink.copy(
                    name = trimmedName,
                    url = trimmedUrl,
                    tagId = tagId
                )
                else -> DeeplinkEntity(name = trimmedName, url = trimmedUrl, tagId = tagId)
            }
            deeplinkRepo.upsert(entity)
            _internal.update { it.copy(sheet = SheetState.Hidden, sheetUrlError = null) }
        }
    }

    // Deeplink delete
    fun requestDeleteDeeplink(d: DeeplinkEntity) {
        _internal.update { it.copy(pendingDeleteDeeplink = d) }
    }

    fun cancelDeleteDeeplink() {
        _internal.update { it.copy(pendingDeleteDeeplink = null) }
    }

    fun confirmDeleteDeeplink() {
        val d = _internal.value.pendingDeleteDeeplink ?: return
        viewModelScope.launch(io + ceh) {
            deeplinkRepo.delete(d)
            _internal.update { it.copy(pendingDeleteDeeplink = null) }
        }
    }

    // Tag CRUD
    fun openCreateTagDialog() {
        _internal.update { it.copy(isCreateTagDialogOpen = true) }
    }

    fun dismissCreateTagDialog() {
        _internal.update { it.copy(isCreateTagDialogOpen = false) }
    }

    /** Creates the tag and invokes [onCreated] with the new id (used by the sheet to auto-select). */
    fun createTag(name: String, onCreated: ((Long) -> Unit)? = null) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.equals(TagEntity.UNGROUPED_NAME, ignoreCase = true)) {
            _internal.update { it.copy(transientMessage = "Invalid tag name") }
            return
        }
        viewModelScope.launch(io + ceh) {
            val id = tagRepo.create(trimmed)
            _internal.update { it.copy(isCreateTagDialogOpen = false) }
            onCreated?.invoke(id)
        }
    }

    fun openRenameTag(tag: TagEntity) {
        if (tag.isSystem) return
        _internal.update { it.copy(renamingTag = tag) }
    }

    fun dismissRenameTag() {
        _internal.update { it.copy(renamingTag = null) }
    }

    fun renameTag(newName: String) {
        val tag = _internal.value.renamingTag ?: return
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(io + ceh) {
            tagRepo.rename(tag, trimmed)
            _internal.update { it.copy(renamingTag = null) }
        }
    }

    fun requestDeleteTag(tag: TagEntity) {
        if (tag.isSystem) return
        _internal.update { it.copy(pendingDeleteTag = tag) }
    }

    fun cancelDeleteTag() {
        _internal.update { it.copy(pendingDeleteTag = null) }
    }

    fun confirmDeleteTag() {
        val tag = _internal.value.pendingDeleteTag ?: return
        viewModelScope.launch(io + ceh) {
            val moved = tagRepo.deleteAndReassign(tag)
            // If the deleted tag was the active filter, switch to All.
            if ((_filter.value as? TagFilter.Tag)?.tagId == tag.id) {
                _filter.value = TagFilter.All
            }
            _internal.update {
                it.copy(
                    pendingDeleteTag = null,
                    transientMessage = if (moved > 0)
                        "Moved $moved deeplink${if (moved == 1) "" else "s"} to Ungrouped"
                    else null
                )
            }
        }
    }

    fun consumeMessage() {
        _internal.update { it.copy(transientMessage = null) }
    }

    /**
     * Exports the currently filtered deeplinks to a CSV in Downloads.
     * [hasLegacyWritePermission] is invoked on API <= 28 to check WRITE_EXTERNAL_STORAGE.
     */
    suspend fun exportCurrentToCsv(
        context: Context,
        hasLegacyWritePermission: () -> Boolean
    ): ExportResult {
        val snapshot = uiState.value
        val items = snapshot.deeplinks
        if (items.isEmpty()) {
            _internal.update { it.copy(transientMessage = "Nothing to export") }
            return ExportResult.Error("Nothing to export")
        }
        return withContext(io) {
            val csv = CsvExporter.buildCsv(items)
            val name = CsvExporter.fileName(snapshot.currentFilterLabel)
            val result = ExportStorage.saveCsvToDownloads(context, name, csv, hasLegacyWritePermission)
            val message = when (result) {
                is ExportResult.Success -> "Exported ${items.size} to ${result.displayPath}"
                is ExportResult.Error -> "Export failed: ${result.message}"
                ExportResult.PermissionRequired -> null
            }
            if (message != null) _internal.update { it.copy(transientMessage = message) }
            result
        }
    }

    /** Reads CSV from [uri] and inserts deeplinks, creating tags on demand. */
    suspend fun importCsvFromUri(context: Context, uri: Uri): ImportResult = withContext(io) {
        try {
            val csv = context.contentResolver.openInputStream(uri)
                ?.use { it.bufferedReader().readText() }
                ?: run {
                    _internal.update { s -> s.copy(transientMessage = "Could not read file") }
                    return@withContext ImportResult.Error("Could not read file")
                }
            val parsed = CsvImporter.parse(csv)
            if (parsed.isEmpty()) {
                _internal.update { it.copy(transientMessage = "No rows to import") }
                return@withContext ImportResult.Error("No rows to import")
            }
            var imported = 0
            var skipped = 0
            var duplicates = 0
            val tagCache = HashMap<String, Long>()
            // Load existing (name|url) keys once so duplicates are detected without per-row DB hits.
            val existingKeys = deeplinkRepo.existingNameUrlKeys().toHashSet()
            for (row in parsed) {
                val key = "${row.name}|${row.url}"
                if (key in existingKeys) {
                    duplicates++
                    continue
                }
                try {
                    val tagKey = row.tagName.lowercase()
                    val tagId = tagCache.getOrPut(tagKey) { tagRepo.getOrCreateByName(row.tagName) }
                    deeplinkRepo.upsert(
                        DeeplinkEntity(name = row.name, url = row.url, tagId = tagId)
                    )
                    existingKeys.add(key) // guard against duplicates within the same CSV
                    imported++
                } catch (e: Exception) {
                    skipped++
                }
            }
            val msg = buildString {
                append("Imported ").append(imported)
                if (duplicates > 0) append(", ").append(duplicates).append(" duplicate")
                    .append(if (duplicates == 1) "" else "s").append(" skipped")
                if (skipped > 0) append(", ").append(skipped).append(" failed")
            }
            _internal.update { it.copy(transientMessage = msg) }
            ImportResult.Success(imported, skipped + duplicates)
        } catch (e: Exception) {
            val msg = "Import failed: ${e.message ?: "unknown error"}"
            _internal.update { it.copy(transientMessage = msg) }
            ImportResult.Error(msg)
        }
    }
}
