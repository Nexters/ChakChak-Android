package com.chac.data.album.media.ai

import com.chac.domain.album.media.model.PromptCategory
import com.chac.domain.album.media.model.PromptSpec
import javax.inject.Inject

/**
 * 사용자 프롬프트를 내부 카테고리 스펙으로 변환한다.
 */
class PromptInterpreter @Inject constructor() {
    fun interpret(promptText: String?): PromptSpec? {
        val normalized = promptText
            ?.trim()
            ?.lowercase()
            .orEmpty()

        if (normalized.isBlank()) return null

        val categories = buildSet {
            if (normalized.containsAny(LANDSCAPE_KEYWORDS)) add(PromptCategory.LANDSCAPE)
            if (normalized.containsAny(ANIMAL_KEYWORDS)) add(PromptCategory.ANIMAL)
            if (normalized.containsAny(FOOD_KEYWORDS)) add(PromptCategory.FOOD)
            if (normalized.containsAny(DOCUMENT_KEYWORDS)) add(PromptCategory.DOCUMENT)
            if (normalized.containsAny(PERSON_KEYWORDS)) add(PromptCategory.PERSON)
        }

        return PromptSpec(
            rawText = promptText.orEmpty().trim(),
            categories = categories,
        )
    }

    private fun String.containsAny(keywords: Set<String>): Boolean = keywords.any { keyword ->
        contains(keyword)
    }

    companion object {
        private val LANDSCAPE_KEYWORDS = setOf(
            "풍경",
            "자연",
            "바다",
            "해변",
            "산",
            "하늘",
            "여행",
            "landscape",
            "nature",
            "beach",
            "mountain",
            "sky",
        )
        private val ANIMAL_KEYWORDS = setOf(
            "동물",
            "강아지",
            "고양이",
            "반려",
            "펫",
            "새",
            "animal",
            "pet",
            "dog",
            "cat",
            "bird",
        )
        private val FOOD_KEYWORDS = setOf(
            "음식",
            "식사",
            "요리",
            "디저트",
            "카페",
            "food",
            "meal",
            "dish",
            "dessert",
            "cafe",
        )
        private val DOCUMENT_KEYWORDS = setOf(
            "문서",
            "서류",
            "영수증",
            "책",
            "필기",
            "자료",
            "document",
            "receipt",
            "paper",
            "text",
            "book",
        )
        private val PERSON_KEYWORDS = setOf(
            "인물",
            "사람",
            "얼굴",
            "셀카",
            "가족",
            "친구",
            "person",
            "human",
            "face",
            "selfie",
            "portrait",
        )
    }
}
