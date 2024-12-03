package br.edu.fatecpg.studyup.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import br.edu.fatecpg.studyup.R

class NotificacaoWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Criação do canal de notificação
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "aula_notification",
                "Lembrete de Aula",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações para lembrete de aulas"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 200, 300)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "aula_notification")
            .setContentTitle("Lembrete de Aula")
            .setContentText("Sua aula começará em 1 hora.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setVibrate(longArrayOf(0, 100, 200, 300))
            .build()

        notificationManager.notify(1, notification)

        return Result.success()
    }
}