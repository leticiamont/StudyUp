package br.edu.fatecpg.studyup.view.areaAluno

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
import br.edu.fatecpg.studyup.adapter.MaterialAdapter
import br.edu.fatecpg.studyup.model.Aula
import com.google.firebase.firestore.FirebaseFirestore

class MateriaisAlunoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var materialAdapter: MaterialAdapter
    private val listaMateriais = mutableListOf<Aula>() // Lista para armazenar os materiais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materiais_aluno)

        // Configurar RecyclerView com Adapter vazio
        recyclerView = findViewById(R.id.rvListaMaterial)
        recyclerView.layoutManager = LinearLayoutManager(this)
        materialAdapter = MaterialAdapter(listaMateriais) // Adapter vazio inicialmente
        recyclerView.adapter = materialAdapter

        // Carregar os materiais das aulas
        carregarMateriais()

        // Configuração do menu
        val btnInicioAluno = findViewById<ImageButton>(R.id.btnInicioProf)
        btnInicioAluno.setOnClickListener {
            val intent = Intent(this, AreaAlunoActivity::class.java)
            startActivity(intent)
        }

        val btnAulasAluno = findViewById<ImageButton>(R.id.btnAulas)
        btnAulasAluno.setOnClickListener {
            val intent = Intent(this, AulasDispActivity::class.java)
            startActivity(intent)
        }
    }

    private fun atualizarRecyclerView(novosMateriais: List<Aula>) {
        listaMateriais.clear() // Limpar a lista
        listaMateriais.addAll(novosMateriais) // Adicionar novos materiais
        materialAdapter.notifyDataSetChanged() // Atualizar o Adapter
    }

    private fun carregarMateriais() {
        val db = FirebaseFirestore.getInstance()
        val listaTemporaria = mutableListOf<Aula>()

        // Carregar documentos de "aulas"
        db.collection("aulas")
            .get()
            .addOnSuccessListener { aulasDocuments ->
                if (aulasDocuments.isEmpty) {
                    Log.d("Firestore", "Nenhuma aula encontrada")
                }

                for (aulaDoc in aulasDocuments) {
                    val aula = aulaDoc.toObject(Aula::class.java)
                    Log.d("Firestore", "Aula carregada: ${aula.nome}, Link de material: ${aula.linkMaterial}, Link de vídeo: ${aula.linkVideo}")

                    // Adicionar à lista temporária
                    listaTemporaria.add(aula)

                    // Atualizar RecyclerView quando todos os dados forem carregados
                    if (listaTemporaria.size == aulasDocuments.size()) {
                        atualizarRecyclerView(listaTemporaria)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erro ao carregar aulas: ${e.message}")
            }
    }
}
