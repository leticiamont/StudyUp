package br.edu.fatecpg.studyup.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.edu.fatecpg.studyup.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mensagem = intent.getStringExtra("mensagem") ?: "Você tem uma aula em breve!"

        // Verifica se a permissão está disponível
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            val notificationManager = NotificationManagerCompat.from(context)
            val notification = NotificationCompat.Builder(context, "canal_notificacao")
                .setContentTitle("Lembrete de Aula")
                .setContentText(mensagem)
                .setSmallIcon(R.mipmap.ic_launcher) // Ícone da notificação
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            notificationManager.notify(0, notification)
        } else {
            Log.e("NotificationReceiver", "Notificações desativadas para este aplicativo")
        }
    }
}
