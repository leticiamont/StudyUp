package br.edu.fatecpg.studyup.model

import com.google.firebase.Timestamp

data class Agendamento(
    var agendamentoId: String? = null,
    var aulaNome: String = "",
    var professorId: String = "",
    var professorNome: String = "",
    var alunoId: String = "",
    var nomeAluno: String = "",
    var dataAgendamento: Timestamp? = null,  // Alterado para Timestamp
    var horaAgendamento: Timestamp? = null  // Alterado para Timestamp
)
