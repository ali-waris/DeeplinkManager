package com.hc.deeplinkmanager.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY isSystem DESC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): TagEntity?

    @Query("SELECT COUNT(*) FROM deeplinks WHERE tagId = :tagId")
    fun observeDeeplinkCount(tagId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)
}

@Dao
interface DeeplinkDao {
    @Transaction
    @Query("SELECT * FROM deeplinks ORDER BY createdAt DESC")
    fun observeAllWithTag(): Flow<List<DeeplinkWithTag>>

    @Transaction
    @Query("SELECT * FROM deeplinks WHERE tagId = :tagId ORDER BY createdAt DESC")
    fun observeByTagWithTag(tagId: Long): Flow<List<DeeplinkWithTag>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(deeplink: DeeplinkEntity): Long

    @Update
    suspend fun update(deeplink: DeeplinkEntity)

    @Delete
    suspend fun delete(deeplink: DeeplinkEntity)

    @Query("UPDATE deeplinks SET tagId = :toTagId WHERE tagId = :fromTagId")
    suspend fun reassignTag(fromTagId: Long, toTagId: Long): Int

    /** "name|url" snapshot of every row — used by import to deduplicate cheaply. */
    @Query("SELECT name || '|' || url FROM deeplinks")
    suspend fun getAllNameUrlKeys(): List<String>

    /** Returns the id of an existing deeplink with the given url, or null. */
    @Query("SELECT id FROM deeplinks WHERE url = :url LIMIT 1")
    suspend fun findIdByUrl(url: String): Long?
}

