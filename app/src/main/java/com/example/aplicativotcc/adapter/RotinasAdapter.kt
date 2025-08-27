package com.example.aplicativotcc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.R
import com.example.aplicativotcc.model.Rotina

class RotinasAdapter(
    private var lista: List<Rotina>,
    private val onDeleteClick: (Rotina) -> Unit,
    private val onItemClick: (Rotina) -> Unit,
    private val onEditClick: (Rotina) -> Unit
) : RecyclerView.Adapter<RotinasAdapter.RotinaViewHolder>() {

    inner class RotinaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeText: TextView = itemView.findViewById(R.id.txtViewNomeRotina)
        val btnDelete: ImageButton = itemView.findViewById(R.id.imgBtnDeletaRotina)
        val btnEdit: ImageButton = itemView.findViewById(R.id.imgBtnEditRotina)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RotinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rotinas, parent, false)
        return RotinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RotinaViewHolder, position: Int) {
        val rotina = lista[position]
        holder.nomeText.text = rotina.nome

        holder.btnDelete.setOnClickListener {
            onDeleteClick(rotina)
        }

        holder.itemView.setOnClickListener {
            onItemClick(rotina)
        }
        holder.btnEdit.setOnClickListener {
            onEditClick(rotina)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun updateRotinas(novaLista: List<Rotina>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}
