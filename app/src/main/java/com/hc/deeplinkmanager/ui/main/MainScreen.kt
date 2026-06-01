package com.hc.deeplinkmanager.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hc.deeplinkmanager.data.local.DeeplinkEntity
import com.hc.deeplinkmanager.data.local.DeeplinkWithTag
import com.hc.deeplinkmanager.data.local.TagEntity
import com.hc.deeplinkmanager.util.DeeplinkLauncher
import com.hc.deeplinkmanager.util.ExportResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current

    // Surface transient messages.
    LaunchedEffect(state.transientMessage) {
        state.transientMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    tags = state.tags,
                    selected = state.selectedFilter,
                    onSelectAll = {
                        viewModel.selectFilter(TagFilter.All)
                        scope.launch { drawerState.close() }
                    },
                    onSelectTag = {
                        viewModel.selectFilter(TagFilter.Tag(it.id))
                        scope.launch { drawerState.close() }
                    },
                    onCreateTag = { viewModel.openCreateTagDialog() },
                    onRenameTag = { viewModel.openRenameTag(it) },
                    onDeleteTag = { viewModel.requestDeleteTag(it) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) {
                        scope.launch {
                            val r = viewModel.exportCurrentToCsv(context) { true }
                            if (r is ExportResult.Error) snackbarHostState.showSnackbar(r.message)
                        }
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Storage permission required to export") }
                    }
                }
                fun hasLegacyWrite(): Boolean = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                TopAppBar(
                    title = { Text(state.currentFilterLabel) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                        }
                    },
                    actions = {
                        val importLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument()
                        ) { uri ->
                            if (uri != null) {
                                scope.launch { viewModel.importCsvFromUri(context, uri) }
                            }
                        }
                        IconButton(onClick = {
                            importLauncher.launch(
                                arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*")
                            )
                        }) {
                            Icon(Icons.Filled.Upload, contentDescription = "Import CSV")
                        }
                        IconButton(
                            enabled = state.deeplinks.isNotEmpty(),
                            onClick = {
                                scope.launch {
                                    val r = viewModel.exportCurrentToCsv(context, ::hasLegacyWrite)
                                    if (r is ExportResult.PermissionRequired &&
                                        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                                    ) {
                                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.SaveAlt, contentDescription = "Export CSV")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { viewModel.openAddSheet() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add deeplink")
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (state.deeplinks.isEmpty()) {
                    EmptyState()
                } else {
                    DeeplinkList(
                        items = state.deeplinks,
                        showTagChip = state.selectedFilter is TagFilter.All,
                        onItemClick = { d ->
                            DeeplinkLauncher.launch(context, d.url)?.let { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        },
                        onItemLongClick = { d ->
                            clipboard.setText(AnnotatedString(d.url))
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch { snackbarHostState.showSnackbar("Deeplink copied") }
                        },
                        onEdit = { viewModel.openEditSheet(it) },
                        onDelete = { viewModel.requestDeleteDeeplink(it) }
                    )
                }
            }
        }
    }

    // Add/Edit Bottom Sheet
    val sheet = state.sheet
    if (sheet !is SheetState.Hidden) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        AddEditDeeplinkSheet(
            sheet = sheet,
            tags = state.tags,
            serverUrlError = state.sheetUrlError,
            onUrlChanged = viewModel::clearSheetUrlError,
            onDismiss = { viewModel.dismissSheet() },
            onSave = viewModel::save,
            onRequestCreateTag = { viewModel.openCreateTagDialog() },
            sheetState = sheetState
        )
    }

    // Dialogs
    state.pendingDeleteDeeplink?.let { d ->
        ConfirmDialog(
            title = "Delete deeplink?",
            message = "'${d.name}' will be removed.",
            confirmLabel = "Delete",
            onConfirm = viewModel::confirmDeleteDeeplink,
            onDismiss = viewModel::cancelDeleteDeeplink
        )
    }
    state.pendingDeleteTag?.let { t ->
        ConfirmDialog(
            title = "Delete tag?",
            message = "Deeplinks in '${t.name}' will be moved to Ungrouped.",
            confirmLabel = "Delete",
            onConfirm = viewModel::confirmDeleteTag,
            onDismiss = viewModel::cancelDeleteTag
        )
    }
    if (state.isCreateTagDialogOpen) {
        TagNameDialog(
            title = "New tag",
            initial = "",
            existing = state.tags,
            onDismiss = viewModel::dismissCreateTagDialog,
            onSubmit = { viewModel.createTag(it) }
        )
    }
    state.renamingTag?.let { t ->
        TagNameDialog(
            title = "Rename tag",
            initial = t.name,
            existing = state.tags.filter { it.id != t.id },
            onDismiss = viewModel::dismissRenameTag,
            onSubmit = { viewModel.renameTag(it) }
        )
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Link,
                contentDescription = null,
                modifier = Modifier.padding(8.dp)
            )
            Text("No deeplinks yet", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tap + to add one",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeeplinkList(
    items: List<DeeplinkWithTag>,
    showTagChip: Boolean,
    onItemClick: (DeeplinkEntity) -> Unit,
    onItemLongClick: (DeeplinkEntity) -> Unit,
    onEdit: (DeeplinkEntity) -> Unit,
    onDelete: (DeeplinkEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.deeplink.id }) { item ->
            DeeplinkRow(
                item = item,
                showTagChip = showTagChip,
                onClick = { onItemClick(item.deeplink) },
                onLongClick = { onItemLongClick(item.deeplink) },
                onEdit = { onEdit(item.deeplink) },
                onDelete = { onDelete(item.deeplink) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeeplinkRow(
    item: DeeplinkWithTag,
    showTagChip: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Column(modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.deeplink.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        item.deeplink.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showTagChip) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(item.tag?.name ?: TagEntity.UNGROUPED_NAME) },
                            leadingIcon = {
                                Icon(Icons.Filled.Label, contentDescription = null, modifier = Modifier.padding(end = 0.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    tags: List<TagEntity>,
    selected: TagFilter,
    onSelectAll: () -> Unit,
    onSelectTag: (TagEntity) -> Unit,
    onCreateTag: () -> Unit,
    onRenameTag: (TagEntity) -> Unit,
    onDeleteTag: (TagEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            "Deeplink Manager",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        NavigationDrawerItem(
            label = { Text("All") },
            selected = selected is TagFilter.All,
            onClick = onSelectAll,
            colors = NavigationDrawerItemDefaults.colors()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            "Tags",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(tags, key = { it.id }) { tag ->
                TagDrawerRow(
                    tag = tag,
                    selected = (selected as? TagFilter.Tag)?.tagId == tag.id,
                    onClick = { onSelectTag(tag) },
                    onRename = { onRenameTag(tag) },
                    onDelete = { onDeleteTag(tag) }
                )
            }
        }
        HorizontalDivider()
        NavigationDrawerItem(
            label = { Text("Create tag") },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            selected = false,
            onClick = onCreateTag,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun TagDrawerRow(
    tag: TagEntity,
    selected: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    NavigationDrawerItem(
        label = { Text(tag.name) },
        icon = { Icon(Icons.Filled.Label, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        badge = {
            if (!tag.isSystem) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Tag options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = { menuExpanded = false; onRename() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditDeeplinkSheet(
    sheet: SheetState,
    tags: List<TagEntity>,
    serverUrlError: String?,
    onUrlChanged: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (name: String, url: String, tagId: Long) -> Unit,
    onRequestCreateTag: () -> Unit,
    sheetState: androidx.compose.material3.SheetState
) {
    val editing = sheet as? SheetState.Edit
    val initialTagId = when (sheet) {
        is SheetState.Add -> sheet.tagId
        is SheetState.Edit -> sheet.deeplink.tagId
        else -> TagEntity.UNGROUPED_ID
    }
    var name by remember(sheet) { mutableStateOf(editing?.deeplink?.name ?: "") }
    var url by remember(sheet) { mutableStateOf(editing?.deeplink?.url ?: "") }
    var tagId by remember(sheet) { mutableStateOf(initialTagId) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var urlError by remember { mutableStateOf<String?>(null) }
    var tagMenuOpen by remember { mutableStateOf(false) }

    // If a new tag is created while the sheet is open, auto-select latest tag matching nothing
    // (the user can still pick manually). Kept simple: no auto-select side-effect here.

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (editing != null) "Edit deeplink" else "Add deeplink",
                style = MaterialTheme.typography.titleLarge
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Name") },
                singleLine = true,
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it; urlError = null; onUrlChanged() },
                label = { Text("Deeplink URL") },
                placeholder = { Text("e.g. hungerstation://?c=SA") },
                singleLine = true,
                isError = urlError != null || serverUrlError != null,
                supportingText = { (urlError ?: serverUrlError)?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri
                )
            )
            // Tag selector
                androidx.compose.material3.ExposedDropdownMenuBox(
                    expanded = tagMenuOpen,
                    onExpandedChange = { tagMenuOpen = it }
                ) {
                    val selectedTagName = tags.firstOrNull { it.id == tagId }?.name
                        ?: TagEntity.UNGROUPED_NAME
                    OutlinedTextField(
                        value = selectedTagName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tag") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagMenuOpen) },
                        modifier = Modifier
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = tagMenuOpen,
                        onDismissRequest = { tagMenuOpen = false }
                    ) {
                        tags.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.name) },
                                onClick = { tagId = t.id; tagMenuOpen = false }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("+ Create new tag…") },
                            onClick = { tagMenuOpen = false; onRequestCreateTag() }
                        )
                    }
                }
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                androidx.compose.material3.Button(
                    onClick = {
                        val n = name.trim()
                        val u = url.trim()
                        nameError = if (n.isEmpty()) "Name is required" else null
                        urlError = when {
                            u.isEmpty() -> "Deeplink URL is required"
                            !u.contains(':') -> "Enter a valid deeplink (e.g. app://path)"
                            else -> null
                        }
                        if (nameError == null && urlError == null) {
                            onSave(n, u, tagId)
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) { Text("Save") }
            }
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TagNameDialog(
    title: String,
    initial: String,
    existing: List<TagEntity>,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it; error = null },
                singleLine = true,
                isError = error != null,
                supportingText = { error?.let { Text(it) } },
                label = { Text("Tag name") },
                modifier = Modifier.fillMaxWidth().imePadding()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val v = value.trim()
                error = when {
                    v.isEmpty() -> "Required"
                    v.equals(TagEntity.UNGROUPED_NAME, ignoreCase = true) -> "Reserved name"
                    existing.any { it.name.equals(v, ignoreCase = true) } -> "Tag already exists"
                    else -> null
                }
                if (error == null) onSubmit(v)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
