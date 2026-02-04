package com.chac.data.album.media.clustering.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.chac.core.resources.R.string
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class ClusteringWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    private val notificationManager by lazy {
        context.getSystemService(NotificationManager::class.java)
    }

    override suspend fun doWork(): Result {
        initNotification()

        return startClustering()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = ForegroundInfo(
        getNotificationId(),
        createNotificationBuilder().build(),
    )

    private fun initNotification() {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotificationBuilder(): NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setColor(
            ContextCompat.getColor(
                context,
                android.R.color.darker_gray,
            ),
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSmallIcon(android.R.mipmap.sym_def_app_icon)
        .setOnlyAlertOnce(true)

    private fun createProgressNotificationBuilder(): NotificationCompat.Builder = createNotificationBuilder()
        .setAutoCancel(false)
        .setContentTitle(context.getString(string.clustering_worker_progress_title))
        .setOngoing(true)

    private fun showFailUploadNotification() {
        val notification = createNotificationBuilder()
            .setAutoCancel(false)
            .setContentTitle(context.getString(string.clustering_worker_fail_title))
            .build()
        notificationManager.cancel(getNotificationId())
        notificationManager.notify(getNotificationId(), notification)
    }

    private suspend fun startClustering(): Result = withContext(Dispatchers.IO) {
        notificationManager.notify(getNotificationId(), createProgressNotificationBuilder().build())
        runCatching {
            // TODO 여기에 클러스터링 업뎃해주는 코드가 들어가야함
            delay(5000)
        }.onFailure {
            handleError(it)
        }
        notificationManager.cancel(getNotificationId())
        return@withContext Result.success()
    }

    private fun handleError(throwable: Throwable) {
        Timber.e("ClusteringWorker handleError - ${throwable.message}")
        showFailUploadNotification()
    }

    private fun getNotificationId(): Int = 1071724654

    companion object {
        private const val CHANNEL_ID = "channel_id::clustering_worker"
        private const val CHANNEL_NAME = "ClusteringWorker"
    }
}
