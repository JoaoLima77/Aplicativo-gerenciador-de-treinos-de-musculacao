package com.example.aplicativotcc.adapter

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.R
import com.example.aplicativotcc.model.Exercicio

class ExercicioAdapter(
    private var lista: List<Exercicio>,
    private val onDeleteClick: (Exercicio) -> Unit
) : RecyclerView.Adapter<ExercicioAdapter.ExercicioViewHolder>() {

    inner class ExercicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeText: TextView = itemView.findViewById(R.id.nomeExercicioText)
        val grupoText: TextView = itemView.findViewById(R.id.grupoMuscularText)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteExercicio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExercicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.listaexercicios, parent, false)
        return ExercicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExercicioViewHolder, position: Int) {
        val exercicio = lista[position]
        holder.nomeText.text = exercicio.nome
        holder.grupoText.text = exercicio.grupoMuscular
        holder.btnDelete.setOnClickListener { onDeleteClick(exercicio) }
    }

    override fun getItemCount() = lista.size

    fun updateLista(novaLista: List<Exercicio>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}
