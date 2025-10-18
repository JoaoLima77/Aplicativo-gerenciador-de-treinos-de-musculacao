package com.example.aplicativotcc.ui.activities

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.R
import com.example.aplicativotcc.adapter.RotinasAdapter
import com.example.aplicativotcc.model.Rotina
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RotinasActivity : AppCompatActivity() {

    private lateinit var planoId: String
    private lateinit var planoNome: String
    private lateinit var rotinaAdapter: RotinasAdapter
    private lateinit var rotinasRecyclerView: RecyclerView
    private lateinit var btnAddRotina: Button
    private lateinit var btnExportar: ImageButton

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

        val tituloRotinas = findViewById<TextView>(R.id.txtviewrotina)
        tituloRotinas.text = getString(R.string.titulo_rotinas, planoNome)

        rotinasRef = FirebaseDatabase.getInstance().getReference("usuarios")
            .child(userId)
            .child("planos")
            .child(planoId)
            .child("rotinas")

        inicializarRecyclerView()

        btnAddRotina = findViewById(R.id.BtnAddRotina)
        btnAddRotina.setOnClickListener { mostrarDialogAdicionarRotina() }

        btnExportar = findViewById(R.id.imgbtnExportar)
        btnExportar.setOnClickListener { exportarCSV() }

        carregarRotinasDoFirebase()

        val btnVoltar: ImageButton = findViewById(R.id.imgBtnVoltarPlanos)
        btnVoltar.setOnClickListener { finish() }
    }

    private fun inicializarRecyclerView() {
        rotinasRecyclerView = findViewById(R.id.rotinasRecyclerView)
        rotinasRecyclerView.layoutManager = LinearLayoutManager(this)

        rotinaAdapter = RotinasAdapter(
            listaRotinas,
            onDeleteClick = { rotina -> mostrarDialogConfirmacaoExclusao(rotina) },
            onItemClick = { rotina ->
                val intent = Intent(this, ExerciciosDaRotinaActivity::class.java)
                intent.putExtra("PLANO_ID", planoId)
                intent.putExtra("ROTINA_ID", rotina.id)
                intent.putExtra("ROTINA_NOME", rotina.nome)
                startActivity(intent)
            },
            onEditClick = { rotina -> mostrarDialogEditarRotina(rotina) }
        )

        rotinasRecyclerView.adapter = rotinaAdapter
    }

    private fun mostrarDialogAdicionarRotina() {
        val layout = layoutInflater.inflate(R.layout.dialogo_adicionar_rotina, null)
        val inputNome = layout.findViewById<EditText>(R.id.editNomeRotina)
        val spinnerDia = layout.findViewById<Spinner>(R.id.spinnerDiaSemana)

        val dias = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")
        spinnerDia.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dias)

        AlertDialog.Builder(this)
            .setTitle("Nova Rotina")
            .setView(layout)
            .setPositiveButton("Salvar") { dialog, _ ->
                val nomeRotina = inputNome.text.toString().trim()
                val diaSemana = spinnerDia.selectedItem.toString()
                if (nomeRotina.isNotEmpty()) {
                    salvarRotinaNoFirebase(nomeRotina, diaSemana)
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun salvarRotinaNoFirebase(nomeRotina: String, diaSemana: String) {
        val novaRef = rotinasRef.push()
        val novaRotina = mapOf(
            "id" to novaRef.key,
            "nome" to nomeRotina,
            "diaSemana" to diaSemana
        )
        novaRef.setValue(novaRotina)
    }

    private fun carregarRotinasDoFirebase() {
        rotinasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaRotinas.clear()
                for (rotinaSnap in snapshot.children) {
                    val rotina = rotinaSnap.getValue(Rotina::class.java)
                    rotina?.let { listaRotinas.add(it.copy(id = rotinaSnap.key)) }
                }
                rotinaAdapter.updateRotinas(listaRotinas)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RotinasActivity, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarDialogConfirmacaoExclusao(rotina: Rotina) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Rotina")
            .setMessage("Deseja excluir '${rotina.nome}'?")
            .setPositiveButton("Sim") { dialog, _ ->
                rotina.id?.let { rotinasRef.child(it).removeValue() }
                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun mostrarDialogEditarRotina(rotina: Rotina) {
        val layout = layoutInflater.inflate(R.layout.dialogo_adicionar_rotina, null)
        val inputNome = layout.findViewById<EditText>(R.id.editNomeRotina)
        val spinnerDia = layout.findViewById<Spinner>(R.id.spinnerDiaSemana)

        val dias = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")
        spinnerDia.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dias)

        inputNome.setText(rotina.nome)
        val pos = dias.indexOf(rotina.diaSemana)
        if (pos >= 0) spinnerDia.setSelection(pos)

        AlertDialog.Builder(this)
            .setTitle("Editar Rotina")
            .setView(layout)
            .setPositiveButton("Salvar") { dialog, _ ->
                val novoNome = inputNome.text.toString().trim()
                val novoDia = spinnerDia.selectedItem.toString()
                if (novoNome.isNotEmpty() && rotina.id != null) {
                    rotinasRef.child(rotina.id).apply {
                        child("nome").setValue(novoNome)
                        child("diaSemana").setValue(novoDia)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // ---------------------- EXPORTAR CSV ------------------------
    private fun exportarCSV() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val planosRef = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(userId)
            .child("planos")
            .child(planoId)

        planosRef.get().addOnSuccessListener { planoSnapshot ->
            val nomePlano = planoSnapshot.child("nome").getValue(String::class.java) ?: "Plano"
            val rotinasSnapshot = planoSnapshot.child("rotinas")

            val csvBuilder = StringBuilder()
            csvBuilder.append("Plano,Rotina,DiaDaSemana,GrupoMuscular,Exercicio,Series,Repeticoes,Peso\n")

            for (rotinaSnap in rotinasSnapshot.children) {
                val nomeRotina = rotinaSnap.child("nome").getValue(String::class.java)
                val diaSemana = rotinaSnap.child("diaSemana").getValue(String::class.java)
                val exerciciosSnap = rotinaSnap.child("exercicios")
                for (exercicioSnap in exerciciosSnap.children) {
                    val grupo = exercicioSnap.child("grupoMuscular").getValue(String::class.java)
                    val nomeEx = exercicioSnap.child("nome").getValue(String::class.java)
                    val series = exercicioSnap.child("series").getValue(String::class.java)
                    val reps = exercicioSnap.child("repeticoes").getValue(String::class.java)
                    val peso = exercicioSnap.child("peso").getValue(String::class.java)

                    csvBuilder.append("$nomePlano,$nomeRotina,$diaSemana,$grupo,$nomeEx,$series,$reps,$peso\n")
                }
            }

            val fileName = "${nomePlano.replace(" ", "_")}.csv"
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            // Compatível com versões antigas
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Files.getContentUri("external")
            }

            val fileUri = resolver.insert(collection, contentValues)

            fileUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvBuilder.toString().toByteArray())
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                Toast.makeText(this, "Arquivo exportado para Downloads/$fileName", Toast.LENGTH_LONG).show()
            } ?: Toast.makeText(this, "Erro ao criar arquivo", Toast.LENGTH_SHORT).show()
        }
    }
}