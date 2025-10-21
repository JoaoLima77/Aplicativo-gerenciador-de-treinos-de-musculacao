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
    private val onEditClick: ((Exercicio) -> Unit)? = null,
    private val onDeleteClick: ((Exercicio) -> Unit)? = null,
    private val onItemClick: ((Exercicio) -> Unit)? = null,
    private val modoSelecao: Boolean = false
) : RecyclerView.Adapter<ExerciciosDarotinaAdapter.ExercicioRotinaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExercicioRotinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercicios_da_rotina, parent, false)
        return ExercicioRotinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExercicioRotinaViewHolder, position: Int) {
        val exercicio = lista[position]
        holder.bind(exercicio, modoSelecao)

        if (modoSelecao) {
            holder.itemView.setOnClickListener { onItemClick?.invoke(exercicio) }
            holder.imgBtnEdita.visibility = View.GONE
            holder.imgBtnDeleta.visibility = View.GONE
        } else {
            holder.imgBtnEdita.setOnClickListener { onEditClick?.invoke(exercicio) }
            holder.imgBtnDeleta.setOnClickListener { onDeleteClick?.invoke(exercicio) }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun updateLista(novaLista: List<Exercicio>) {
        lista = novaLista
        notifyDataSetChanged()
    }

    class ExercicioRotinaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nome: TextView = itemView.findViewById(R.id.txtNomeExercicio)
        private val grupo: TextView = itemView.findViewById(R.id.txtGrupoMuscular)
        private val detalhes: TextView = itemView.findViewById(R.id.txtDetalhes)
        val imgBtnDeleta: ImageButton = itemView.findViewById(R.id.btnDeletarexerciciorotina)
        val imgBtnEdita: ImageButton = itemView.findViewById(R.id.btneditarexerciciorotina)

        fun bind(exercicio: Exercicio, modoSelecao: Boolean) {
            nome.text = exercicio.nome
            grupo.text = exercicio.grupoMuscular

            if (modoSelecao) {
                detalhes.text = itemView.context.getString(R.string.toque_para_adicionar)
            } else {
                val info = listOfNotNull(
                    exercicio.series?.takeIf { it.isNotBlank() }?.let { "Séries: $it" },
                    exercicio.repeticoes?.takeIf { it.isNotBlank() }?.let { "Reps: $it" },
                    exercicio.peso?.takeIf { it.isNotBlank() }?.let { "Peso: $it kg" }
                )
                detalhes.text = if (info.isNotEmpty()) info.joinToString(" • ") else "Sem detalhes"
            }
        }
    }
}
