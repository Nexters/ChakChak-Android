package com.chac.onboarding

import kotlinx.coroutines.flow.Flow

/** 온보딩 완료 상태를 관리하는 저장소 */
interface OnboardingRepository {
    /** 온보딩 완료 여부를 관찰한다 */
    val isCompleted: Flow<Boolean>

    /** 온보딩을 완료 처리한다 */
    suspend fun markCompleted()
}
