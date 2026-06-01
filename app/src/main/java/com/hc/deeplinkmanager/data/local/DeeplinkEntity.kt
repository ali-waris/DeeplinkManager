package com.hc.deeplinkmanager.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "deeplinks",
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("tagId")]
)
data class DeeplinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val url: String,
    val tagId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

