package com.hc.deeplinkmanager.di

import android.content.Context
import com.hc.deeplinkmanager.data.local.DeeplinkDao
import com.hc.deeplinkmanager.data.local.DeeplinkDatabase
import com.hc.deeplinkmanager.data.local.TagDao
import com.hc.deeplinkmanager.data.repo.DeeplinkRepository
import com.hc.deeplinkmanager.data.repo.DeeplinkRepositoryImpl
import com.hc.deeplinkmanager.data.repo.TagRepository
import com.hc.deeplinkmanager.data.repo.TagRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DeeplinkDatabase =
        DeeplinkDatabase.build(context)

    @Provides
    fun provideDeeplinkDao(db: DeeplinkDatabase): DeeplinkDao = db.deeplinkDao()

    @Provides
    fun provideTagDao(db: DeeplinkDatabase): TagDao = db.tagDao()
}

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDeeplinkRepository(impl: DeeplinkRepositoryImpl): DeeplinkRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository
}

