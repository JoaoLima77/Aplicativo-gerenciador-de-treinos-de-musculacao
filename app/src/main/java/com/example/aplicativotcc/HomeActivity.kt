package com.example.aplicativotcc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aplicativotcc.ui.ContaFragment
import com.example.aplicativotcc.ui.ExerciciosFragment
import com.example.aplicativotcc.ui.PlanosFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        supportFragmentManager.beginTransaction()
            .replace(R.id.home_fragment_container, PlanosFragment())
            .commit()

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_planos -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.home_fragment_container, PlanosFragment())
                        .commit()
                    true
                }
                R.id.nav_conta -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.home_fragment_container, ContaFragment())
                        .commit()
                    true
                }
                R.id.nav_exercicios -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.home_fragment_container, ExerciciosFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}
