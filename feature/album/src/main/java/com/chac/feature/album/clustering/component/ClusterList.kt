package com.chac.feature.album.clustering.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.chac.core.designsystem.ui.component.ChacImage
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R
import com.chac.domain.album.media.model.MediaType
import com.chac.feature.album.model.SaveUiStatus
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.model.MediaUiModel

/**
 * 클러스터 목록을 표시한다
 *
 * @param clusters 클러스터 UI 모델 목록
 * @param isLoading 로딩 중 여부
 * @param onClickSavePartial '사진 정리하기' 버튼 클릭 이벤트
 * @param onClickSaveAll '그대로 저장' 버튼 클릭 이벤트
 */
@Composable
fun ClusterList(
    clusters: List<MediaClusterUiModel>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onClickSavePartial: (MediaClusterUiModel) -> Unit,
    onClickSaveAll: (MediaClusterUiModel) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        items(items = clusters, key = { it.id }) { cluster ->
            ClusterCard(
                cluster = cluster,
                onClickSavePartial = { onClickSavePartial(cluster) },
                onClickSaveAll = { onClickSaveAll(cluster) },
            )
        }
        if (isLoading) {
            item {
                LoadingFooter()
            }
        }
    }
}

/**
 * 클러스터 정보를 카드로 표시한다
 *
 * @param cluster 표시할 클러스터 모델
 * @param onClickSavePartial '사진 정리하기' 버튼 클릭 이벤트
 * @param onClickSaveAll '그대로 저장'버튼 클릭 이벤트
 */
@Composable
private fun ClusterCard(
    cluster: MediaClusterUiModel,
    modifier: Modifier = Modifier,
    onClickSavePartial: () -> Unit,
    onClickSaveAll: () -> Unit,
) {
    val isDimmed = cluster.saveStatus != SaveUiStatus.Default
    val cardShape = RoundedCornerShape(12.dp)
    val badgeText = when (cluster.saveStatus) {
        SaveUiStatus.SaveCompleted -> stringResource(R.string.clustering_save_completed_badge)
        SaveUiStatus.Saving -> stringResource(R.string.clustering_saveing_badge)
        SaveUiStatus.Default -> null
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            shape = cardShape,
        ) {
            val defaultTitle = stringResource(R.string.clustering_default_album_title)
            val displayTitle = cluster.title.ifBlank { defaultTitle }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ClusterThumbnailStack(mediaList = cluster.mediaList)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.clustering_photo_count,
                            formatCount(cluster.mediaList.size),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onClickSavePartial,
                        modifier = Modifier.widthIn(min = 140.dp),
                        enabled = !isDimmed,
                    ) {
                        Text(text = stringResource(R.string.clustering_action_organize))
                    }
                    TextButton(
                        onClick = onClickSaveAll,
                        modifier = Modifier.align(Alignment.Start),
                        enabled = !isDimmed,
                    ) {
                        Text(text = stringResource(R.string.clustering_action_keep))
                    }
                }
            }
        }

        if (isDimmed) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f), cardShape),
            )
        }

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(6.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * 겹친 사진 더미 형태의 썸네일 플레이스홀더를 표시한다
 *
 * @param mediaList 이미지 리스트
 */
@Composable
private fun ClusterThumbnailStack(
    mediaList: List<MediaUiModel>,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val offsets = listOf(0.dp, 4.dp)

    Box(modifier = modifier) {
        offsets.forEachIndexed { index, offset ->
            val media = mediaList.getOrNull(index)

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .offset(x = offset, y = offset)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface)
                    .zIndex(offsets.lastIndex - index.toFloat()), // 이미지 중첩 렌더링 순서 보정
            ) {
                if (media != null) {
                    ChacImage(
                        model = media.uriString,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                    )

                    // dim
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.6f), shape),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClusterListPreview() {
    ChacTheme {
        val sampleMedia: (Int) -> List<MediaUiModel> = { count ->
            List(count) { index ->
                MediaUiModel(
                    id = index.toLong(),
                    uriString = "content://sample/$index",
                    dateTaken = 0L,
                    mediaType = MediaType.IMAGE,
                )
            }
        }
        val sampleClusters = listOf(
            MediaClusterUiModel(
                id = 1L,
                title = "Jeju Trip",
                mediaList = sampleMedia(34),
                saveStatus = SaveUiStatus.Default,
            ),
            MediaClusterUiModel(
                id = 2L,
                title = "서초동",
                mediaList = sampleMedia(34),
                saveStatus = SaveUiStatus.SaveCompleted,
            ),
        )
        ClusterList(
            clusters = sampleClusters,
            isLoading = true,
            onClickSavePartial = {},
            onClickSaveAll = {},
        )
    }
}
