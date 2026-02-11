package com.chac.domain.album.media.model

/**
 * 자연어 프롬프트를 기반으로 생성된 필터 스펙.
 *
 * @property rawText 사용자가 입력한 원본 프롬프트
 * @property categories 추출된 타겟 카테고리 목록
 */
data class PromptSpec(
    val rawText: String,
    val categories: Set<PromptCategory>,
) {
    val cacheKey: String
        get() = categories
            .map { it.name }
            .sorted()
            .joinToString(separator = "_")
            .ifBlank { "ALL" }
}
