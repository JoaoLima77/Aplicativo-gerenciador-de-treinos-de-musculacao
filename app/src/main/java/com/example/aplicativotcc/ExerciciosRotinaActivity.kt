package com.example.aplicativotcc

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.adapter.ExercicioAdapter
import com.example.aplicativotcc.model.Exercicio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExerciciosRotinaActivity : AppCompatActivity() {

    private lateinit var exerciciosRef: DatabaseReference
    private lateinit var rotinaRef: DatabaseReference
    private lateinit var adapter: ExercicioAdapter

    private val listaExercicios = mutableListOf<Exercicio>()
    private lateinit var planoId: String
    private lateinit var rotinaId: String
    private lateinit var rotinaNome: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercicios_rotina)

        planoId = intent.getStringExtra("PLANO_ID") ?: ""
        rotinaId = intent.getStringExtra("ROTINA_ID") ?: ""
        rotinaNome = intent.getStringExtra("ROTINA_NOME") ?: ""
        title = "Exercícios: $rotinaNome"

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        exerciciosRef = FirebaseDatabase.getInstance().getReference("usuarios")
            .child(userId)
            .child("exercicios")

        rotinaRef = FirebaseDatabase.getInstance().getReference("usuarios")
            .child(userId)
            .child("planos")
            .child(planoId)
            .child("rotinas")
            .child(rotinaId)
            .child("exercicios")

        val recyclerView = findViewById<RecyclerView>(R.id.exerciciosDaContaRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnVoltar: ImageButton = findViewById(R.id.imgBtnVoltarRotinas)
        btnVoltar.setOnClickListener {
            finish()
        }

        adapter = ExercicioAdapter(listaExercicios) { exercicio ->
            mostrarDialogAdicionarInfo(exercicio)
        }

        recyclerView.adapter = adapter
        carregarExercicios()
    }

    private fun carregarExercicios() {
        exerciciosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaExercicios.clear()
                for (item in snapshot.children) {
                    val exercicio = item.getValue(Exercicio::class.java)
                    exercicio?.let { listaExercicios.add(it) }
                }
                adapter.updateLista(listaExercicios)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ExerciciosRotinaActivity, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarDialogAdicionarInfo(exercicio: Exercicio) {
        val layout = layoutInflater.inflate(R.layout.dialog_info_exercicio, null)
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

                rotinaRef.push().setValue(exercicioMap)
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
}
