package com.chac.data.photo.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.chac.domain.photo.media.Media
import com.chac.domain.photo.media.MediaLocation
import com.chac.domain.photo.media.MediaSortOrder
import com.chac.domain.photo.media.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class MediaDataSource
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val contentResolver = context.contentResolver
        private val ioDispatcher = Dispatchers.IO

        suspend fun getMedia(
            startTime: Long,
            endTime: Long,
            mediaType: MediaType,
            mediaSortOrder: MediaSortOrder,
        ): List<Media> =
            withContext(ioDispatcher) {
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

                            val uri = ContentUris.withAppendedId(
                                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                                id,
                            )

                            val type = when (mediaTypeValue) {
                                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> MediaType.IMAGE
                                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> MediaType.VIDEO
                                else -> MediaType.IMAGE
                            }

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

        suspend fun getMediaLocation(uri: String): MediaLocation? =
            withContext(ioDispatcher) {
                getMediaLocation(context, uri)
            }
    }
