package com.chac.feature.album.clustering.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.chac.core.designsystem.ui.icon.ArrowRight
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.icon.SaveAll
import com.chac.core.designsystem.ui.icon.SaveCompletedBadge
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R
import com.chac.domain.album.media.model.MediaType
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.model.MediaUiModel
import com.chac.feature.album.model.SaveUiStatus

/**
 * 클러스터 목록을 표시한다
 *
 * @param clusters 클러스터 UI 모델 목록
 * @param clusterCardBackgroundColor 각 클러스터 카드 배경색을 반환하는 함수.
 * 리스트 외부에서 배경색 정책을 주입할 수 있도록 노출해, 화면/상태별로 규칙을 변경할 때 사용한다.
 * @param onClickSavePartial '사진 정리하기' 버튼 클릭 이벤트 콜백
 * @param onClickSaveAll '그대로 저장' 버튼 클릭 이벤트 콜백
 */
@Composable
fun ClusterList(
    clusters: List<MediaClusterUiModel>,
    modifier: Modifier = Modifier,
    clusterCardBackgroundColor: (MediaClusterUiModel, Int) -> Color = { _, index ->
        val colors = listOf(
            ChacColors.Primary,
            ChacColors.Sub01,
            ChacColors.Sub02,
            ChacColors.Sun03,
        )
        colors[index % colors.size]
    },
    onClickSavePartial: (MediaClusterUiModel) -> Unit,
    onClickSaveAll: (MediaClusterUiModel) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        itemsIndexed(items = clusters, key = { _, item -> item.id }) { index, cluster ->
            ClusterCard(
                cluster = cluster,
                backgroundColor = clusterCardBackgroundColor(cluster, index),
                onClickSavePartial = { onClickSavePartial(cluster) },
                onClickSaveAll = { onClickSaveAll(cluster) },
            )
        }
    }
}

/**
 * 클러스터 정보를 카드로 표시한다
 *
 * @param cluster 표시할 클러스터 모델
 * @param backgroundColor 카드 배경색
 * @param onClickSavePartial '사진 정리하기' 버튼 클릭 이벤트 콜백
 * @param onClickSaveAll '그대로 저장' 버튼 클릭 이벤트 콜백
 */
@Composable
private fun ClusterCard(
    cluster: MediaClusterUiModel,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClickSavePartial: () -> Unit,
    onClickSaveAll: () -> Unit,
) {
    val isDimmed = cluster.saveStatus != SaveUiStatus.Default
    val saveStatusText = when (cluster.saveStatus) {
        SaveUiStatus.Default -> null
        SaveUiStatus.SaveCompleted -> stringResource(R.string.clustering_save_completed_badge)
        SaveUiStatus.Saving -> stringResource(R.string.clustering_saveing_badge)
    }
    val cardShape = RoundedCornerShape(16.dp)

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
            shape = cardShape,
        ) {
            val defaultTitle = stringResource(R.string.clustering_default_album_title)
            val displayTitle = cluster.title.ifBlank { defaultTitle }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ClusterThumbnailStack(
                    thumbnailUriStrings = cluster.thumbnailUriStrings,
                    mediaCount = cluster.mediaList.size,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = displayTitle,
                        style = ChacTextStyles.ContentTitle,
                        color = ChacColors.Text01,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    ) {
                        SaveAllCircleButton(
                            enabled = !isDimmed,
                            onClick = onClickSaveAll,
                        )

                        SavePartialPillButton(
                            enabled = !isDimmed,
                            onClick = onClickSavePartial,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        if (isDimmed) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(ChacColors.Token00000040, cardShape),
            )
        }

        if (saveStatusText != null) {
            SaveStatusdBadge(
                text = saveStatusText,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .zIndex(1f),
            )
        }
    }
}

@Composable
private fun SaveStatusdBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = ChacColors.Stroke01,
                shape = CircleShape,
            )
            .clip(CircleShape)
            .background(ChacColors.Background)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = ChacIcons.SaveCompletedBadge,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Unspecified,
        )

        Text(
            text = text,
            style = ChacTextStyles.Caption,
            color = ChacColors.Sub01,
        )
    }
}

/**
 * 겹친 사진 더미 형태의 썸네일 플레이스홀더를 표시한다
 *
 * @param thumbnailUriStrings 썸네일 URI 문자열 목록
 * @param mediaCount 미디어 개수
 */
@Composable
private fun ClusterThumbnailStack(
    thumbnailUriStrings: List<String>,
    mediaCount: Int,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val offsets = listOf(0.dp, 4.dp)

    Box(modifier = modifier) {
        offsets.forEachIndexed { index, offset ->
            val mediaUri = thumbnailUriStrings.getOrNull(index)

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .offset(x = offset, y = offset)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface)
                    .zIndex(offsets.lastIndex - index.toFloat()), // 이미지 중첩 렌더링 순서 보정
            ) {
                if (mediaUri != null) {
                    ChacImage(
                        model = mediaUri,
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .background(
                    color = ChacColors.Token00000070,
                    shape = CircleShape,
                )
                .padding(horizontal = 8.dp)
                .zIndex(offsets.size.toFloat()), // 이미지보다 위에 렌더링하기 위한 zIndex 설정
        ) {
            Text(
                text = "+${formatCount(mediaCount)}",
                style = ChacTextStyles.SubNumber,
                color = ChacColors.TextBtn01,
            )
        }
    }
}

/**
 * 클러스터의 모든 항목을 저장하는 원형 버튼을 표시한다.
 *
 * @param enabled 버튼 활성화 여부
 * @param onClick 버튼 클릭 이벤트 콜백
 */
@Composable
private fun SaveAllCircleButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = ChacColors.Ffffff40)
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .padding(all = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ChacIcons.SaveAll,
            contentDescription = null,
            tint = ChacColors.TextBtn01,
        )
    }
}

/**
 * 클러스터의 일부 항목을 정리해 저장하는 필 버튼을 표시한다.
 *
 * @param enabled 버튼 활성화 여부
 * @param onClick 버튼 클릭 이벤트 콜백
 */
@Composable
private fun SavePartialPillButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(ChacColors.Ffffff80)
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.clustering_action_organize),
            style = ChacTextStyles.SubBtn,
            color = ChacColors.TextBtn02,
        )

        Spacer(modifier = Modifier.size(8.dp))

        Icon(
            imageVector = ChacIcons.ArrowRight,
            contentDescription = null,
            tint = ChacColors.TextBtn02,
        )
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
                mediaList = sampleMedia(128),
                thumbnailUriStrings = listOf(
                    "content://sample/0",
                    "content://sample/1",
                ),
                saveStatus = SaveUiStatus.Default,
            ),
            MediaClusterUiModel(
                id = 2L,
                title = "강남구",
                mediaList = sampleMedia(77),
                thumbnailUriStrings = listOf(
                    "content://sample/0",
                    "content://sample/1",
                ),
                saveStatus = SaveUiStatus.Saving,
            ),
            MediaClusterUiModel(
                id = 3L,
                title = "서초동",
                mediaList = sampleMedia(34),
                thumbnailUriStrings = listOf(
                    "content://sample/0",
                    "content://sample/1",
                ),
                saveStatus = SaveUiStatus.SaveCompleted,
            ),
        )

        ClusterList(
            clusters = sampleClusters,
            onClickSavePartial = {},
            onClickSaveAll = {},
        )
    }
}
