package com.example.aplicativotcc.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.aplicativotcc.LoginActivity
import com.example.aplicativotcc.R
import com.google.firebase.auth.FirebaseAuth
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

class ContaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_conta, container, false)

        // Botão de logout
        val logoutButton = view.findViewById<Button>(R.id.btnLogout)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)

        // Decorator → mês atual em branco
        class CurrentMonthDayDecorator : DayViewDecorator {
            private val todayMonth = CalendarDay.today().month

            override fun shouldDecorate(day: CalendarDay): Boolean {
                return day.month == todayMonth
            }

            override fun decorate(view: DayViewFacade) {
                view.addSpan(android.text.style.ForegroundColorSpan(Color.WHITE))
            }
        }

        // Decorator → outros meses em cinza
        class OtherMonthDayDecorator : DayViewDecorator {
            private val todayMonth = CalendarDay.today().month

            override fun shouldDecorate(day: CalendarDay): Boolean {
                return day.month != todayMonth
            }

            override fun decorate(view: DayViewFacade) {
                view.addSpan(android.text.style.ForegroundColorSpan(Color.GRAY))
            }
        }

        // Aplica os decorators
        calendarView.addDecorator(CurrentMonthDayDecorator())
        calendarView.addDecorator(OtherMonthDayDecorator())
        return view
    }
}
