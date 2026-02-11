package com.chac.data.album.media.clustering.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.chac.domain.album.media.ClusteringWorkScheduler
import com.chac.domain.album.media.model.ClusteringWorkState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class ClusteringWorkerSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ClusteringWorkScheduler {
    override fun scheduleClustering() {
        val workerManager = WorkManager.getInstance(context)
        val workRequest = OneTimeWorkRequestBuilder<ClusteringWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(
                Data.Builder().build(),
            )
            .build()
        workerManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest,
        )
    }

    override fun observeClusteringWorkState(): Flow<ClusteringWorkState> = WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkFlow(WORK_NAME)
        .map { workInfos ->
            val target = workInfos.firstOrNull { !it.state.isFinished } ?: workInfos.lastOrNull()
            target?.state.toDomainModel()
        }
        .distinctUntilChanged()

    override fun cancelClustering() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        private const val WORK_NAME = "clustering_work"
    }
}

private fun WorkInfo.State?.toDomainModel(): ClusteringWorkState = when (this) {
    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> ClusteringWorkState.Enqueued
    WorkInfo.State.RUNNING -> ClusteringWorkState.Running
    WorkInfo.State.SUCCEEDED -> ClusteringWorkState.Succeeded
    WorkInfo.State.FAILED -> ClusteringWorkState.Failed
    WorkInfo.State.CANCELLED -> ClusteringWorkState.Cancelled
    null -> ClusteringWorkState.Idle
}
