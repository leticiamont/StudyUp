package br.edu.fatecpg.studyup.view.areaAluno

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.edu.fatecpg.studyup.R
import br.edu.fatecpg.studyup.model.Agendamento
import br.edu.fatecpg.studyup.view.LoginActivity
import br.edu.fatecpg.studyup.view.MainActivity
import br.edu.fatecpg.studyup.view.areaAluno.MateriaisAlunoActivity
import br.edu.fatecpg.studyup.view.areaAluno.AulasDispActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale

class AreaAlunoActivity : AppCompatActivity() {
    private lateinit var txvProximaAula: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_aluno)

        val txvBoasVindas = findViewById<TextView>(R.id.txvBoasVindas)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        db.collection("alunos").document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                val nome = document.getString("nome") ?: "Aluno"
                txvBoasVindas.text = "Bem Vindo, $nome!"
            }


        //logica dos menu

        val btnInicioAluno = findViewById<ImageButton>(R.id.btnInicioProf)
        btnInicioAluno.setOnClickListener {
            // Lógica para ir para a tela de início
            val intent = Intent(this, AreaAlunoActivity::class.java)
            startActivity(intent)
        }

        val btnAulasAluno = findViewById<ImageButton>(R.id.btnAulas)
        btnAulasAluno.setOnClickListener {
            // Lógica para ir para a tela de aulas
            val intent = Intent(this, AulasDispActivity::class.java)
            startActivity(intent)
        }

        val btnMaterialAluno = findViewById<ImageButton>(R.id.btnMaterial)
        btnMaterialAluno.setOnClickListener {
            // Lógica para ir para a tela de materiais
            val intent = Intent(this, MateriaisAlunoActivity::class.java)
            startActivity(intent)
        }

        val btnSairAluno = findViewById<ImageButton>(R.id.btnSairProf)
        btnSairAluno.setOnClickListener {
            // Lógica para sair do aplicativo
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
}