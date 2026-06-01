package com.hc.deeplinkmanager.ui.main

import com.hc.deeplinkmanager.data.local.DeeplinkEntity
import com.hc.deeplinkmanager.data.local.DeeplinkWithTag
import com.hc.deeplinkmanager.data.local.TagEntity

sealed interface TagFilter {
    data object All : TagFilter
    data class Tag(val tagId: Long) : TagFilter
}

sealed interface SheetState {
    data object Hidden : SheetState
    data class Add(val tagId: Long) : SheetState
    data class Edit(val deeplink: DeeplinkEntity) : SheetState
}

data class MainUiState(
    val tags: List<TagEntity> = emptyList(),
    val deeplinks: List<DeeplinkWithTag> = emptyList(),
    val selectedFilter: TagFilter = TagFilter.All,
    val sheet: SheetState = SheetState.Hidden,
    val pendingDeleteDeeplink: DeeplinkEntity? = null,
    val pendingDeleteTag: TagEntity? = null,
    val renamingTag: TagEntity? = null,
    val isCreateTagDialogOpen: Boolean = false,
    val transientMessage: String? = null,
    val sheetUrlError: String? = null
) {
    val currentFilterLabel: String
        get() = when (val f = selectedFilter) {
            TagFilter.All -> "All"
            is TagFilter.Tag -> tags.firstOrNull { it.id == f.tagId }?.name ?: "All"
        }
}

