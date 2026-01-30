package com.chac.data.album.media.timeformat.di

import com.chac.data.album.media.timeformat.AndroidTimeFormatProvider
import com.chac.data.album.media.timeformat.TimeFormatProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface TimeFormatProviderModule {
    @Binds
    @Singleton
    fun bindTimeFormatProvider(provider: AndroidTimeFormatProvider): TimeFormatProvider
}
