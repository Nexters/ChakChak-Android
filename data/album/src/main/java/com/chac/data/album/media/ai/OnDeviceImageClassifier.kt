package com.chac.data.album.media.ai

import android.content.Context
import androidx.core.net.toUri
import com.chac.domain.album.media.model.PromptCategory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit Image Labeling 기반 온디바이스 분류기.
 */
internal class OnDeviceImageClassifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val labeler: ImageLabeler by lazy {
        ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    }

    private val cache = ConcurrentHashMap<Long, Map<PromptCategory, Float>>()

    suspend fun classify(
        mediaId: Long,
        uriString: String,
    ): Map<PromptCategory, Float> {
        val cached = cache[mediaId]
        if (cached != null) return cached

        val result = withContext(Dispatchers.IO) {
            runCatching {
                val image = InputImage.fromFilePath(context, uriString.toUri())
                val labels = label(image)
                mapLabels(labels)
            }.onFailure { throwable ->
                Timber.w(throwable, "Failed to classify mediaId=$mediaId")
            }.getOrDefault(emptyMap())
        }

        cache[mediaId] = result
        return result
    }

    private suspend fun label(image: InputImage) = suspendCancellableCoroutine { continuation ->
        labeler.process(image)
            .addOnSuccessListener { labels ->
                continuation.resume(labels)
            }.addOnFailureListener { throwable ->
                continuation.resumeWithException(throwable)
            }
    }

    private fun mapLabels(labels: List<ImageLabel>): Map<PromptCategory, Float> {
        val categoryScores = mutableMapOf<PromptCategory, Float>()

        labels.forEach { label ->
            if (label.confidence < MIN_CONFIDENCE) return@forEach

            val normalizedLabel = label.text.lowercase()
            val category = when {
                normalizedLabel.containsAny(LANDSCAPE_KEYWORDS) -> PromptCategory.LANDSCAPE
                normalizedLabel.containsAny(ANIMAL_KEYWORDS) -> PromptCategory.ANIMAL
                normalizedLabel.containsAny(FOOD_KEYWORDS) -> PromptCategory.FOOD
                normalizedLabel.containsAny(DOCUMENT_KEYWORDS) -> PromptCategory.DOCUMENT
                normalizedLabel.containsAny(PERSON_KEYWORDS) -> PromptCategory.PERSON
                else -> null
            } ?: return@forEach

            categoryScores[category] = maxOf(categoryScores[category] ?: 0f, label.confidence)
        }

        return categoryScores
    }

    private fun String.containsAny(keywords: Set<String>): Boolean = keywords.any { keyword ->
        contains(keyword)
    }

    companion object {
        private const val MIN_CONFIDENCE = 0.55f

        private val LANDSCAPE_KEYWORDS = setOf(
            "landscape",
            "nature",
            "outdoor",
            "mountain",
            "beach",
            "sky",
            "sea",
            "lake",
            "sunset",
            "tree",
            "plant",
        )
        private val ANIMAL_KEYWORDS = setOf(
            "animal",
            "pet",
            "dog",
            "cat",
            "bird",
            "wildlife",
            "horse",
            "cow",
        )
        private val FOOD_KEYWORDS = setOf(
            "food",
            "meal",
            "dish",
            "dessert",
            "fruit",
            "vegetable",
            "drink",
            "beverage",
            "cake",
        )
        private val DOCUMENT_KEYWORDS = setOf(
            "document",
            "paper",
            "text",
            "book",
            "newspaper",
            "receipt",
            "menu",
            "poster",
        )
        private val PERSON_KEYWORDS = setOf(
            "person",
            "human",
            "face",
            "portrait",
            "selfie",
            "man",
            "woman",
            "people",
        )
    }
}
