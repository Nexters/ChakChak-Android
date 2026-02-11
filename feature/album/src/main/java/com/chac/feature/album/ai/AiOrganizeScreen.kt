package com.chac.feature.album.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chac.core.designsystem.ui.icon.Back
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.permission.MediaWithLocationPermissionUtil
import com.chac.core.permission.MediaWithLocationPermissionUtil.launchMediaWithLocationPermission
import com.chac.core.permission.compose.moveToPermissionSetting
import com.chac.core.permission.compose.rememberAwaitNotificationPermissionResult
import com.chac.core.permission.compose.rememberRegisterMediaWithLocationPermission
import com.chac.core.resources.R
import com.chac.feature.album.clustering.component.ClusterList
import com.chac.feature.album.clustering.model.ClusteringUiState

@Composable
fun AiOrganizeRoute(
    viewModel: AiOrganizeViewModel = hiltViewModel(),
    onClickCluster: (Long) -> Unit,
    onClickBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val promptText by viewModel.promptTextState.collectAsStateWithLifecycle()
    val hasRequested by viewModel.hasRequestedPromptState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mediaWithLocationPermission = rememberRegisterMediaWithLocationPermission(
        onGranted = { viewModel.onMediaWithLocationPermissionChanged(true) },
        onDenied = { viewModel.onMediaWithLocationPermissionChanged(false) },
        onPermanentlyDenied = { viewModel.onMediaWithLocationPermissionChanged(false) },
    )

    val awaitNotificationPermissionResult = rememberAwaitNotificationPermissionResult()

    LaunchedEffect(Unit) {
        awaitNotificationPermissionResult()

        val hasMediaWithLocationPermission = MediaWithLocationPermissionUtil.checkPermission(context)
        viewModel.onMediaWithLocationPermissionChanged(hasMediaWithLocationPermission)
        if (!hasMediaWithLocationPermission) {
            mediaWithLocationPermission.launchMediaWithLocationPermission()
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onMediaWithLocationPermissionChanged(MediaWithLocationPermissionUtil.checkPermission(context))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AiOrganizeScreen(
        uiState = uiState,
        promptText = promptText,
        hasRequested = hasRequested,
        onPromptChanged = viewModel::onPromptChanged,
        onPromptChipClicked = viewModel::onPromptChipClicked,
        onClickRun = viewModel::onRunPromptClustering,
        onClickCluster = onClickCluster,
        onClickBack = onClickBack,
    )
}

@Composable
private fun AiOrganizeScreen(
    uiState: ClusteringUiState,
    promptText: String,
    hasRequested: Boolean,
    onPromptChanged: (String) -> Unit,
    onPromptChipClicked: (String) -> Unit,
    onClickRun: () -> Unit,
    onClickCluster: (Long) -> Unit,
    onClickBack: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChacColors.Background)
            .padding(horizontal = 20.dp),
    ) {
        AiOrganizeTopBar(onClickBack = onClickBack)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.ai_organize_title),
            style = ChacTextStyles.SubTitle01,
            color = ChacColors.Text01,
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = promptText,
            onValueChange = onPromptChanged,
            modifier = Modifier.fillMaxWidth(),
            textStyle = ChacTextStyles.Body,
            placeholder = {
                Text(
                    text = stringResource(R.string.ai_organize_prompt_placeholder),
                    style = ChacTextStyles.Body,
                    color = ChacColors.Text03,
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ChacColors.BackgroundPopup,
                unfocusedContainerColor = ChacColors.BackgroundPopup,
                disabledContainerColor = ChacColors.BackgroundPopup,
                focusedIndicatorColor = ChacColors.Primary,
                unfocusedIndicatorColor = ChacColors.Stroke01,
            ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(PROMPT_CHIPS) { chip ->
                AssistChip(
                    onClick = { onPromptChipClicked(chip) },
                    label = {
                        Text(
                            text = chip,
                            style = ChacTextStyles.Caption,
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onClickRun,
            enabled = promptText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChacColors.Primary,
                contentColor = ChacColors.TextBtn01,
            ),
        ) {
            Text(
                text = stringResource(R.string.ai_organize_run),
                style = ChacTextStyles.Body,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            ClusteringUiState.PermissionChecking -> Unit

            ClusteringUiState.PermissionDenied -> {
                AiPermissionRequiredState { moveToPermissionSetting(context) }
            }

            is ClusteringUiState.WithClusters -> {
                Text(
                    text = stringResource(R.string.ai_organize_total_photo_count, uiState.totalPhotoCount),
                    style = ChacTextStyles.Caption,
                    color = ChacColors.Text03,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    when {
                        !hasRequested -> {
                            EmptyMessage(
                                message = stringResource(R.string.ai_organize_initial_message),
                            )
                        }

                        uiState.clusters.isEmpty() && uiState is ClusteringUiState.Loading -> {
                            EmptyMessage(
                                message = stringResource(R.string.ai_organize_loading_message),
                            )
                        }

                        uiState.clusters.isEmpty() -> {
                            EmptyMessage(
                                message = stringResource(R.string.ai_organize_empty_message),
                            )
                        }

                        else -> {
                            ClusterList(
                                clusters = uiState.clusters,
                                onClickCluster = onClickCluster,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiOrganizeTopBar(
    onClickBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(52.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onClickBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = ChacIcons.Back,
                contentDescription = stringResource(R.string.settings_back_cd),
                tint = ChacColors.Text01,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = stringResource(R.string.ai_organize_top_bar_title),
            style = ChacTextStyles.Title,
            color = ChacColors.Text01,
        )
    }
}

@Composable
private fun AiPermissionRequiredState(
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Text(
            text = stringResource(R.string.clustering_permission_message),
            style = ChacTextStyles.Body,
            color = ChacColors.Text03,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(
                containerColor = ChacColors.Primary,
                contentColor = ChacColors.TextBtn01,
            ),
        ) {
            Text(text = stringResource(R.string.clustering_permission_action))
        }
    }
}

@Composable
private fun EmptyMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = ChacTextStyles.Body,
            color = ChacColors.Text03,
            textAlign = TextAlign.Center,
        )
    }
}

private val PROMPT_CHIPS = listOf(
    "풍경 사진 정리해줘",
    "동물 사진만 정리해줘",
    "음식 사진 모아줘",
)
