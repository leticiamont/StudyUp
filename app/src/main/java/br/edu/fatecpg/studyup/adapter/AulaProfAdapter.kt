package br.edu.fatecpg.studyup.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.fatecpg.studyup.R
import br.edu.fatecpg.studyup.dao.AulaDao
import br.edu.fatecpg.studyup.model.Aula
import br.edu.fatecpg.studyup.view.TesteActivity
import br.edu.fatecpg.studyup.view.areaProfessor.EditarAulaActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class AulaProfAdapter(
    private val aulasProf: MutableList<Aula>, // Lista mutável para atualizar dinamicamente
    private val professorId: String // ID do professor logado
) : RecyclerView.Adapter<AulaProfAdapter.AulaProfViewHolder>() {

    class AulaProfViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nomeAula: TextView = view.findViewById(R.id.txvNomeAulaP)
        val dataAula: TextView = view.findViewById(R.id.txvDataAulaP)
        val horaAula: TextView = view.findViewById(R.id.txvHoraAulaP)
        val linkAula: TextView = view.findViewById(R.id.txvLinkAulaP)
        val linkVideo: TextView = view.findViewById(R.id.txvLinkVideoP)
        val btnExcluir: Button = view.findViewById(R.id.btnExcluir)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AulaProfViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aula_prof, parent, false)
        return AulaProfViewHolder(view)
    }

    override fun onBindViewHolder(holder: AulaProfViewHolder, position: Int) {
        val aulaProf = aulasProf[position]

        // Preencher os dados da aula
        holder.nomeAula.text = aulaProf.nome

        // Formatar data e hora para String
        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Verifica se aulaProf.data não é nulo e converte
        val dataString = aulaProf.data?.let { formatoData.format(it.toDate()) } ?: "" // Usando let para garantir que não é nulo
        holder.dataAula.text = dataString

        // Verifica se aulaProf.hora não é nulo e converte
        val horaString = aulaProf.hora?.let { formatoHora.format(it.toDate()) } ?: "" // Usando let para garantir que não é nulo
        holder.horaAula.text = horaString

        holder.linkAula.text = aulaProf.linkMaterial
        holder.linkVideo.text = aulaProf.linkVideo

        // Botão de excluir
        holder.btnExcluir.setOnClickListener {
            excluirAula(aulaProf, position)
        }

        // Botão de editar
        holder.btnEditar.setOnClickListener {
            editarAula(it.context, aulaProf)
        }
    }

    override fun getItemCount(): Int = aulasProf.size

    // Função para excluir uma aula
    private fun excluirAula(aula: Aula, position: Int) {
        val aulaDao = AulaDao()
        aulaDao.excluirAula(aula.aulaId ?: return, onSuccess = {
            // Remover o item da lista local
            aulasProf.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, aulasProf.size)
            Log.d("AulaProfAdapter", "Aula excluída com sucesso!")
        }, onFailure = { exception ->
            Log.e("AulaProfAdapter", "Erro ao excluir a aula: ", exception)
        })
    }

    // Função para editar uma aula
    private fun editarAula(context: Context, aula: Aula) {
        val intent = Intent(context, EditarAulaActivity::class.java)
        intent.putExtra("aulaId", aula.aulaId)
        intent.putExtra("nome", aula.nome)
        intent.putExtra("data", aula.data) // Data deve ser serializável
        intent.putExtra("hora", aula.hora) // Hora deve ser serializável
        intent.putExtra("linkMaterial", aula.linkMaterial)
        intent.putExtra("linkVideo", aula.linkVideo)
        context.startActivity(intent)
    }
}

