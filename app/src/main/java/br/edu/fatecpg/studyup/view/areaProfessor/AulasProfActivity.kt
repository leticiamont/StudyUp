package br.edu.fatecpg.studyup.view.areaProfessor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.fatecpg.studyup.R
import br.edu.fatecpg.studyup.adapter.AulaAdapter
import br.edu.fatecpg.studyup.adapter.AulaProfAdapter
import br.edu.fatecpg.studyup.model.Aula
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class AulasProfActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var aulaProfAdapter: AulaProfAdapter
    private val listaAulasProf = mutableListOf<Aula>() // Lista mutável para atualizar os dados dinamicamente
    private lateinit var professorId: String // Variável para armazenar o ID do professor logado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aulas_prof)

        // pega o ID do professor logado
        professorId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        recyclerView = findViewById(R.id.rvListaAulasProf)
        recyclerView.layoutManager = LinearLayoutManager(this)
        aulaProfAdapter = AulaProfAdapter(listaAulasProf, professorId) // Passa o professorId para o adapter
        recyclerView.adapter = aulaProfAdapter

        carregarAulasProf()

        // menu
        val btnInicioProf = findViewById<ImageButton>(R.id.btnInicioProf)
        val btnMaterial = findViewById<ImageButton>(R.id.btnMaterial)

        btnInicioProf.setOnClickListener {
            val intent = Intent(this, AreaProfActivity::class.java)
            startActivity(intent)
        }

        btnMaterial.setOnClickListener {
            val intent = Intent(this, AddAulaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun carregarAulasProf() {
        val professorId = FirebaseAuth.getInstance().currentUser?.uid
        if (professorId == null) {
            Log.e("AulasProfActivity", "Professor não autenticado")
            return
        }

        Log.d("Firestore", "Iniciando carregamento de aulas para o professor...")
        val db = FirebaseFirestore.getInstance()
        val listaTemporaria = mutableListOf<Aula>()

        db.collection("aulas")
            .whereEqualTo("professorId", professorId) // Filtra pelas aulas do professor logado
            .get()
            .addOnSuccessListener { aulasDocuments ->
                if (aulasDocuments.isEmpty) {
                    Log.d("Firestore", "Nenhuma aula encontrada para este professor")
                }

                for (aulaDoc in aulasDocuments) {
                    val aula = aulaDoc.toObject(Aula::class.java)
                    val dataString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(aula.data?.toDate())
                    val horaString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(aula.hora?.toDate())

                    aula.dataString = dataString // Adiciona a string de data formatada
                    aula.horaString = horaString // Adiciona a string de hora formatada

                    listaTemporaria.add(aula)

                    // Atualiza o RecyclerView quando todas as aulas forem carregadas
                    if (listaTemporaria.size == aulasDocuments.size()) {
                        atualizarRecyclerView(listaTemporaria)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erro ao carregar aulas para o professor: ${e.message}")
            }
    }


    private fun atualizarRecyclerView(novasAulas: List<Aula>) {
        listaAulasProf.clear()
        listaAulasProf.addAll(novasAulas)
        aulaProfAdapter.notifyDataSetChanged() // Notifica o adapter para atualizar a UI
    }
}
