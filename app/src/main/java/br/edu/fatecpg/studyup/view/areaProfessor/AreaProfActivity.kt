package br.edu.fatecpg.studyup.view.areaProfessor

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.edu.fatecpg.studyup.R
import br.edu.fatecpg.studyup.model.Aula
import br.edu.fatecpg.studyup.view.LoginActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale

class AreaProfActivity : AppCompatActivity() {
    private lateinit var txvProximaAula: TextView
    private val db = FirebaseFirestore.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_prof)

        val txvBoasVindas = findViewById<TextView>(R.id.txvBoasVindas)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        db.collection("professores").document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                val nome = document.getString("nome") ?: "Professor"
                txvBoasVindas.text = "Bem Vindo Profª $nome!"
            }

        //logica dos menu

        val btnSairProf = findViewById<ImageButton>(R.id.btnSairProf)
        val btnInicioProf = findViewById<ImageButton>(R.id.btnInicioProf)
        val btnAulas = findViewById<ImageButton>(R.id.btnAulas)
        val btnMaterial = findViewById<ImageButton>(R.id.btnMaterial)

        btnSairProf.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnInicioProf.setOnClickListener {
            val intent = Intent(this, AreaProfActivity::class.java)
            startActivity(intent)
        }

        btnAulas.setOnClickListener {
            val intent = Intent(this, AulasProfActivity::class.java)
            startActivity(intent)
        }

        btnMaterial.setOnClickListener {
            val intent = Intent(this, AddAulaActivity::class.java)
            startActivity(intent)
        }
    }
}