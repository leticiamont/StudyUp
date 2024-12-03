package br.edu.fatecpg.studyup.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.edu.fatecpg.studyup.R
import br.edu.fatecpg.studyup.view.areaAluno.AreaAlunoActivity
import br.edu.fatecpg.studyup.view.areaProfessor.AreaProfActivity
import br.edu.fatecpg.studyup.view.telasCadastro.CadastroAlunoActivity
import br.edu.fatecpg.studyup.view.telasCadastro.CadastroProfActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var edtEmail: EditText
    private lateinit var edtSenha: EditText
    private lateinit var btnEntrar: Button
    private lateinit var txvCriarConta: TextView

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edtEmail = findViewById(R.id.edtEmail)
        edtSenha = findViewById(R.id.edtSenha)
        btnEntrar = findViewById(R.id.btnEntrar)
        txvCriarConta = findViewById(R.id.txvCriarConta)

        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        btnEntrar.setOnClickListener {
            realizarLogin()
        }

        // Logica para abrir o pop up de cadastro
        val tvxCriarConta = findViewById<TextView>(R.id.txvCriarConta)
        tvxCriarConta.setOnClickListener {
            showPopupCadastro()
        }
    }

    //função para realizar o login

    private fun realizarLogin() {
        val email = edtEmail.text.toString().trim()
        val senha = edtSenha.text.toString().trim()

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    verificarTipoUsuario(userId)
                } else {
                    Toast.makeText(this, "Erro ao fazer login: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //função para verificar o tipo de usuario

    private fun verificarTipoUsuario(userId: String) {
        firestore.collection("alunos").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // É um aluno
                    val nome = document.getString("nome") ?: "Aluno"
                    val intent = Intent(this, AreaAlunoActivity::class.java)
                    intent.putExtra("NOME", nome) // Envia o nome para a próxima tela
                    startActivity(intent)
                    finish()
                } else {
                    // Verifica se é um professor
                    firestore.collection("professores").document(userId).get()
                        .addOnSuccessListener { profDocument ->
                            if (profDocument.exists()) {
                                // É um professor
                                val nome = profDocument.getString("nome") ?: "Professor"
                                val intent = Intent(this, AreaProfActivity::class.java)
                                intent.putExtra("NOME", nome) // Envia o nome para a próxima tela
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Tipo de usuário não encontrado.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Erro ao verificar tipo de usuário.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao verificar tipo de usuário.", Toast.LENGTH_SHORT).show()
            }
    }

    //função para abrir o pop up de cadastro

    private fun showPopupCadastro() {
        val layoutInflater = LayoutInflater.from(this)
        val view = layoutInflater.inflate(R.layout.popup_cadastro, null)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        val radioGroupOpcoes = view.findViewById<RadioGroup>(R.id.radioGroupOpcoes)
        val btnContinuar = view.findViewById<Button>(R.id.btnContinuar)

        btnContinuar.setOnClickListener {
            when (radioGroupOpcoes.checkedRadioButtonId) {
                R.id.radioProf -> {
                    // Lógica para Professor
                    val intent = Intent(this, CadastroProfActivity::class.java)
                    startActivity(intent)
                }
                R.id.radioAluno -> {
                    // Lógica para Aluno
                    val intent = Intent(this, CadastroAlunoActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    // Lógica para caso nenhuma opção seja selecionada
                    Toast.makeText(this, "Selecione uma opção", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        dialog.show()
    }
}