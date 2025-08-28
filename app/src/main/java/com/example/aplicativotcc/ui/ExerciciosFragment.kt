package com.example.aplicativotcc.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.LoginActivity
import com.example.aplicativotcc.R
import com.example.aplicativotcc.adapter.ExerciciosAdapter
import com.example.aplicativotcc.model.Exercicio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ExerciciosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExerciciosAdapter
    private lateinit var btnAdd: Button
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
        btnAdd = view.findViewById(R.id.btnAddExercicio)

        adapter = ExerciciosAdapter(
            listaExercicios,
            onDeleteClick = { exercicio -> confirmarExclusao(exercicio) },
            onEditClick = { exercicio -> mostrarDialogEditarExercicio(exercicio) } // novo
        )


        val searchEditText = view.findViewById<EditText>(R.id.edtTxtPesquisaExc)

        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
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

        btnAdd.setOnClickListener { mostrarDialogAdicionarExercicio() }

        carregarExercicios()

        return view
    }

    private fun mostrarDialogAdicionarExercicio() {
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

    private fun carregarExercicios() {
        exerciciosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaExercicios.clear()
                for (item in snapshot.children) {
                    val exercicio = item.getValue(Exercicio::class.java)
                    exercicio?.let { listaExercicios.add(it) }
                }
                adapter.updateLista(listaExercicios)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erro ao carregar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmarExclusao(exercicio: Exercicio) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Exercício")
            .setMessage("Deseja excluir '${exercicio.nome}'?")
            .setPositiveButton("Sim") { dialog, _ ->
                exercicio.id?.let { exerciciosRef.child(it).removeValue() }
                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    private fun mostrarDialogEditarExercicio(exercicio: Exercicio) {
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
