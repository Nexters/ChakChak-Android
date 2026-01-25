package com.chac.feature.album.model

import androidx.compose.runtime.Immutable
import com.chac.domain.album.media.MediaType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/**
 * 미디어 UI 표현 모델.
 *
 * @property id 미디어 식별자
 * @property uriString 미디어 URI 문자열
 * @property dateTaken 촬영 시각(에포크 밀리초)
 * @property mediaType 미디어 타입
 */
@Immutable
@Serializable(with = MediaUiModelSerializer::class)
data class MediaUiModel(
    val id: Long,
    val uriString: String,
    val dateTaken: Long,
    val mediaType: MediaType,
)

private object MediaUiModelSerializer : KSerializer<MediaUiModel> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MediaUiModel") {
        element<Long>("id")
        element<String>("uriString")
        element<Long>("dateTaken")
        element<String>("mediaType")
    }

    override fun serialize(encoder: Encoder, value: MediaUiModel) {
        encoder.encodeStructure(descriptor) {
            encodeLongElement(descriptor, 0, value.id)
            encodeStringElement(descriptor, 1, value.uriString)
            encodeLongElement(descriptor, 2, value.dateTaken)
            encodeStringElement(descriptor, 3, value.mediaType.name)
        }
    }

    override fun deserialize(decoder: Decoder): MediaUiModel = decoder.decodeStructure(descriptor) {
        var id = 0L
        var uriString = ""
        var dateTaken = 0L
        var mediaType = MediaType.IMAGE
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break
                0 -> id = decodeLongElement(descriptor, 0)
                1 -> uriString = decodeStringElement(descriptor, 1)
                2 -> dateTaken = decodeLongElement(descriptor, 2)
                3 -> {
                    val typeName = decodeStringElement(descriptor, 3)
                    mediaType = runCatching { MediaType.valueOf(typeName) }.getOrDefault(MediaType.IMAGE)
                }

                else -> throw IllegalStateException("Unknown index $index")
            }
        }
        MediaUiModel(
            id = id,
            uriString = uriString,
            dateTaken = dateTaken,
            mediaType = mediaType,
        )
    }
}
