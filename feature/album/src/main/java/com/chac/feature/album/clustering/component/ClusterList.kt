package com.chac.feature.album.clustering.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.chac.core.designsystem.ui.component.ChacImage
import com.chac.core.designsystem.ui.icon.ArrowTopRight
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.domain.album.media.model.MediaType
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.model.MediaUiModel

/**
 * 클러스터 목록을 표시한다
 *
 * @param clusters 클러스터 UI 모델 목록
 * @param clusterCardBackgroundColor 각 클러스터 카드 배경색을 반환하는 함수.
 * 리스트 외부에서 배경색 정책을 주입할 수 있도록 노출해, 화면/상태별로 규칙을 변경할 때 사용한다.
 * @param onClickCluster 클러스터 카드 클릭 이벤트 콜백
 */
@Composable
fun ClusterList(
    clusters: List<MediaClusterUiModel>,
    modifier: Modifier = Modifier,
    clusterCardBackgroundColor: (MediaClusterUiModel, Int) -> Color = { _, index ->
        val colors = listOf(
            ChacColors.Primary,
            ChacColors.PointColor01,
            ChacColors.PointColor02,
        )
        colors[index % colors.size]
    },
    onClickCluster: (MediaClusterUiModel) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        itemsIndexed(items = clusters, key = { _, item -> item.id }) { index, cluster ->
            ClusterCard(
                cluster = cluster,
                backgroundColor = clusterCardBackgroundColor(cluster, index),
                onClick = { onClickCluster(cluster) },
            )
        }
    }
}

/**
 * 클러스터 정보를 카드로 표시한다
 *
 * @param cluster 표시할 클러스터 모델
 * @param backgroundColor 카드 배경색
 * @param onClick 카드 클릭 이벤트 콜백
 */
@Composable
private fun ClusterCard(
    cluster: MediaClusterUiModel,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
            shape = cardShape,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(all = 14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                ClusterThumbnailStack(
                    thumbnailUriStrings = cluster.thumbnailUriStrings,
                    mediaCount = cluster.mediaList.size,
                )
                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    Text(
                        text = cluster.address,
                        style = ChacTextStyles.Headline02,
                        color = ChacColors.Text01,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = cluster.formattedDate,
                        style = ChacTextStyles.DateText,
                        color = ChacColors.Ffffff80,
                    )
                }

                Icon(
                    imageVector = ChacIcons.ArrowTopRight,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .size(16.dp),
                    tint = Color.White,
                )
            }
        }
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
                address = "Jeju Trip",
                formattedDate = "2024.01.15",
                mediaList = sampleMedia(128),
                thumbnailUriStrings = listOf(
                    "content://sample/0",
                    "content://sample/1",
                ),
            ),
            MediaClusterUiModel(
                id = 2L,
                address = "강남구",
                formattedDate = "2024.02.20",
                mediaList = sampleMedia(77),
                thumbnailUriStrings = listOf(
                    "content://sample/0",
                    "content://sample/1",
                ),
            ),
            MediaClusterUiModel(
                id = 3L,
                address = "정말로 동네명이 너무너무 긴 경우에는 어떻게 나올까 궁금하다 궁금해 너무나도 궁금해서 미쳐버리겠다.",
                formattedDate = "2024.03.10",
                mediaList = sampleMedia(34),
                thumbnailUriStrings = listOf(
                    "content://sample/0",
                    "content://sample/1",
                ),
            ),
        )

        ClusterList(
            clusters = sampleClusters,
            onClickCluster = {},
        )
    }
}
