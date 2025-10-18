package com.example.aplicativotcc.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aplicativotcc.R
import com.example.aplicativotcc.ui.fragments.ExerciciosFragment
import com.example.aplicativotcc.ui.fragments.PlanosFragment
import com.example.aplicativotcc.ui.fragments.RegistroFragment
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
                        .replace(R.id.home_fragment_container, RegistroFragment())
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