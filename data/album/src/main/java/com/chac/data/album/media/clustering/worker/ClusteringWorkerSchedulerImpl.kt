package com.chac.data.album.media.clustering.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.chac.domain.album.media.ClusteringWorkScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
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

    companion object {
        private const val WORK_NAME = "clustering_work"
    }
}
