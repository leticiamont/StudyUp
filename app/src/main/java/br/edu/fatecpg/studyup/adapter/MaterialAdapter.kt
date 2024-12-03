package br.edu.fatecpg.studyup.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.fatecpg.studyup.R
import br.edu.fatecpg.studyup.model.Aula

class MaterialAdapter(private val materiais: List<Aula>) : RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder>() {

    class MaterialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nomeMaterial: TextView = view.findViewById(R.id.txvNomeAula)
        val linkMaterial: TextView = view.findViewById(R.id.txvLinkAula)
        val videoMaterial: WebView = view.findViewById(R.id.wvVideo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_materiais_aluno, parent, false)
        return MaterialViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        val material = materiais[position]

        // Exibir nome e link do material
        holder.nomeMaterial.text = material.nome
        holder.linkMaterial.text = material.linkMaterial ?: "Material indisponível"

        // Verificar se o link do vídeo é válido
        val linkVideo = material.linkVideo // Não estamos fazendo cast direto aqui
        if (!linkVideo.isNullOrEmpty() && linkVideo.contains("youtube.com")) {
            holder.videoMaterial.visibility = View.VISIBLE
            holder.videoMaterial.settings.javaScriptEnabled = true
            holder.videoMaterial.settings.domStorageEnabled = true // Habilitar armazenamento DOM

            // Extrair o ID do vídeo e configurar o embed
            val videoId = linkVideo.substringAfter("v=", "").substringBefore("&")
            if (videoId.isNotEmpty()) {
                val embedLink = "https://www.youtube.com/embed/$videoId"
                holder.videoMaterial.loadDataWithBaseURL(
                    null,
                    "<iframe width=\"100%\" height=\"100%\" src=\"$embedLink\" frameborder=\"0\" allowfullscreen></iframe>",
                    "text/html",
                    "utf-8",
                    null
                )
            } else {
                holder.videoMaterial.visibility = View.GONE
            }
        } else {
            holder.videoMaterial.visibility = View.GONE // Esconde se não houver vídeo ou link inválido
        }
    }

    override fun getItemCount() = materiais.size
}
