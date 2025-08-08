package com.example.aplicativotcc.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.LoginActivity
import com.example.aplicativotcc.R
import com.example.aplicativotcc.RotinasActivity
import com.example.aplicativotcc.adapter.PlanAdapter
import com.example.aplicativotcc.model.PlanoDeTreino
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PlanosFragment : Fragment() {

    private lateinit var planosRecyclerView: RecyclerView
    private lateinit var planAdapter: PlanAdapter
    private lateinit var btnAddPlano: Button
    private lateinit var planosRef: DatabaseReference

    private val listaPlanos: MutableList<PlanoDeTreino> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_planos, container, false)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
            return view
        }

        planosRef = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(user.uid)
            .child("planos")

        planosRecyclerView = view.findViewById(R.id.plansRecyclerView)
        btnAddPlano = view.findViewById(R.id.BtnAddPlano)

        inicializarRecyclerView()
        btnAddPlano.setOnClickListener { mostrarDialogAdicionarPlano() }
        carregarPlanosDoFirebase()

        return view
    }

    private fun inicializarRecyclerView() {
        planosRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        planAdapter = PlanAdapter(
            listaPlanos,
            onItemClick = { plano ->
                val intent = Intent(requireContext(), RotinasActivity::class.java)
                intent.putExtra("PLANO_ID", plano.id)
                intent.putExtra("PLANO_NOME", plano.nome)
                startActivity(intent)
            },
            onDeleteClick = { plano ->
                mostrarDialogConfirmacaoExclusao(plano)
            }
        )
        planosRecyclerView.adapter = planAdapter
    }

    private fun mostrarDialogAdicionarPlano() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Criar Novo Plano de Treino")

        val input = EditText(requireContext())
        input.hint = "Nome do Plano"
        builder.setView(input)

        builder.setPositiveButton("Salvar") { dialog, _ ->
            val nomePlano = input.text.toString().trim()
            if (nomePlano.isNotEmpty()) {
                salvarPlanoNoFirebase(nomePlano)
                dialog.dismiss()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun salvarPlanoNoFirebase(nomePlano: String) {
        val novoPlanoRef = planosRef.push()
        val plano = mapOf("nome" to nomePlano)

        novoPlanoRef.setValue(plano)
            .addOnSuccessListener { println("Plano salvo com sucesso") }
            .addOnFailureListener { println("Erro ao salvar plano: ${it.message}") }
    }

    private fun carregarPlanosDoFirebase() {
        planosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaPlanos.clear()
                for (planoSnapshot in snapshot.children) {
                    val plano = planoSnapshot.getValue(PlanoDeTreino::class.java)
                    plano?.let {
                        listaPlanos.add(it.copy(id = planoSnapshot.key))
                    }
                }
                planAdapter.updatePlans(listaPlanos)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao carregar planos: ${error.message}")
            }
        })
    }

    private fun mostrarDialogConfirmacaoExclusao(plano: PlanoDeTreino) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Plano")
            .setMessage("Deseja excluir '${plano.nome}'?")
            .setPositiveButton("Sim") { dialog, _ ->
                plano.id?.let { planosRef.child(it).removeValue() }
                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
