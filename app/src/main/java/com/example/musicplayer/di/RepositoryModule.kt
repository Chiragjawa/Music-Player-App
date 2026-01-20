package com.example.musicplayer.di

import com.example.musicplayer.data.api.SaavnApi
import com.example.musicplayer.data.repository.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMusicRepository(api: SaavnApi): MusicRepository {
        return MusicRepository(api)
    }
}
