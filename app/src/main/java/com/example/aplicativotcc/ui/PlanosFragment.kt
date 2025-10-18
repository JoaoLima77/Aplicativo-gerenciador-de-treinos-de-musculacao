package com.example.aplicativotcc.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.LoginActivity
import com.example.aplicativotcc.R
import com.example.aplicativotcc.RotinasActivity
import com.example.aplicativotcc.adapter.PlanosAdapter
import com.example.aplicativotcc.model.PlanoDeTreino
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.InputStreamReader

class PlanosFragment : Fragment() {

    private lateinit var planosRecyclerView: RecyclerView
    private lateinit var planosAdapter: PlanosAdapter
    private lateinit var btnAddPlano: Button
    private lateinit var btnImportarPlano: ImageButton
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
        btnImportarPlano = view.findViewById(R.id.imgbtnImportar) // novo botão no layout

        inicializarRecyclerView()
        btnAddPlano.setOnClickListener { mostrarDialogAdicionarPlano() }
        carregarPlanosDoFirebase()

        // Configurar importação de CSV
        val pickFileLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { importarCSV(it) }
        }

        btnImportarPlano.setOnClickListener {
            pickFileLauncher.launch("text/*")
        }

        return view
    }

    private fun inicializarRecyclerView() {
        planosRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        planosAdapter = PlanosAdapter(
            listaPlanos,
            onItemClick = { plano ->
                val intent = Intent(requireContext(), RotinasActivity::class.java)
                intent.putExtra("PLANO_ID", plano.id)
                intent.putExtra("PLANO_NOME", plano.nome)
                startActivity(intent)
            },
            onDeleteClick = { plano -> mostrarDialogConfirmacaoExclusao(plano) },
            onEditClick = { plano -> mostrarDialogEditarPlano(plano) }
        )
        planosRecyclerView.adapter = planosAdapter
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
                planosAdapter.updatePlans(listaPlanos)
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

    private fun mostrarDialogEditarPlano(plano: PlanoDeTreino) {
        val input = EditText(requireContext())
        input.setText(plano.nome)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Nome do Plano")
            .setView(input)
            .setPositiveButton("Salvar") { dialog, _ ->
                val novoNome = input.text.toString().trim()
                if (novoNome.isNotEmpty() && plano.id != null) {
                    planosRef.child(plano.id).child("nome").setValue(novoNome)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // ---------------------- IMPORTAR ------------------------
    private fun importarCSV(uri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val reader = InputStreamReader(requireContext().contentResolver.openInputStream(uri))
        val linhas = reader.readLines().drop(1)
        reader.close()

        val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId)
        val planosRef = userRef.child("planos")
        val exerciciosGlobaisRef = userRef.child("exercicios")

        val novoPlanoRef = planosRef.push()
        var nomePlano: String? = null
        val rotinasMap = mutableMapOf<String, DatabaseReference>()

        // Buscar exercícios existentes para evitar duplicação
        exerciciosGlobaisRef.get().addOnSuccessListener { snapshot ->
            val exerciciosExistentes = mutableSetOf<String>()
            for (exSnap in snapshot.children) {
                val nomeExistente = exSnap.child("nome").getValue(String::class.java)
                nomeExistente?.let { exerciciosExistentes.add(it.lowercase().trim()) }
            }

            for (linha in linhas) {
                val partes = linha.split(",")
                if (partes.size < 8) continue

                nomePlano = partes[0]
                val nomeRotina = partes[1]
                val diaSemana = partes[2]
                val grupoMuscular = partes[3]
                val nomeEx = partes[4]
                val series = partes[5]
                val reps = partes[6]
                val peso = partes[7]

                // Cria ou obtém referência da rotina
                val rotinaRef = rotinasMap.getOrPut(nomeRotina) {
                    val ref = novoPlanoRef.child("rotinas").push()
                    ref.child("nome").setValue(nomeRotina)
                    ref.child("diaSemana").setValue(diaSemana)
                    ref
                }

                // Adiciona exercício dentro da rotina
                val exercicioRef = rotinaRef.child("exercicios").push()
                val exercicioId = exercicioRef.key ?: continue
                val exercicioData = mapOf(
                    "id" to exercicioId,
                    "grupoMuscular" to grupoMuscular,
                    "nome" to nomeEx,
                    "series" to series,
                    "repeticoes" to reps,
                    "peso" to peso
                )
                exercicioRef.setValue(exercicioData)

                // Adiciona no nó global só se ainda não existir
                if (!exerciciosExistentes.contains(nomeEx.lowercase().trim())) {
                    val globalExercicioRef = exerciciosGlobaisRef.push()
                    globalExercicioRef.setValue(
                        mapOf(
                            "id" to globalExercicioRef.key,
                            "grupoMuscular" to grupoMuscular,
                            "nome" to nomeEx
                        )
                    )
                    exerciciosExistentes.add(nomeEx.lowercase().trim())
                }
            }

            if (nomePlano != null)
                novoPlanoRef.child("nome").setValue(nomePlano)

            Toast.makeText(requireContext(), "Plano importado e exercícios cadastrados!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao verificar exercícios existentes.", Toast.LENGTH_SHORT).show()
        }
    }
}
