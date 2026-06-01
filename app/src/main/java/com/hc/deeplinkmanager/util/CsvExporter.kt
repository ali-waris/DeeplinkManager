package com.hc.deeplinkmanager.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.hc.deeplinkmanager.data.local.DeeplinkWithTag
import com.hc.deeplinkmanager.data.local.TagEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    /** Builds a CSV string (RFC 4180-ish: quote fields and double inner quotes). */
    fun buildCsv(items: List<DeeplinkWithTag>): String {
        val sb = StringBuilder("Name,URL,Tag,CreatedAt").append('\n')
        items.forEach { item ->
            val tagName = item.tag?.name ?: TagEntity.UNGROUPED_NAME
            sb.append(escape(item.deeplink.name)).append(',')
                .append(escape(item.deeplink.url)).append(',')
                .append(escape(tagName)).append(',')
                .append(item.deeplink.createdAt)
                .append('\n')
        }
        return sb.toString()
    }

    private fun escape(value: String): String {
        val needsQuoting = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuoting) "\"$escaped\"" else escaped
    }

    fun fileName(filterLabel: String): String {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val safeLabel = filterLabel.replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "all" }
        return "deeplinks_${safeLabel}_$ts.csv"
    }
}

sealed class ExportResult {
    data class Success(val displayPath: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
    data object PermissionRequired : ExportResult()
}

object ExportStorage {
    /**
     * Writes [csv] to the public Downloads directory.
     * - API 29+: MediaStore (no permission).
     * - API 24-28: requires WRITE_EXTERNAL_STORAGE granted (else returns PermissionRequired).
     */
    fun saveCsvToDownloads(
        context: Context,
        fileName: String,
        csv: String,
        hasLegacyWritePermission: () -> Boolean
    ): ExportResult {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collection, values)
                    ?: return ExportResult.Error("Could not create file")
                resolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                    ?: return ExportResult.Error("Could not open file for writing")
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                ExportResult.Success("Download/$fileName")
            } else {
                if (!hasLegacyWritePermission()) return ExportResult.PermissionRequired
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists() && !dir.mkdirs()) return ExportResult.Error("Could not create Downloads folder")
                val file = File(dir, fileName)
                file.writeText(csv)
                ExportResult.Success(file.absolutePath)
            }
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Export failed")
        }
    }
}

data class ParsedDeeplink(
    val name: String,
    val url: String,
    val tagName: String
)

sealed class ImportResult {
    data class Success(val imported: Int, val skipped: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

object CsvImporter {
    /**
     * Parses CSV content with columns: Name,URL,Tag(,CreatedAt). Header row is auto-detected.
     * Handles quoted fields and embedded commas/quotes per RFC 4180.
     */
    fun parse(csv: String): List<ParsedDeeplink> {
        val rows = parseRows(csv)
        if (rows.isEmpty()) return emptyList()
        val first = rows.first()
        val headerLooks = first.size >= 2 &&
            first[0].equals("name", ignoreCase = true) &&
            first[1].equals("url", ignoreCase = true)
        val dataRows = if (headerLooks) rows.drop(1) else rows
        return dataRows.mapNotNull { row ->
            val name = row.getOrNull(0)?.trim().orEmpty()
            val url = row.getOrNull(1)?.trim().orEmpty()
            val tag = row.getOrNull(2)?.trim().orEmpty()
            if (name.isEmpty() || url.isEmpty()) null
            else ParsedDeeplink(name, url, tag)
        }
    }

    private fun parseRows(csv: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val field = StringBuilder()
        var inQuotes = false
        val row = mutableListOf<String>()
        var i = 0
        while (i < csv.length) {
            val c = csv[i]
            when {
                inQuotes -> when {
                    c == '"' && i + 1 < csv.length && csv[i + 1] == '"' -> {
                        field.append('"'); i++
                    }
                    c == '"' -> inQuotes = false
                    else -> field.append(c)
                }
                c == '"' -> inQuotes = true
                c == ',' -> {
                    row.add(field.toString()); field.setLength(0)
                }
                c == '\r' -> { /* swallow */ }
                c == '\n' -> {
                    row.add(field.toString()); field.setLength(0)
                    if (row.any { it.isNotEmpty() }) rows.add(row.toList())
                    row.clear()
                }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            if (row.any { it.isNotEmpty() }) rows.add(row.toList())
        }
        return rows
    }
}


