package br.edu.fatecpg.studyup.view

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import br.edu.fatecpg.studyup.R
import br.edu.fatecpg.studyup.dao.AulaDao
import br.edu.fatecpg.studyup.model.Aula
import br.edu.fatecpg.studyup.notifications.NotificacaoWorker
import br.edu.fatecpg.studyup.notifications.NotificationReceiver
import br.edu.fatecpg.studyup.view.areaProfessor.AulasProfActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class TesteActivity : AppCompatActivity() {
    private lateinit var aulaDao: AulaDao
    private lateinit var aulaId: String
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teste)

        aulaDao = AulaDao()

        val data = findViewById<EditText>(R.id.editTextDate)
        val time = findViewById<EditText>(R.id.editTextTime)
        val button = findViewById<Button>(R.id.button)
        val editar = findViewById<Button>(R.id.button2)
        val name = findViewById<EditText>(R.id.editTextText)

        editar.setOnClickListener {
            Toast.makeText(this, "Botão está funcionando!", Toast.LENGTH_SHORT).show()

            val nome = name.text.toString().trim()
            val data = data.text.toString().trim()
            val hora = time.text.toString().trim()

            if (nome.isEmpty() || data.isEmpty() || hora.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dataFormatada = formatoData.parse(data)
            val horaFormatada = formatoHora.parse(hora)

            val aulaAtualizada = Aula(
                aulaId = aulaId,
                nome = nome,
                data = Timestamp(dataFormatada),
                hora = Timestamp(horaFormatada),
            )

            aulaDao.atualizarAula(aulaAtualizada, onSuccess = {
                Toast.makeText(this, "Aula atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AulasProfActivity::class.java)
                startActivity(intent)
                finish()
            }, onFailure = { e ->
                Toast.makeText(this, "Erro ao atualizar aula: ${e.message}", Toast.LENGTH_SHORT).show()
            })
        }

        button.setOnClickListener {
            // Verifica se a permissão para agendar alarmes exatos foi concedida
            if (!verificarPermissaoAgendarAlarme()) return@setOnClickListener

            val dataString = data.text.toString().trim()
            val timeString = time.text.toString().trim()

            if (dataString.isEmpty() || timeString.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha ambos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Continua com a lógica para salvar e agendar
            salvarAula(dataString, timeString)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "canal_notificacao",
                "Lembretes de Aula",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    private fun salvarAula(dataString: String, timeString: String) {
        val dataHoraString = "$dataString $timeString" // Junta data e hora
        try {
            val formatoDataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dataHora = formatoDataHora.parse(dataHoraString)

            if (dataHora != null) {
                val timestamp = Timestamp(dataHora)

                // Salva no Firestore
                val aula = hashMapOf(
                    "nome" to "Exemplo de Aula",
                    "dataHora" to timestamp
                )

                db.collection("aulas")
                    .add(aula)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Aula cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
                        scheduleNotificationWithWorkManager(timestamp)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao cadastrar aula: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Erro ao formatar a data e hora.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao formatar a data e hora.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun scheduleNotificationWithWorkManager(timestamp: Timestamp) {
        val delayInMillis = timestamp.toDate().time - System.currentTimeMillis() - 3600000 // 1h antes
        val data = Data.Builder()
            .putString("title", "Lembrete de Aula")
            .putString("message", "Sua aula começará em 1 hora.")
            .build()

        val notificationWork = OneTimeWorkRequestBuilder<NotificacaoWorker>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(this).enqueue(notificationWork)

        Toast.makeText(this, "Notificação agendada para 1 hora antes da aula.", Toast.LENGTH_SHORT).show()
    }

    private fun verificarPermissaoAgendarAlarme(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Permissão para agendar alarmes exatos necessária", Toast.LENGTH_SHORT).show()
                // Mostrar uma mensagem explicando ou redirecionar o usuário para as configurações
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return false
            }
        }
        return true
    }
}
