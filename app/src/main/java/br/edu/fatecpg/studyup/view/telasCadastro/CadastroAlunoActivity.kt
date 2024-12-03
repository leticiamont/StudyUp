package br.edu.fatecpg.studyup.view.telasCadastro

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.fatecpg.studyup.R
import br.edu.fatecpg.studyup.dao.AlunoDao
import br.edu.fatecpg.studyup.model.Aluno
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroAlunoActivity : AppCompatActivity() {
    private lateinit var edtNomeAluno: EditText
    private lateinit var edtEmailAluno: EditText
    private lateinit var edtTelAluno: EditText
    private lateinit var edtSenhaAluno: EditText
    private lateinit var edtConfirmaAluno: EditText
    private lateinit var btnCadastrarAluno: Button
    private val alunoDAO = AlunoDao()

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_aluno)

        // Inicializando os componentes do layout
        edtNomeAluno = findViewById(R.id.edtNomeAluno)
        edtEmailAluno = findViewById(R.id.edtEmailAluno)
        edtTelAluno = findViewById(R.id.edtTelAluno)
        edtSenhaAluno = findViewById(R.id.edtSenhaAluno)
        edtConfirmaAluno = findViewById(R.id.edtConfirmaAluno)
        btnCadastrarAluno = findViewById(R.id.btnCadastrarAluno)

        // Configurando o botão de cadastro
        btnCadastrarAluno.setOnClickListener {
            cadastrarAluno()
        }
    }

    private fun cadastrarAluno() {
        val nome = edtNomeAluno.text.toString().trim()
        val email = edtEmailAluno.text.toString().trim()
        val telefone = edtTelAluno.text.toString().trim()
        val senha = edtSenhaAluno.text.toString().trim()
        val confirmaSenha = edtConfirmaAluno.text.toString().trim()

        // Validação dos campos
        if (nome.isEmpty() || email.isEmpty() || telefone.isEmpty() || senha.isEmpty() || confirmaSenha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        if (senha != confirmaSenha) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show()
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
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val aluno = Aluno(nome, email, telefone, userId)

                    alunoDAO.salvarAluno(aluno)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao salvar no Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Erro ao criar usuário: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
