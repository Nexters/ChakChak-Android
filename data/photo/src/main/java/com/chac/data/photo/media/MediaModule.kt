package com.chac.data.photo.media

import com.chac.domain.photo.media.MediaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface MediaModule {
    @Binds
    @Singleton
    fun bindMediaRepository(mediaRepository: MediaRepositoryImpl): MediaRepository
}
