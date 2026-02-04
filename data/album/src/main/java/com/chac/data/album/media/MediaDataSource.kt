package com.chac.data.album.media

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import com.chac.domain.album.media.model.Media
import com.chac.domain.album.media.model.MediaLocation
import com.chac.domain.album.media.model.MediaSortOrder
import com.chac.domain.album.media.model.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class MediaDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val contentResolver = context.contentResolver
    private val ioDispatcher = Dispatchers.IO

    suspend fun getMedia(
        startTime: Long,
        endTime: Long,
        mediaType: MediaType,
        mediaSortOrder: MediaSortOrder,
    ): List<Media> = withContext(ioDispatcher) {
        val items = mutableListOf<Media>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
        )

        // 미디어 타입 필터 설정
        val typeSelection = when (mediaType) {
            MediaType.IMAGE -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}"
            MediaType.VIDEO -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}"
            MediaType.ALL ->
                "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} OR " +
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO})"
        }

        // 시간 필터 설정
        val timeSelection = "${MediaStore.Files.FileColumns.DATE_TAKEN} IS NOT NULL AND " +
            "${MediaStore.Files.FileColumns.DATE_TAKEN} > 0 AND " +
            "${MediaStore.Files.FileColumns.DATE_TAKEN} >= ? AND " +
            "${MediaStore.Files.FileColumns.DATE_TAKEN} <= ?"

        // 최종 쿼리 조건 구성
        val selection = "$typeSelection AND $timeSelection"
        val selectionArgs = arrayOf(startTime.toString(), endTime.toString())

        // 정렬 설정
        val mediaSortOrderSQL = when (mediaSortOrder) {
            MediaSortOrder.NEWEST_FIRST -> "${MediaStore.Files.FileColumns.DATE_TAKEN} DESC"
            MediaSortOrder.OLDEST_FIRST -> "${MediaStore.Files.FileColumns.DATE_TAKEN} ASC"
        }

        var cursor: Cursor? = null

        try {
            cursor = contentResolver.query(
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                projection,
                selection,
                selectionArgs,
                mediaSortOrderSQL,
            )

            cursor?.let {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val dateTakenColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
                val mediaTypeColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val dateTaken = it.getLong(dateTakenColumn)
                    val mediaTypeValue = it.getInt(mediaTypeColumn)

                    // DATE_TAKEN이 유효하지 않은 경우 무시
                    if (dateTaken <= 0) continue

                    val type = when (mediaTypeValue) {
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> MediaType.IMAGE
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> MediaType.VIDEO
                        else -> MediaType.IMAGE
                    }
                    val collection = when (type) {
                        MediaType.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        MediaType.ALL -> MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    }
                    val uri = ContentUris.withAppendedId(
                        collection,
                        id,
                    )

                    items.add(
                        Media(
                            id = id,
                            uriString = uri.toString(),
                            dateTaken = dateTaken,
                            mediaType = type,
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        items
    }

    suspend fun getMediaLocation(uri: String): MediaLocation? = withContext(ioDispatcher) {
        getMediaLocation(context, uri)
    }

    /**
     * 미디어를 지정한 앨범으로 저장한다
     *
     * 저장 흐름:
     * 1) 원본 미디어를 지정 경로로 이동 시도
     * 2) 이동 실패 시 MediaStore에 새 항목을 만들고 복사
     * 3) 복사 성공 시 원본 삭제 및 pending 해제
     *
     * pending 처리:
     * - 복사 중에는 IS_PENDING=1로 설정해 사용자에게 노출되지 않게 함
     * - 복사가 끝나면 IS_PENDING=0으로 변경해 갤러리에 표시되도록 함
     *
     * @param title 앨범 제목
     * @param mediaList 저장할 미디어 목록
     * @return 저장에 성공한 미디어 목록
     */
    suspend fun saveAlbum(
        title: String,
        mediaList: List<Media>,
    ): List<Media> = withContext(ioDispatcher) {
        val albumTitle = sanitizeAlbumTitle(title)
        val savedMedia = mutableListOf<Media>()

        mediaList.forEach { media ->
            val sourceUri = media.uriString.toUri()
            val relativePath = "${Environment.DIRECTORY_PICTURES}/$albumTitle"

            if (moveToAlbum(sourceUri, relativePath)) {
                savedMedia.add(media)
                return@forEach
            }

            val collection = when (media.mediaType) {
                MediaType.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                MediaType.ALL -> MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            }

            val displayName = queryString(sourceUri, MediaStore.MediaColumns.DISPLAY_NAME)
                ?: buildFallbackName(media)
            val mimeType = queryString(sourceUri, MediaStore.MediaColumns.MIME_TYPE)
                ?: defaultMimeType(media.mediaType)

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val targetUri = contentResolver.insert(collection, contentValues) ?: return@forEach
            val copied = copyToAlbum(sourceUri, targetUri)

            if (!copied) {
                contentResolver.delete(targetUri, null, null)
                return@forEach
            }

            val pendingValues = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            contentResolver.update(targetUri, pendingValues, null, null)
            deleteSource(sourceUri)

            savedMedia.add(media)
        }

        savedMedia
    }

    /**
     * 앨범 제목을 저장 가능한 문자열로 정규화한다.
     *
     * @param title 원본 앨범 제목
     * @return 공백은 기본값으로 대체하고, 경로 구분 문자를 안전한 문자로 치환한 제목
     */
    private fun sanitizeAlbumTitle(title: String): String {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return "Chac"
        return trimmed.replace("/", "_")
    }

    /**
     * 표시 이름이 없을 때 사용할 대체 파일명을 생성한다.
     *
     * @param media 대상 미디어
     * @return 대체 파일명
     */
    private fun buildFallbackName(media: Media): String = "chac_${media.id}"

    /**
     * 미디어 타입에 맞는 기본 MIME 타입을 반환한다.
     *
     * @param mediaType 미디어 타입
     * @return 기본 MIME 타입
     */
    private fun defaultMimeType(mediaType: MediaType): String = when (mediaType) {
        MediaType.IMAGE -> "image/*"
        MediaType.VIDEO -> "video/*"
        MediaType.ALL -> "application/octet-stream"
    }

    /**
     * 소스 URI의 데이터를 대상 URI로 복사한다.
     *
     * @param sourceUri 복사할 원본 URI
     * @param targetUri 복사 대상 URI
     * @return 복사 성공 여부
     */
    private fun copyToAlbum(
        sourceUri: Uri,
        targetUri: Uri,
    ): Boolean = runCatching {
        contentResolver.openInputStream(sourceUri)?.use { input ->
            contentResolver.openOutputStream(targetUri)?.use { output ->
                input.copyTo(output)
                true
            } ?: false
        } ?: false
    }.getOrDefault(false)

    /**
     * 미디어를 지정한 상대 경로로 이동한다
     *
     * @param sourceUri 이동할 미디어 URI
     * @param relativePath 이동할 상대 경로
     * @return 이동 성공 여부
     */
    private fun moveToAlbum(
        sourceUri: Uri,
        relativePath: String,
    ): Boolean = runCatching {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }
        contentResolver.update(sourceUri, contentValues, null, null) > 0
    }.getOrDefault(false)

    /**
     * 원본 미디어를 삭제한다
     *
     * @param sourceUri 삭제할 미디어 URI
     */
    private fun deleteSource(sourceUri: Uri) {
        runCatching { contentResolver.delete(sourceUri, null, null) }
    }

    /**
     * 지정된 컬럼 값을 URI에서 조회한다.
     *
     * @param uri 조회할 대상 URI
     * @param column 조회할 컬럼명
     * @return 조회 결과 문자열. 없으면 null
     */
    private fun queryString(
        uri: Uri,
        column: String,
    ): String? {
        val projection = arrayOf(column)
        return contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val index = cursor.getColumnIndex(column)
            if (index < 0) return@use null
            cursor.getString(index)
        }
    }
}
