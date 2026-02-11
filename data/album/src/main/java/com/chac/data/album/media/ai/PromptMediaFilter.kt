package com.chac.data.album.media.ai

import com.chac.domain.album.media.model.Media
import com.chac.domain.album.media.model.PromptSpec
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * 프롬프트 스펙에 맞춰 미디어를 필터링한다.
 */
internal class PromptMediaFilter @Inject constructor(
    private val classifier: OnDeviceImageClassifier,
) {
    suspend fun filter(
        mediaList: List<Media>,
        promptSpec: PromptSpec?,
    ): List<Media> {
        if (promptSpec == null || promptSpec.categories.isEmpty()) {
            return mediaList
        }

        val targetCategories = promptSpec.categories

        return coroutineScope {
            mediaList.chunked(CLASSIFY_BATCH_SIZE).flatMap { batch ->
                batch.map { media ->
                    async {
                        val scores = classifier.classify(
                            mediaId = media.id,
                            uriString = media.uriString,
                        )
                        if (scores.keys.any { it in targetCategories }) {
                            media
                        } else {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }
        }
    }

    companion object {
        private const val CLASSIFY_BATCH_SIZE = 12
    }
}
