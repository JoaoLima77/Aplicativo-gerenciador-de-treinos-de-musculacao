package com.example.aplicativotcc.adapter

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.R
import com.example.aplicativotcc.model.Exercicio

class ExerciciosAdapter(
    private var lista: List<Exercicio>,
    private val onDeleteClick: (Exercicio) -> Unit,
    private val onEditClick: (Exercicio) -> Unit
) : RecyclerView.Adapter<ExerciciosAdapter.ExercicioViewHolder>() {

    class ExercicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeText: TextView = itemView.findViewById(R.id.nomeExercicioText)
        val grupoText: TextView = itemView.findViewById(R.id.grupoMuscularText)
        val imgBtnDeleta: ImageButton = itemView.findViewById(R.id.btnDeletarexerciciorotina)
        val imgBtnEdita: ImageButton = itemView.findViewById(R.id.btneditarexerciciorotina)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExercicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercicios, parent, false)
        return ExercicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExercicioViewHolder, position: Int) {
        val exercicio = lista[position]
        holder.nomeText.text = exercicio.nome
        holder.grupoText.text = exercicio.grupoMuscular
        holder.imgBtnDeleta.setOnClickListener { onDeleteClick(exercicio) }
        holder.imgBtnEdita.setOnClickListener { onEditClick(exercicio) }
    }

    override fun getItemCount() = lista.size

    fun updateLista(novaLista: List<Exercicio>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}
