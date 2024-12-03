package br.edu.fatecpg.studyup.dao

import br.edu.fatecpg.studyup.model.Professor
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

class ProfessorDao {
    private val firestore = FirebaseFirestore.getInstance()

    fun salvarProfessor(professor: Professor): Task<Void> {
        return firestore.collection("professores").document(professor.userId).set(professor)
    }
}