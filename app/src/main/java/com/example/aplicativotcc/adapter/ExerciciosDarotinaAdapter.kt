package com.example.aplicativotcc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.R
import com.example.aplicativotcc.model.Exercicio

class ExerciciosDarotinaAdapter(
    private var lista: List<Exercicio>,
    private val onEdit: (Exercicio) -> Unit,
    private val onDelete: (Exercicio) -> Unit
) : RecyclerView.Adapter<ExerciciosDarotinaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercicios_da_rotina, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercicio = lista[position]
        holder.bind(exercicio)
        holder.btnEditar.setOnClickListener { onEdit(exercicio) }
        holder.btnExcluir.setOnClickListener { onDelete(exercicio) }
    }

    override fun getItemCount(): Int = lista.size

    fun updateLista(novaLista: List<Exercicio>) {
        lista = novaLista
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nome: TextView = itemView.findViewById(R.id.txtNomeExercicio)
        private val grupo: TextView = itemView.findViewById(R.id.txtGrupoMuscular)
        private val detalhes: TextView = itemView.findViewById(R.id.txtDetalhes)
        val btnExcluir: ImageButton = itemView.findViewById(R.id.btnDeletarexerciciorotina)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btneditarexerciciorotina)

        fun bind(exercicio: Exercicio) {
            nome.text = exercicio.nome
            grupo.text = exercicio.grupoMuscular

            val info = listOfNotNull(
                exercicio.series?.takeIf { it.isNotBlank() }?.let { "Séries: $it" },
                exercicio.repeticoes?.takeIf { it.isNotBlank() }?.let { "Reps: $it" },
                exercicio.peso?.takeIf { it.isNotBlank() }?.let { "Peso: $it kg" }
            )

            detalhes.text = if (info.isNotEmpty()) info.joinToString(" • ") else "Sem detalhes"
        }
    }
}
