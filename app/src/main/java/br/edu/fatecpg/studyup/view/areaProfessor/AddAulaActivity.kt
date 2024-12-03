package br.edu.fatecpg.studyup.view.areaProfessor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class AddAulaActivity : AppCompatActivity() {
    private val aulaDao = AulaDao()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_aula)

        //funcionalidade do menu
        val btnInicioProf = findViewById<ImageButton>(R.id.btnInicioProf)
        val btnAulas = findViewById<ImageButton>(R.id.btnAulas)

        btnInicioProf.setOnClickListener {
            val intent = Intent(this, AreaProfActivity::class.java)
            startActivity(intent)
        }

        btnAulas.setOnClickListener {
            val intent = Intent(this, AulasProfActivity::class.java)
            startActivity(intent)
        }

        // Lógica do botão adicionar
        val edtNomeAula = findViewById<EditText>(R.id.edtNomeAula)
        val edtDiaAula = findViewById<EditText>(R.id.edtDiaAula)
        val edtHoraAula = findViewById<EditText>(R.id.edtHoraAula)
        val edtLink = findViewById<EditText>(R.id.edtLink)
        val edtVideo = findViewById<EditText>(R.id.edtVideo)
        val btnAdicionar = findViewById<Button>(R.id.btnAdicionar)

        btnAdicionar.setOnClickListener {
            val nome = edtNomeAula.text.toString().trim()
            val data = edtDiaAula.text.toString().trim()
            val hora = edtHoraAula.text.toString().trim()
            val linkMaterial = edtLink.text.toString().trim()
            val linkVideo = edtVideo.text.toString().trim()

            // Validação dos campos
            if (nome.isEmpty() || data.isEmpty() || hora.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val professorId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            // Buscar o nome do professor no Firestore
            aulaDao.buscarNomeProfessor(professorId, onSuccess = { professorNome ->
                // Formatar data e hora para Timestamp
                val formatoDataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val dataHoraString = "$data $hora"
                try {
                    val dataHora = formatoDataHora.parse(dataHoraString)

                    if (dataHora != null) {
                        val timestamp = Timestamp(dataHora)

                        // Criar a aula
                        val aula = Aula(
                            nome = nome,
                            data = timestamp, // Usando Timestamp
                            hora = timestamp, // Usando Timestamp
                            linkMaterial = if (linkMaterial.isNotEmpty()) linkMaterial else null,
                            linkVideo = if (linkVideo.isNotEmpty()) linkVideo else null,
                            professorId = professorId,
                            professorNome = professorNome
                        )

                        // Salvar aula no Firestore
                        aulaDao.adicionarAula(aula, onSuccess = { aulaId ->
                                Toast.makeText(this, "Aula adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                                edtNomeAula.text.clear()
                                edtDiaAula.text.clear()
                                edtHoraAula.text.clear()
                                edtLink.text.clear()
                                edtVideo.text.clear()

                                // Agendar a notificação para 1 hora antes da aula
                                scheduleNotificationWithWorkManager(timestamp)
                        }, onFailure = { e ->
                            Toast.makeText(this, "Erro ao adicionar aula: ${e.message}", Toast.LENGTH_SHORT).show()
                        })
                    }
                } catch (e: ParseException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Erro ao formatar a data e hora.", Toast.LENGTH_SHORT).show()
                }
            }, onFailure = { e ->
                Toast.makeText(this, "Erro ao buscar nome do professor: ${e.message}", Toast.LENGTH_SHORT).show()
            })
        }
    }

    // Agendar a notificação usando WorkManager
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
}
