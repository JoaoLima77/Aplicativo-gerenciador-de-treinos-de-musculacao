package com.example.aplicativotcc

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.adapter.RotinaAdapter
import com.example.aplicativotcc.model.Rotina
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RotinasActivity : AppCompatActivity() {

    private lateinit var planoId: String
    private lateinit var planoNome: String
    private lateinit var rotinaAdapter: RotinaAdapter
    private lateinit var rotinasRecyclerView: RecyclerView
    private lateinit var btnAddRotina: Button

    private val listaRotinas = mutableListOf<Rotina>()
    private lateinit var rotinasRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            finish()
            return
        }

        setContentView(R.layout.activity_rotinas)

        planoId = intent.getStringExtra("PLANO_ID") ?: ""
        planoNome = intent.getStringExtra("PLANO_NOME") ?: ""
        title = "Rotinas de $planoNome"

        rotinasRef = FirebaseDatabase.getInstance().getReference("usuarios")
            .child(userId)
            .child("planos")
            .child(planoId)
            .child("rotinas")

        inicializarRecyclerView()

        btnAddRotina = findViewById(R.id.BtnAddRotina)
        btnAddRotina.setOnClickListener {
            mostrarDialogAdicionarRotina()
        }

        carregarRotinasDoFirebase()

        val btnVoltar: ImageButton = findViewById(R.id.imgBtnVoltarPlanos)
        btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun inicializarRecyclerView() {
        rotinasRecyclerView = findViewById(R.id.rotinasRecyclerView)
        rotinasRecyclerView.layoutManager = LinearLayoutManager(this)

        rotinaAdapter = RotinaAdapter(
            listaRotinas,
            onDeleteClick = { rotina ->
                mostrarDialogConfirmacaoExclusao(rotina)
            },
            onItemClick = { rotina ->
                val intent = Intent(this, ExerciciosRotinaActivity::class.java)
                intent.putExtra("PLANO_ID", planoId)
                intent.putExtra("ROTINA_ID", rotina.id)
                intent.putExtra("ROTINA_NOME", rotina.nome)
                startActivity(intent)
            }
        )

        rotinasRecyclerView.adapter = rotinaAdapter
    }

    private fun mostrarDialogAdicionarRotina() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nova Rotina")

        val input = EditText(this)
        input.hint = "Nome da Rotina"
        builder.setView(input)

        builder.setPositiveButton("Salvar") { dialog, _ ->
            val nomeRotina = input.text.toString().trim()
            if (nomeRotina.isNotEmpty()) {
                salvarRotinaNoFirebase(nomeRotina)
                dialog.dismiss()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun salvarRotinaNoFirebase(nomeRotina: String) {
        val novaRef = rotinasRef.push()
        val novaRotina = mapOf("nome" to nomeRotina)

        novaRef.setValue(novaRotina)
    }

    private fun carregarRotinasDoFirebase() {
        rotinasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaRotinas.clear()
                for (rotinaSnap in snapshot.children) {
                    val rotina = rotinaSnap.getValue(Rotina::class.java)
                    rotina?.let {
                        listaRotinas.add(it.copy(id = rotinaSnap.key))
                    }
                }
                rotinaAdapter.updateRotinas(listaRotinas)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro: ${error.message}")
            }
        })
    }

    private fun mostrarDialogConfirmacaoExclusao(rotina: Rotina) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Rotina")
            .setMessage("Deseja realmente excluir '${rotina.nome}'?")
            .setPositiveButton("Sim") { dialog, _ ->
                rotina.id?.let { rotinasRef.child(it).removeValue() }
                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
