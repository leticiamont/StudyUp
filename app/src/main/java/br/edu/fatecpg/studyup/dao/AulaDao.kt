package br.edu.fatecpg.studyup.dao

import br.edu.fatecpg.studyup.model.Aula
import com.google.firebase.firestore.FirebaseFirestore

class AulaDao {
    private val db = FirebaseFirestore.getInstance()
    private val aulasCollection = db.collection("aulas")

    // Adicionar aula
    fun adicionarAula(aula: Aula, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = aulasCollection.document() // Gera um ID automaticamente
        aula.aulaId = docRef.id // Define o ID na classe Aula

        // Verifica se a data e hora não são nulas e converte para Timestamp se necessário
        if (aula.data != null && aula.hora != null) {
            // Garante que a aula tenha data e hora em Timestamp
            docRef.set(aula)
                .addOnSuccessListener {
                    onSuccess(docRef.id)  // Retorna o ID gerado para uso posterior
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(Exception("A data ou hora não foram definidas"))
        }
    }

    // Buscar aulas de um professor específico
    fun buscarNomeProfessor(userId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("professores").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val professorNome = document.getString("nome") ?: "Professor"
                    onSuccess(professorNome)
                } else {
                    onFailure(Exception("Professor não encontrado"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Excluir aula
    fun excluirAula(aulaId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        aulasCollection.document(aulaId).delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Atualizar aula
    fun atualizarAula(aula: Aula, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (aula.aulaId != null) {
            // Verifica se aulaId é não nulo e atualiza a aula
            aulasCollection.document(aula.aulaId!!).set(aula)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(Exception("ID da aula é nulo"))
        }
    }
}


