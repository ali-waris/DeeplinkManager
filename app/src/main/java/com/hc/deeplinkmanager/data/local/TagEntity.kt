package com.hc.deeplinkmanager.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val isSystem: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val UNGROUPED_ID: Long = 1L
        const val UNGROUPED_NAME: String = "Ungrouped"
    }
}

