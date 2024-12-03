package br.edu.fatecpg.studyup.model

import com.google.firebase.Timestamp

data class Aula(
    var aulaId: String? = null, // ID da aula
    var nome: String = "", // Nome da aula
    var data: Timestamp? = null, // Data da aula
    var hora: Timestamp? = null, // Hora da aula
    var linkMaterial: String? = null, // Link para material
    var linkVideo: String? = null, // Link para vídeo
    var professorId: String = "", // ID do professor
    var professorNome: String = "", // Nome do professor

    // Propriedades adicionais para armazenar as strings formatadas de data e hora
    var dataString: String = "",
    var horaString: String = ""
)