package com.example.aplicativotcc.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.R
import com.example.aplicativotcc.adapter.ExerciciosAdapter
import com.example.aplicativotcc.model.Exercicio
import com.example.aplicativotcc.ui.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExerciciosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExerciciosAdapter
    private lateinit var btnAddExercicio: Button
    private lateinit var exerciciosRef: DatabaseReference

    private val listaExercicios = mutableListOf<Exercicio>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercicios, container, false)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
            return view
        }

        exerciciosRef = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(user.uid)
            .child("exercicios")

        recyclerView = view.findViewById(R.id.exerciciosRecyclerView)
        btnAddExercicio = view.findViewById(R.id.btnAddExercicio)

        adapter = ExerciciosAdapter(
            listaExercicios,
            onDeleteClick = { exercicio -> excluirExercicio(exercicio) },
            onEditClick = { exercicio -> editarExercicio(exercicio) }
        )


        val edtTxtPesquisa = view.findViewById<EditText>(R.id.edtTxtPesquisa)

        edtTxtPesquisa.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val filtro = s.toString().lowercase()
                val listaFiltrada = listaExercicios.filter {
                    it.nome?.lowercase()?.contains(filtro) == true
                }
                adapter.updateLista(listaFiltrada)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnAddExercicio.setOnClickListener { adicionarExercicio() }

        carregarExercicios()

        return view
    }

    private fun adicionarExercicio() {
        val dialogView = layoutInflater.inflate(R.layout.dialogo_adicionar_exercicio, null)
        val nomeEditText = dialogView.findViewById<EditText>(R.id.nomeExercicioEditText)
        val grupoSpinner = dialogView.findViewById<Spinner>(R.id.grupoMuscularSpinner)

        val grupos = listOf(
            "Trapézio", "Ombros", "Peito", "Tríceps", "Bíceps", "Antebraço",
            "Abdômen", "Costas", "Lombar", "Glúteos",
            "Frente Coxa", "Trás Coxa", "Panturrilha", "Cardio"
        )

        grupoSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            grupos
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Adicionar Exercício")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = nomeEditText.text.toString().trim()
                val grupo = grupoSpinner.selectedItem.toString()

                if (nome.isEmpty()) {
                    Toast.makeText(requireContext(), "Digite um nome válido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val novoRef = exerciciosRef.push()
                val exercicio = mapOf(
                    "id" to novoRef.key,
                    "nome" to nome,
                    "grupoMuscular" to grupo
                )

                novoRef.setValue(exercicio)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Exercício salvo!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Erro: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private var exerciciosListener: ValueEventListener? = null

    private fun carregarExercicios() {
        exerciciosListener?.let { exerciciosRef.removeEventListener(it) }

        exerciciosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaExercicios.clear()
                for (item in snapshot.children) {
                    val exercicio = item.getValue(Exercicio::class.java)
                    exercicio?.let { listaExercicios.add(it) }
                }
                adapter.updateLista(listaExercicios)
            }

            override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        exerciciosRef.addValueEventListener(exerciciosListener!!)
    }

    override fun onStop() {
        super.onStop()
        exerciciosListener?.let {
            exerciciosRef.removeEventListener(it)
            exerciciosListener = null
        }
    }

    override fun onResume() {
        super.onResume()
        carregarExercicios()
    }


    private fun excluirExercicio(exercicio: Exercicio) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val planosRef = FirebaseDatabase.getInstance().getReference("usuarios")
            .child(userId)
            .child("planos")

        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Exercício")
            .setMessage("Deseja excluir '${exercicio.nome}'?")
            .setPositiveButton("Sim") { dialog, _ ->
                exercicio.id?.let { exercicioId ->

                    planosRef.get().addOnSuccessListener { snapshot ->

                        val vinculado = snapshot.children.any { planoSnap ->
                            planoSnap.child("rotinas").children.any { rotinaSnap ->
                                rotinaSnap.child("exercicios").children.any { exercicioSnap ->
                                    exercicioSnap.child("id").getValue(String::class.java) == exercicioId
                                }
                            }
                        }

                        if (vinculado) {
                            Toast.makeText(
                                requireContext(),
                                "Não é possível excluir: este exercício está vinculado a uma rotina.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@addOnSuccessListener
                        }

                        exerciciosRef.child(exercicioId).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Exercício excluído com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Erro ao excluir: ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Erro ao verificar vínculos.", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    private fun editarExercicio(exercicio: Exercicio) {
        val dialogView = layoutInflater.inflate(R.layout.dialogo_adicionar_exercicio, null)
        val nomeEditText = dialogView.findViewById<EditText>(R.id.nomeExercicioEditText)
        val grupoSpinner = dialogView.findViewById<Spinner>(R.id.grupoMuscularSpinner)

        val grupos = listOf(
            "Trapézio", "Ombros", "Peito", "Tríceps", "Bíceps", "Antebraço",
            "Abdômen", "Costas", "Lombar", "Glúteos",
            "Frente Coxa", "Trás Coxa", "Panturrilha", "Cardio"
        )

        grupoSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            grupos
        )

        nomeEditText.setText(exercicio.nome)
        val posicaoGrupo = grupos.indexOf(exercicio.grupoMuscular)
        if (posicaoGrupo >= 0) grupoSpinner.setSelection(posicaoGrupo)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Exercício")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val novoNome = nomeEditText.text.toString().trim()
                val novoGrupo = grupoSpinner.selectedItem.toString()

                if (novoNome.isEmpty()) {
                    Toast.makeText(requireContext(), "Digite um nome válido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                exercicio.id?.let {
                    exerciciosRef.child(it).child("nome").setValue(novoNome)
                    exerciciosRef.child(it).child("grupoMuscular").setValue(novoGrupo)
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
            .show()
    }

}