package com.example.aplicativotcc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.R
import com.example.aplicativotcc.model.Rotina

class RegistroAdapter(
    private var rotinas: List<Rotina>,
    private val onItemClick: (Rotina) -> Unit
) : RecyclerView.Adapter<RegistroAdapter.RotinaViewHolder>() {

    class RotinaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtViewNome: TextView = view.findViewById(R.id.txtViewNomeRotina)
        val txtViewDia: TextView = view.findViewById(R.id.txtViewDiaSemana)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RotinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rotina_registro, parent, false)
        return RotinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RotinaViewHolder, position: Int) {
        val rotina = rotinas[position]
        holder.txtViewNome.text = rotina.nome
        holder.txtViewDia.text = rotina.diaSemana
        holder.itemView.setOnClickListener { onItemClick(rotina) }
    }

    override fun getItemCount(): Int = rotinas.size

    fun updateRotinas(novasRotinas: List<Rotina>) {
        rotinas = novasRotinas
        notifyDataSetChanged()
    }
}