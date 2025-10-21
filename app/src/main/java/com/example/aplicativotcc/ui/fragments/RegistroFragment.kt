package com.example.aplicativotcc.ui.fragments

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicativotcc.R
import com.example.aplicativotcc.adapter.RegistroAdapter
import com.example.aplicativotcc.model.Rotina
import com.example.aplicativotcc.ui.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.Calendar
import java.util.Locale

class RegistroFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RegistroAdapter
    private val listaRotinas = mutableListOf<Rotina>()
    private val diasComRotina = mutableSetOf<Int>()
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var rotinasRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_registro, container, false)
        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }
        calendarView = view.findViewById(R.id.calendarView)
        recyclerView = view.findViewById(R.id.recyclerRotinasDoDia)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RegistroAdapter(listaRotinas) { rotina ->
        }
        recyclerView.adapter = adapter
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return view
        rotinasRef = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(userId)
            .child("planos")

        carregarRotinas()

        calendarView.addDecorator(DiasDoMesAtualDecorator())
        calendarView.addDecorator(DiasForaDoMesDecorator())
        calendarView.setOnDateChangedListener { _, date, _ ->
            val calendario = Calendar.getInstance()
            calendario.time = date.date
            val diaSelecionado = when (calendario.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Segunda"
                Calendar.TUESDAY -> "Terça"
                Calendar.WEDNESDAY -> "Quarta"
                Calendar.THURSDAY -> "Quinta"
                Calendar.FRIDAY -> "Sexta"
                Calendar.SATURDAY -> "Sábado"
                else -> "Domingo"
            }
            mostrarRotinasDoDia(diaSelecionado)
        }
        return view
    }

    private fun carregarRotinas() {
        rotinasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                diasComRotina.clear()
                for (planoSnap in snapshot.children) {
                    val rotinasSnap = planoSnap.child("rotinas")
                    for (rotinaSnap in rotinasSnap.children) {
                        val rotina = rotinaSnap.getValue(Rotina::class.java)
                        rotina?.let {
                            when (it.diaSemana?.lowercase(Locale.getDefault())) {
                                "segunda" -> diasComRotina.add(Calendar.MONDAY)
                                "terça" -> diasComRotina.add(Calendar.TUESDAY)
                                "quarta" -> diasComRotina.add(Calendar.WEDNESDAY)
                                "quinta" -> diasComRotina.add(Calendar.THURSDAY)
                                "sexta" -> diasComRotina.add(Calendar.FRIDAY)
                                "sábado" -> diasComRotina.add(Calendar.SATURDAY)
                                "domingo" -> diasComRotina.add(Calendar.SUNDAY)
                            }
                        }
                    }
                }
                calendarView.addDecorator(DiaComRotinaDecorator())
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarRotinasDoDia(diaSemana: String) {
        listaRotinas.clear()
        rotinasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (planoSnap in snapshot.children) {
                    val rotinasSnap = planoSnap.child("rotinas")
                    for (rotinaSnap in rotinasSnap.children) {
                        val rotina = rotinaSnap.getValue(Rotina::class.java)
                        if (rotina?.diaSemana.equals(diaSemana, ignoreCase = true)) {
                            listaRotinas.add(rotina!!)
                        }
                    }
                }
                adapter.updateRotinas(listaRotinas)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    inner class DiaComRotinaDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            val cal = Calendar.getInstance()
            cal.time = day.date
            val diaSemana = cal.get(Calendar.DAY_OF_WEEK)
            return diasComRotina.contains(diaSemana)
        }
        override fun decorate(view: DayViewFacade) {
            val drawable = GradientDrawable()
            drawable.setStroke(2, "#3CD548".toColorInt())
            drawable.cornerRadius = 360f
            view.setBackgroundDrawable(drawable)
        }
    }

    inner class DiasDoMesAtualDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            val hoje = calendarView.currentDate
            return day.month == hoje.month && day.year == hoje.year
        }
        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan("#FFFFFF".toColorInt()))
        }
    }

    inner class DiasForaDoMesDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            val hoje = calendarView.currentDate
            return day.month != hoje.month || day.year != hoje.year
        }
        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan("#AA595959".toColorInt()))
        }
    }
}