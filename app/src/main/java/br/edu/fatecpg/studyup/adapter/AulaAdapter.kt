package br.edu.fatecpg.studyup.adapter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import br.edu.fatecpg.studyup.R
import br.edu.fatecpg.studyup.model.Agendamento
import br.edu.fatecpg.studyup.model.Aula
import br.edu.fatecpg.studyup.notifications.NotificationReceiver
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AulaAdapter(private val aulas: List<Aula>) : RecyclerView.Adapter<AulaAdapter.AulaViewHolder>() {

    class AulaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nomeAula: TextView = view.findViewById(R.id.txvNomeAula)
        val dataAula: TextView = view.findViewById(R.id.txvDataAula)
        val horaAula: TextView = view.findViewById(R.id.txvHoraAula)
        val nomeProfessor: TextView = view.findViewById(R.id.txvNomeProfessor)
        val btnAgendar: Button = view.findViewById(R.id.btnAgendar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AulaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aulas_alunos, parent, false)
        return AulaViewHolder(view)
    }

    val db = FirebaseFirestore.getInstance()

    override fun onBindViewHolder(holder: AulaViewHolder, position: Int) {
        val aula = aulas[position]

        // Preenche o nome da aula e o nome do professor
        holder.nomeAula.text = aula.nome
        holder.nomeProfessor.text = aula.professorNome

        // Formatar data e hora para String
        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Verifica se aula.data não é nulo e faz a conversão
        val dataString = aula.data?.let { formatoData.format(it.toDate()) } ?: "" // Verifica a nulidade antes de formatar
        holder.dataAula.text = dataString

        // Verifica se aula.hora não é nulo e faz a conversão
        val horaString = aula.hora?.let { formatoHora.format(it.toDate()) } ?: "" // Verifica a nulidade antes de formatar
        holder.horaAula.text = horaString

        val alunoId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        holder.btnAgendar.setOnClickListener {
            if (alunoId.isNotEmpty()) {
                val firestore = FirebaseFirestore.getInstance()

                // Busca o nome do aluno no Firestore usando o alunoId
                firestore.collection("alunos").document(alunoId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val nomeAluno = document.getString("nome") ?: "Nome não encontrado"

                            // Cria o objeto Agendamento com o nome do aluno
                            val agendamento = Agendamento(
                                aulaNome = aula.nome,
                                professorId = aula.professorId,
                                professorNome = aula.professorNome,
                                alunoId = alunoId,
                                nomeAluno = nomeAluno,
                                dataAgendamento = aula.data,
                                horaAgendamento = aula.hora
                            )

                            // Gera um ID automaticamente para o agendamento
                            val agendamentoRef = firestore.collection("agendamentos").document() // Gera um ID automático
                            agendamento.agendamentoId = agendamentoRef.id // Define o ID na classe Agendamento

                            // Adiciona o agendamento à coleção "agendamentos"
                            agendamentoRef.set(agendamento)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Aula agendada com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Agendar a notificação para 1 hora antes da aula
                                    scheduleNotification(holder.itemView.context, aula.data, aula.hora)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Erro ao agendar: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Log.e("Firestore", "Aluno não encontrado!")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Erro ao buscar nome do aluno: ${e.message}")
                    }
            } else {
                Log.e("Auth", "Aluno não autenticado")
            }
        }
    }

    override fun getItemCount() = aulas.size

    // Agora o método de agendamento da notificação não usa mais o holder
    private fun scheduleNotification(context: Context, data: Timestamp?, hora: Timestamp?) {
        if (data != null && hora != null) {
            // Agendar notificação para 1h antes da aula
            val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

            val dataString = formatoData.format(data.toDate())
            val horaString = formatoHora.format(hora.toDate())

            val dataHoraString = "$dataString $horaString"

            try {
                val formatoDataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val dataHora = formatoDataHora.parse(dataHoraString)

                if (dataHora != null) {
                    val timestamp = Timestamp(dataHora)

                    // Calcular o horário para 1 hora antes
                    val calendar = Calendar.getInstance()
                    calendar.time = timestamp.toDate() // Converter Timestamp para Date
                    calendar.add(Calendar.HOUR, -1) // Subtrai 1 hora para a notificação

                    // Agendar a notificação usando AlarmManager
                    val notificationIntent = Intent(context, NotificationReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )

                    Toast.makeText(context, "Notificação agendada para 1 hora antes da aula.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ParseException) {
                Toast.makeText(context, "Erro ao formatar a data e hora.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


