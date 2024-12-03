package br.edu.fatecpg.studyup.view.telasCadastro

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.fatecpg.studyup.R
import br.edu.fatecpg.studyup.dao.ProfessorDao
import br.edu.fatecpg.studyup.model.Professor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroProfActivity : AppCompatActivity() {
    private lateinit var edtNomeProf: EditText
    private lateinit var edtEmailProf: EditText
    private lateinit var edtSenhaProf: EditText
    private lateinit var edtConfSenhaProf: EditText
    private lateinit var edtTelProf: EditText
    private lateinit var btnCadastrar: Button
    private val auth = FirebaseAuth.getInstance()
    private val professorDAO = ProfessorDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_prof)

        // Inicializar os campos da interface
        edtNomeProf = findViewById(R.id.edtNomeProf)
        edtEmailProf = findViewById(R.id.edtEmailProf)
        edtSenhaProf = findViewById(R.id.edtSenhaProf)
        edtConfSenhaProf = findViewById(R.id.edtConfSenhaProf)
        edtTelProf = findViewById(R.id.edtTelProf)
        btnCadastrar = findViewById(R.id.btnCadastrarAluno)

        // Configura o botão de cadastro
        btnCadastrar.setOnClickListener {
            cadastrarProfessor()
        }
    }

    private fun cadastrarProfessor() {
        val nome = edtNomeProf.text.toString().trim()
        val email = edtEmailProf.text.toString().trim()
        val senha = edtSenhaProf.text.toString().trim()
        val confSenha = edtConfSenhaProf.text.toString().trim()
        val telefone = edtTelProf.text.toString().trim()

        // Validação dos campos
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confSenha.isEmpty() || telefone.isEmpty()) {
            Toast.makeText(this, "Todos os campos devem ser preenchidos.", Toast.LENGTH_SHORT).show()
            return
        }
        if (senha != confSenha) {
            Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
            return
        }
        if (senha.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return
        }

        // Criar usuário no Firebase Authentication
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        val professor = Professor(nome, email, telefone, userId)
                        professorDAO.salvarProfessor(professor)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erro ao salvar os dados: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Erro ao cadastrar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}