package com.pragmatsoft.daf.di

import android.content.Context
import com.pragmatsoft.daf.data.local.DataStoreRepository
import com.pragmatsoft.daf.utils.AndroidStringProvider
import com.pragmatsoft.daf.utils.AudioHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CommonModule {
    @Provides
    @Singleton
    fun provideTasksRepository(@ApplicationContext context: Context): DataStoreRepository {
        return DataStoreRepository(context)
    }

    @Provides
    @Singleton
    fun provideAudioHelper(@ApplicationContext context: Context): AudioHelper {
        return AudioHelper(context)
    }

    @Provides
    @Singleton
    fun provideAndroidStringProvider(@ApplicationContext context: Context): AndroidStringProvider {
        return AndroidStringProvider(context)
    }
}