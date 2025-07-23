package com.example.aplicativotcc.adapter

import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.R
import com.example.aplicativotcc.model.PlanoDeTreino

class PlanAdapter(
    private var planos: List<PlanoDeTreino>,
    private val onItemClick: (PlanoDeTreino) -> Unit,
    private val onDeleteClick: (PlanoDeTreino) -> Unit
) : RecyclerView.Adapter<PlanAdapter.PlanViewHolder>() {

    class PlanViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val txtViewNomePlano: TextView = itemView.findViewById(R.id.txtViewNomePlano)
        val imgBtnDeletaPlano: ImageButton = itemView.findViewById(R.id.imgBtnDeletaPlano)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): PlanViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.listaplanos, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plano = planos[position]
        holder.txtViewNomePlano.text = plano.nome

        holder.itemView.setOnClickListener {
            onItemClick(plano)
        }

        holder.imgBtnDeletaPlano.setOnClickListener {
            if (plano.id != null) {
                onDeleteClick(plano)
            }
        }
    }

    override fun getItemCount(): Int = planos.size

    fun updatePlans(newPlans: List<PlanoDeTreino>) {
        planos = newPlans
        notifyDataSetChanged()
    }
}