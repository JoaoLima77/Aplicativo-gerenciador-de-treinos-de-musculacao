package com.example.aplicativotcc

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.adapter.ExerciciosDarotinaAdapter
import com.example.aplicativotcc.model.Exercicio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ExerciciosDaRotinaActivity : AppCompatActivity() {

    private lateinit var adapterDaRotina: ExerciciosDarotinaAdapter
    private lateinit var exerciciosGlobaisRef: DatabaseReference
    private lateinit var exerciciosDaRotinaRef: DatabaseReference

    private val listaExerciciosDaRotina = mutableListOf<Exercicio>()

    private lateinit var planoId: String
    private lateinit var rotinaId: String
    private lateinit var rotinaNome: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercicios_da_rotina)

        planoId = intent.getStringExtra("PLANO_ID") ?: ""
        rotinaId = intent.getStringExtra("ROTINA_ID") ?: ""
        rotinaNome = intent.getStringExtra("ROTINA_NOME") ?: ""
        title = "Exercícios: $rotinaNome"

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        exerciciosGlobaisRef = FirebaseDatabase.getInstance().getReference("usuarios")
            .child(userId)
            .child("exercicios")

        exerciciosDaRotinaRef = FirebaseDatabase.getInstance().getReference("usuarios")
            .child(userId)
            .child("planos")
            .child(planoId)
            .child("rotinas")
            .child(rotinaId)
            .child("exercicios")

        val recyclerView = findViewById<RecyclerView>(R.id.exerciciosDaContaRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnVoltar: ImageButton = findViewById(R.id.imgBtnVoltarRotinas)
        btnVoltar.setOnClickListener { finish() }

        val btnAdicionar: Button = findViewById(R.id.btnAddExercicio)
        btnAdicionar.setOnClickListener { mostrarDialogListaDeExerciciosGlobais() }

        adapterDaRotina = ExerciciosDarotinaAdapter(
            listaExerciciosDaRotina,
            onEdit = { exercicio ->
                mostrarDialogEditarExercicio(exercicio)
            },
            onDelete = { exercicio ->
                confirmarRemocaoExercicio(exercicio)
            }
        )

        recyclerView.adapter = adapterDaRotina

        carregarExerciciosDaRotina()
    }

    private fun carregarExerciciosDaRotina() {
        exerciciosDaRotinaRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaExerciciosDaRotina.clear()
                for (item in snapshot.children) {
                    val exercicio = item.getValue(Exercicio::class.java)
                    exercicio?.let { listaExerciciosDaRotina.add(it) }
                }
                adapterDaRotina.updateLista(listaExerciciosDaRotina)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ExerciciosDaRotinaActivity, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarDialogListaDeExerciciosGlobais() {
        exerciciosGlobaisRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val exerciciosGlobais = mutableListOf<Exercicio>()
                for (item in snapshot.children) {
                    val exercicio = item.getValue(Exercicio::class.java)
                    exercicio?.let { exerciciosGlobais.add(it) }
                }

                if (exerciciosGlobais.isEmpty()) {
                    Toast.makeText(this@ExerciciosDaRotinaActivity, "Nenhum exercício disponível.", Toast.LENGTH_SHORT).show()
                    return
                }

                val dialogView = layoutInflater.inflate(R.layout.dialog_lista_exercicios, null)
                val editBuscar = dialogView.findViewById<EditText>(R.id.editBuscarExercicio)
                val recycler = dialogView.findViewById<RecyclerView>(R.id.recyclerExerciciosDialog)

                recycler.layoutManager = LinearLayoutManager(this@ExerciciosDaRotinaActivity)
                val adapter = ExerciciosDarotinaAdapter(
                    exerciciosGlobais,
                    modoSelecao = true,
                    onSelect = { exercicio ->
                        mostrarDialogAdicionarInfo(exercicio)
                    }
                )

                recycler.adapter = adapter

                val dialog = AlertDialog.Builder(this@ExerciciosDaRotinaActivity)
                    .setTitle("Escolha um exercício")
                    .setView(dialogView)
                    .setNegativeButton("Fechar", null)
                    .create()

                dialog.show()

                editBuscar.addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        val termo = s.toString().trim().lowercase()
                        val filtrados = exerciciosGlobais.filter {
                            it.nome?.lowercase()?.contains(termo) == true
                        }
                        adapter.updateLista(filtrados)
                    }
                    override fun afterTextChanged(s: android.text.Editable?) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ExerciciosDaRotinaActivity, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun mostrarDialogAdicionarInfo(exercicio: Exercicio) {
        val layout = layoutInflater.inflate(R.layout.dialogo_info_exercicio, null)
        val inputSeries = layout.findViewById<EditText>(R.id.editSeries)
        val inputReps = layout.findViewById<EditText>(R.id.editReps)
        val inputPeso = layout.findViewById<EditText>(R.id.editPeso)

        AlertDialog.Builder(this)
            .setTitle("Adicionar ${exercicio.nome}")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val series = inputSeries.text.toString()
                val reps = inputReps.text.toString()
                val peso = inputPeso.text.toString()

                val exercicioMap = mapOf(
                    "id" to exercicio.id,
                    "nome" to exercicio.nome,
                    "grupoMuscular" to exercicio.grupoMuscular,
                    "series" to series,
                    "repeticoes" to reps,
                    "peso" to peso
                )

                exerciciosDaRotinaRef.push().setValue(exercicioMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Exercício adicionado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogEditarExercicio(exercicio: Exercicio) {
        val layout = layoutInflater.inflate(R.layout.dialogo_info_exercicio, null)
        val inputSeries = layout.findViewById<EditText>(R.id.editSeries)
        val inputReps = layout.findViewById<EditText>(R.id.editReps)
        val inputPeso = layout.findViewById<EditText>(R.id.editPeso)

        inputSeries.setText(exercicio.series ?: "")
        inputReps.setText(exercicio.repeticoes ?: "")
        inputPeso.setText(exercicio.peso ?: "")

        AlertDialog.Builder(this)
            .setTitle("Editar ${exercicio.nome}")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val updated = mapOf(
                    "id" to exercicio.id,
                    "nome" to exercicio.nome,
                    "grupoMuscular" to exercicio.grupoMuscular,
                    "series" to inputSeries.text.toString(),
                    "repeticoes" to inputReps.text.toString(),
                    "peso" to inputPeso.text.toString()
                )

                exercicio.id?.let { id ->
                    exerciciosDaRotinaRef.orderByChild("id").equalTo(id)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (child in snapshot.children) {
                                    child.ref.setValue(updated)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun confirmarRemocaoExercicio(exercicio: Exercicio) {
        AlertDialog.Builder(this)
            .setTitle("Excluir exercício")
            .setMessage("Deseja excluir '${exercicio.nome}'?")
            .setPositiveButton("Sim") { _, _ ->
                exercicio.id?.let { id ->
                    exerciciosDaRotinaRef.orderByChild("id").equalTo(id)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (child in snapshot.children) {
                                    child.ref.removeValue()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@ExerciciosDaRotinaActivity, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }
}