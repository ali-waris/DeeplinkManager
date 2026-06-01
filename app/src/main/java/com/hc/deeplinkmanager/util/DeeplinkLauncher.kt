package com.hc.deeplinkmanager.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

object DeeplinkLauncher {
    /**
     * Launches a deeplink URL. Returns null on success, or a user-facing error message on failure.
     */
    fun launch(context: Context, rawUrl: String): String? {
        val url = rawUrl.trim()
        if (url.isEmpty()) return "Empty deeplink"
        return try {
            val uri = url.toUri()
            if (uri.scheme.isNullOrBlank()) return "Invalid deeplink (missing scheme)"
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            null
        } catch (e: ActivityNotFoundException) {
            "No app found to open this link"
        } catch (e: Exception) {
            "Could not open: ${e.message ?: "unknown error"}"
        }
    }
}

