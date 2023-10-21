package me.dio.copa.catar.notification.scheduler.extensions

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import me.dio.copa.catar.domain.model.MatchDomain
import java.time.Duration
import java.time.LocalDateTime

private val NOTIFICATION_TITLE_KEY = "NOTIFICATION_TITLE_KEY"
private val NOTIFICATION_CONTENT_KEY = "NOTIFICATION_CONTENT_KEY"
class NotificationMatchesWorker(private val context: Context, workerParameters: WorkerParameters)
    : Worker(context, workerParameters) {
    override fun doWork(): Result {
        val title = inputData.getString(NOTIFICATION_TITLE_KEY) ?: throw IllegalAccessError("Title is requered")
        val content = inputData.getString(NOTIFICATION_CONTENT_KEY) ?: throw IllegalAccessError("Content is requered")

        context.showNotification(title, content)
        return Result.success()
    }

    companion object{
        fun start(context: Context, match: MatchDomain){
            val (id,_,_,team01,team02,matchDate) = match
            val initialDelay = Duration.between(LocalDateTime.now(), matchDate)
            val inputData = workDataOf(
                NOTIFICATION_TITLE_KEY to "Falta 5 minutos para rolar a bola!!!",
                NOTIFICATION_CONTENT_KEY to "Hoje tem ${team01.flag} x ${team02.flag}, ao vivo no SBT."
            )

            WorkManager.getInstance(context).enqueueUniqueWork(
                id, ExistingWorkPolicy.KEEP, createRequest(initialDelay, inputData)
            )
        }

        fun cancel(context: Context, match: MatchDomain){
            WorkManager.getInstance(context).cancelUniqueWork(match.id)
        }

        private fun createRequest(initialDelay:Duration, inputData: Data): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<NotificationMatchesWorker>()
                .setInitialDelay(initialDelay).setInputData(inputData).build()
    }
}