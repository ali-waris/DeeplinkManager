package com.hc.deeplinkmanager.data.repo

import androidx.room.withTransaction
import com.hc.deeplinkmanager.data.local.DeeplinkDao
import com.hc.deeplinkmanager.data.local.DeeplinkEntity
import com.hc.deeplinkmanager.data.local.DeeplinkWithTag
import com.hc.deeplinkmanager.data.local.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface DeeplinkRepository {
    fun observeAll(): Flow<List<DeeplinkWithTag>>
    fun observeByTag(tagId: Long): Flow<List<DeeplinkWithTag>>
    suspend fun upsert(deeplink: DeeplinkEntity)
    suspend fun delete(deeplink: DeeplinkEntity)
    /** Snapshot of all existing (name|url) keys — used to deduplicate imports. */
    suspend fun existingNameUrlKeys(): Set<String>
    /** Returns id of any existing deeplink with [url], or null. */
    suspend fun findIdByUrl(url: String): Long?
}

class DeeplinkRepositoryImpl @Inject constructor(
    private val dao: DeeplinkDao
) : DeeplinkRepository {
    override fun observeAll(): Flow<List<DeeplinkWithTag>> = dao.observeAllWithTag()
    override fun observeByTag(tagId: Long) = dao.observeByTagWithTag(tagId)
    override suspend fun upsert(deeplink: DeeplinkEntity) {
        if (deeplink.id == 0L) dao.insert(deeplink) else dao.update(deeplink)
    }
    override suspend fun delete(deeplink: DeeplinkEntity) = dao.delete(deeplink)
    override suspend fun existingNameUrlKeys(): Set<String> = dao.getAllNameUrlKeys().toHashSet()
    override suspend fun findIdByUrl(url: String): Long? = dao.findIdByUrl(url)
}

interface TagRepository {
    fun observeAll(): Flow<List<TagEntity>>
    suspend fun create(name: String): Long
    suspend fun rename(tag: TagEntity, newName: String)
    /** Reassigns all deeplinks of [tag] to "Ungrouped" then deletes the tag. Returns reassigned count. */
    suspend fun deleteAndReassign(tag: TagEntity): Int
    /** Returns id for tag named [name] (case-insensitive); creates if absent. */
    suspend fun getOrCreateByName(name: String): Long
}

class TagRepositoryImpl @Inject constructor(
    private val tagDao: com.hc.deeplinkmanager.data.local.TagDao,
    private val deeplinkDao: DeeplinkDao,
    private val database: com.hc.deeplinkmanager.data.local.DeeplinkDatabase
) : TagRepository {
    override fun observeAll(): Flow<List<TagEntity>> = tagDao.observeAll()

    override suspend fun create(name: String): Long =
        tagDao.insert(TagEntity(name = name.trim()))

    override suspend fun rename(tag: TagEntity, newName: String) {
        require(!tag.isSystem) { "System tag cannot be renamed" }
        tagDao.update(tag.copy(name = newName.trim()))
    }

    override suspend fun deleteAndReassign(tag: TagEntity): Int {
        require(!tag.isSystem) { "System tag cannot be deleted" }
        return database.withTransaction {
            val moved = deeplinkDao.reassignTag(tag.id, TagEntity.UNGROUPED_ID)
            tagDao.delete(tag)
            moved
        }
    }

    override suspend fun getOrCreateByName(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.equals(TagEntity.UNGROUPED_NAME, ignoreCase = true)) {
            return TagEntity.UNGROUPED_ID
        }
        return tagDao.findByName(trimmed)?.id ?: tagDao.insert(TagEntity(name = trimmed))
    }
}


