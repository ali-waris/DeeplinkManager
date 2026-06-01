package com.hc.deeplinkmanager.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class DeeplinkWithTag(
    @Embedded val deeplink: DeeplinkEntity,
    @Relation(
        parentColumn = "tagId",
        entityColumn = "id"
    )
    val tag: TagEntity?
)

