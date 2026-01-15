package com.chac.data.photo.media.clustering.di

import com.chac.data.photo.media.clustering.ClusteringStrategy
import com.chac.data.photo.media.clustering.LocationBasedClusteringStrategy
import com.chac.data.photo.media.clustering.TimeBasedClusteringStrategy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
internal interface ClusteringModule {
    @Binds
    @TimeBasedClustering
    fun bindTimeBasedClusteringStrategy(timeBasedClusteringStrategy: TimeBasedClusteringStrategy): ClusteringStrategy

    @Binds
    @LocationBasedClustering
    fun bindLocationBasedClusteringStrategy(locationBasedClusteringStrategy: LocationBasedClusteringStrategy): ClusteringStrategy
}

@Qualifier
annotation class TimeBasedClustering

@Qualifier
annotation class LocationBasedClustering
