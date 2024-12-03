package br.edu.fatecpg.studyup.view.areaProfessor

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import br.edu.fatecpg.studyup.view.LoginActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class EditarAulaActivity : AppCompatActivity() {
    private lateinit var aulaDao: AulaDao
    private lateinit var aulaId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_aula)

        aulaDao = AulaDao()

        val edtEditNome = findViewById<EditText>(R.id.edtEditNome)
        val edtEditData = findViewById<EditText>(R.id.edtEditData)
        val edtEditHora = findViewById<EditText>(R.id.edtEditHora)
        val edtEditLink = findViewById<EditText>(R.id.edtEditLink)
        val edtEditVideo = findViewById<EditText>(R.id.edtEditVideo)

        aulaId = intent.getStringExtra("aulaId") ?: return
        edtEditNome.setText(intent.getStringExtra("nome"))
        val edtEditDataTimestamp = intent.getSerializableExtra("data") as? Timestamp
        val edtEditHoraTimestamp = intent.getSerializableExtra("hora") as? Timestamp

        edtEditLink.setText(intent.getStringExtra("linkMaterial"))
        edtEditVideo.setText(intent.getStringExtra("linkVideo"))

        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Converter Timestamp para Date e depois para String
        if (edtEditDataTimestamp != null && edtEditHoraTimestamp != null) {
            edtEditData.setText(formatoData.format(edtEditDataTimestamp.toDate())) // Formata data
            edtEditHora.setText(formatoHora.format(edtEditHoraTimestamp.toDate())) // Formata hora
        }

        val btnEditar = findViewById<Button>(R.id.btnEdit)

        btnEditar.setOnClickListener {
            Log.d("EditarAulaActivity", "Botão salvar clicado")

            val nome = edtEditNome.text.toString().trim()
            val data = edtEditData.text.toString().trim()
            val hora = edtEditHora.text.toString().trim()
            val linkMaterial = edtEditLink.text.toString().trim()
            val linkVideo = edtEditVideo.text.toString().trim()

            // Verificação de campos obrigatórios
            if (nome.isEmpty() || data.isEmpty() || hora.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tentativa de converter a data e hora
            val dataFormatada = try {
                formatoData.parse(data)
            } catch (e: ParseException) {
                Toast.makeText(this, "Formato de data inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val horaFormatada = try {
                formatoHora.parse(hora)
            } catch (e: ParseException) {
                Toast.makeText(this, "Formato de hora inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ID do professor
            val professorId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            // Buscar o nome do professor no Firestore
            aulaDao.buscarNomeProfessor(professorId, onSuccess = { professorNome ->
                // Criação do objeto Aula com dados fornecidos
                val aulaAtualizada = Aula(
                    aulaId = aulaId,
                    nome = nome,
                    data = Timestamp(dataFormatada),
                    hora = Timestamp(horaFormatada),
                    linkMaterial = if (linkMaterial.isNotEmpty()) linkMaterial else null,
                    linkVideo = if (linkVideo.isNotEmpty()) linkVideo else null,
                    professorId = professorId,
                    professorNome = professorNome // Nome correto do professor
                )

                // Atualização no banco de dados
                aulaDao.atualizarAula(aulaAtualizada, onSuccess = {
                    Toast.makeText(this, "Aula atualizada com sucesso!", Toast.LENGTH_SHORT).show()

                    // Atualizar notificação com WorkManager
                    scheduleNotificationWithWorkManager(Timestamp(dataFormatada))

                    val intent = Intent(this, AulasProfActivity::class.java)
                    startActivity(intent)
                    finish()
                }, onFailure = { exception ->
                    Toast.makeText(this, "Erro ao atualizar aula: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditarAulaActivity", "Erro ao atualizar aula", exception)
                })
            }, onFailure = { exception ->
                Toast.makeText(this, "Erro ao buscar nome do professor: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("EditarAulaActivity", "Erro ao buscar nome do professor", exception)
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


