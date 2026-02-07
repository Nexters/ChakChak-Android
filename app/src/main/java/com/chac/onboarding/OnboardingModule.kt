package com.chac.onboarding

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface OnboardingModule {
    @Binds
    @Singleton
    fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository
}
