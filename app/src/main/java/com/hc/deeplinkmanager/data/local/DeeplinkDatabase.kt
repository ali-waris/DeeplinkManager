package com.hc.deeplinkmanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DeeplinkEntity::class, TagEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DeeplinkDatabase : RoomDatabase() {
    abstract fun deeplinkDao(): DeeplinkDao
    abstract fun tagDao(): TagDao

    companion object {
        private const val DB_NAME = "deeplink_manager.db"

        fun build(context: Context): DeeplinkDatabase =
            Room.databaseBuilder(context, DeeplinkDatabase::class.java, DB_NAME)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Seed the system "Ungrouped" tag with a fixed id.
                        db.execSQL(
                            "INSERT INTO tags (id, name, isSystem, createdAt) VALUES (?, ?, ?, ?)",
                            arrayOf<Any>(
                                TagEntity.UNGROUPED_ID,
                                TagEntity.UNGROUPED_NAME,
                                1,
                                System.currentTimeMillis()
                            )
                        )
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
    }
}

