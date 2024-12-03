package br.edu.fatecpg.studyup.view.areaAluno

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.fatecpg.studyup.R
import br.edu.fatecpg.studyup.adapter.AulaAdapter
import br.edu.fatecpg.studyup.model.Aula
import br.edu.fatecpg.studyup.view.areaAluno.AreaAlunoActivity
import br.edu.fatecpg.studyup.view.areaAluno.MateriaisAlunoActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class AulasDispActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var aulaAdapter: AulaAdapter
    private val listaAulas = mutableListOf<Aula>() // Lista mutável para atualizar os dados dinamicamente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aulas_disp)

        // Configurar RecyclerView com um Adapter vazio inicialmente
        recyclerView = findViewById(R.id.rvLIstaAulas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        aulaAdapter = AulaAdapter(listaAulas) // Adapter inicial vazio
        recyclerView.adapter = aulaAdapter

        // Carregar aulas
        carregarAulas()

        // Configuração do menu
        val btnInicioAluno = findViewById<ImageButton>(R.id.btnInicioProf)
        btnInicioAluno.setOnClickListener {
            Log.d("Menu", "Navegando para a tela de início do aluno")
            val intent = Intent(this, AreaAlunoActivity::class.java)
            startActivity(intent)
        }

        val btnMaterialAluno = findViewById<ImageButton>(R.id.btnMaterial)
        btnMaterialAluno.setOnClickListener {
            Log.d("Menu", "Navegando para a tela de materiais do aluno")
            val intent = Intent(this, MateriaisAlunoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun atualizarRecyclerView(novasAulas: List<Aula>) {
        Log.d("RecyclerView", "Atualizando RecyclerView com ${novasAulas.size} aulas")
        listaAulas.clear() // Limpar a lista atual
        listaAulas.addAll(novasAulas) // Adicionar as novas aulas
        aulaAdapter.notifyDataSetChanged() // Notificar o Adapter para atualizar a exibição
    }

    private fun carregarAulas() {
        Log.d("Firestore", "Iniciando carregamento de aulas...")
        val db = FirebaseFirestore.getInstance()
        val listaTemporaria = mutableListOf<Aula>()

        db.collection("aulas")
            .get()
            .addOnSuccessListener { aulasDocuments ->
                if (aulasDocuments.isEmpty) {
                    Log.d("Firestore", "Nenhuma aula encontrada")
                }

                for (aulaDoc in aulasDocuments) {
                    val aula = aulaDoc.toObject(Aula::class.java)

                    // Formata a data e hora e armazena nas propriedades da aula
                    val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

                    val dataString = if (aula.data != null) {
                        formatoData.format(aula.data?.toDate())
                    } else {
                        ""
                    }

                    val horaString = if (aula.hora != null) {
                        formatoHora.format(aula.hora?.toDate())
                    } else {
                        ""
                    }

                    // Atribui as strings formatadas às propriedades da aula
                    aula.dataString = dataString
                    aula.horaString = horaString

                    listaTemporaria.add(aula)

                    // Atualiza o RecyclerView quando todas as aulas forem carregadas
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

