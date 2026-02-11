package com.chac.data.album.media.clustering.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.chac.core.resources.R.string
import com.chac.domain.album.media.usecase.GetClusteredMediaStreamUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class ClusteringWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val getClusteredMediaStreamUseCase: GetClusteredMediaStreamUseCase,
) : CoroutineWorker(context, workerParams) {
    private val notificationManager by lazy {
        context.getSystemService(NotificationManager::class.java)
    }

    override suspend fun doWork(): Result {
        initNotification()
        setForeground(getForegroundInfo())

        return startClustering()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = ForegroundInfo(
        getNotificationId(),
        createProgressNotificationBuilder().build(),
        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
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
        .setSmallIcon(getNotificationIconRes())
        .setOnlyAlertOnce(true)

    private fun createProgressNotificationBuilder(): NotificationCompat.Builder = createNotificationBuilder()
        .setAutoCancel(false)
        .setContentTitle(context.getString(string.clustering_worker_progress_title))
        .setContentText(context.getString(string.clustering_worker_progress_message))
        .setTicker(context.getString(string.clustering_worker_progress_title))
        .setOngoing(true)

    private fun showFailUploadNotification() {
        val notification = createNotificationBuilder()
            .setAutoCancel(false)
            .setContentTitle(context.getString(string.clustering_worker_fail_title))
            .setContentText(context.getString(string.clustering_worker_fail_message))
            .setTicker(context.getString(string.clustering_worker_fail_title))
            .build()
        notificationManager.cancel(getNotificationId())
        notificationManager.notify(getNotificationId(), notification)
    }

    private suspend fun startClustering(): Result = withContext(Dispatchers.IO) {
        notificationManager.notify(getNotificationId(), createProgressNotificationBuilder().build())
        return@withContext try {
            getClusteredMediaStreamUseCase().collect {}
            notificationManager.cancel(getNotificationId())
            Result.success()
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            handleError(throwable)
            Result.failure()
        }
    }

    private fun handleError(throwable: Throwable) {
        Timber.e(throwable, "ClusteringWorker handleError")
        showFailUploadNotification()
    }

    private fun getNotificationId(): Int = 1071724654

    private fun getNotificationIconRes(): Int {
        val appIconRes = context.applicationInfo.icon
        return if (appIconRes != 0) appIconRes else android.R.mipmap.sym_def_app_icon
    }

    companion object {
        private const val CHANNEL_ID = "channel_id::clustering_worker"
        private const val CHANNEL_NAME = "ClusteringWorker"
    }
}
