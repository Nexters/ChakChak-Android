package com.chac.feature.album.clustering.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.component.ChacImage
import com.chac.core.designsystem.ui.icon.ArrowTopRight
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.modifier.verticalScrollFadingEdge
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.domain.album.media.model.MediaType
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.model.MediaUiModel

private const val CLUSTER_CARD_ENTER_FADE_MS = 320

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
    contentPadding: PaddingValues = PaddingValues(top = 12.dp, bottom = 40.dp),
    fadingEdgeTop: Dp = 12.dp,
    fadingEdgeBottom: Dp = 12.dp,
    clusterCardBackgroundColor: (MediaClusterUiModel, Int) -> Color = { _, index ->
        val colors = listOf(
            ChacColors.Primary,
            ChacColors.PointColor01,
            ChacColors.PointColor02,
        )
        colors[index % colors.size]
    },
    onClickCluster: (Long) -> Unit,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .verticalScrollFadingEdge(
                state = listState,
                top = fadingEdgeTop,
                bottom = fadingEdgeBottom,
            ),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        // 규칙: 페이딩 엣지(top/bottom) 높이만큼 contentPadding(top/bottom)을 확보해야 자연스럽다.
        contentPadding = contentPadding,
    ) {
        itemsIndexed(items = clusters, key = { _, item -> item.id }) { index, cluster ->
            AnimatedClusterCard(
                index = index,
                cluster = cluster,
                backgroundColor = clusterCardBackgroundColor(cluster, index),
                onClick = { onClickCluster(cluster.id) },
            )
        }
    }
}

/**
 * 클러스터 카드를 등장 애니메이션과 함께 표시한다.
 *
 * 카드 ID를 키로 가시성 상태를 저장해 같은 아이템이 Recomposition 될때에는 애니메이션이 반복 재생되지 않도록 한다.
 *
 * @param index 리스트 내 카드 인덱스(등장 지연 계산에 사용)
 * @param cluster 표시할 클러스터 UI 모델
 * @param backgroundColor 카드 배경색
 * @param onClick 카드 클릭 이벤트 콜백
 */
@Composable
private fun AnimatedClusterCard(
    index: Int,
    cluster: MediaClusterUiModel,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var isVisible by rememberSaveable(cluster.id) { mutableStateOf(false) }

    LaunchedEffect(cluster.id) {
        if (!isVisible) {
            isVisible = true
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = CLUSTER_CARD_ENTER_FADE_MS,
                easing = LinearOutSlowInEasing,
            ),
        ),
    ) {
        ClusterCard(
            cluster = cluster,
            backgroundColor = backgroundColor,
            onClick = onClick,
        )
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
                .clip(shape = cardShape)
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
            shape = cardShape,
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 14.dp, top = 14.dp, end = 16.dp, bottom = 14.dp)
                    .fillMaxWidth()
                    .height(72.dp),
                verticalAlignment = Alignment.Top,
            ) {
                ClusterThumbnail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                    thumbnailUriString = cluster.thumbnailUriStrings.firstOrNull(),
                    mediaCount = cluster.mediaList.size,
                )

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(space = 6.dp, alignment = Alignment.Top),
                    ) {
                        Text(
                            text = cluster.address,
                            style = ChacTextStyles.Headline02,
                            color = ChacColors.Text01,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = cluster.formattedDate,
                            style = ChacTextStyles.DateText,
                            color = ChacColors.Ffffff80,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = ChacIcons.ArrowTopRight,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .size(20.dp)
                            .padding(3.dp),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

/**
 * 대표 썸네일과 미디어 개수 배지를 표시한다.
 *
 * @param thumbnailUriString 썸네일 URI 문자열
 * @param mediaCount 미디어 개수
 */
@Composable
private fun ClusterThumbnail(
    thumbnailUriString: String?,
    mediaCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        if (thumbnailUriString != null) {
            ChacImage(
                model = thumbnailUriString,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .background(
                    color = ChacColors.Token00000070,
                    shape = CircleShape,
                )
                .padding(horizontal = 6.dp),
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
                formattedDate = "2024년 01월 15일",
                mediaList = sampleMedia(128),
                thumbnailUriStrings = listOf(
                    "content://sample/0",
                    "content://sample/1",
                ),
            ),
            MediaClusterUiModel(
                id = 2L,
                address = "강남구",
                formattedDate = "2024년 02월 20일",
                mediaList = sampleMedia(77),
                thumbnailUriStrings = listOf(
                    "content://sample/0",
                    "content://sample/1",
                ),
            ),
            MediaClusterUiModel(
                id = 3L,
                address = "정말로 동네명이 너무너무 긴 경우에는 어떻게 나올까 궁금하다 궁금해 너무나도 궁금해서 미쳐버리겠다.",
                formattedDate = "2024년 03월 10일",
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
