package br.edu.fatecpg.studyup.dao

import br.edu.fatecpg.studyup.model.Aluno
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

class AlunoDao {
    private val firestore = FirebaseFirestore.getInstance()

    fun salvarAluno(aluno: Aluno): Task<Void> {
        return firestore.collection("alunos")
            .document(aluno.userId)
            .set(aluno)
    }
}