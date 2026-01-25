package com.chac.feature.album.model

import com.chac.domain.album.media.MediaType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** MediaUiModelSerializer 테스트 */
class MediaUiModelSerializerTest {
    private val json = Json { ignoreUnknownKeys = true }

    /** MediaUiModelSerializer 왕복 직렬화를 검증한다 */
    @Test
    fun `직렬화 후 역직렬화하면 동일한 값이 나온다`() {
        // GIVEN
        val model = MediaUiModel(
            id = 12L,
            uriString = "content://media/12",
            dateTaken = 1700000000000L,
            mediaType = MediaType.IMAGE,
        )

        // WHEN
        val encoded = json.encodeToString(MediaUiModel.serializer(), model)
        val decoded = json.decodeFromString(MediaUiModel.serializer(), encoded)

        // THEN
        assertEquals(model, decoded)
    }

    /** MediaUiModelSerializer가 잘못된 타입을 만나면 실패하는지 확인한다 */
    @Test
    fun `잘못된 타입이 있으면 역직렬화가 실패한다`() {
        // GIVEN
        val encoded = """{"id": "oops", "uriString": "content://media/2", "dateTaken": 2000, "mediaType": "IMAGE"}""".trimIndent()

        // WHEN, THEN
        assertThrows(SerializationException::class.java) {
            json.decodeFromString(MediaUiModel.serializer(), encoded)
        }
    }
}
