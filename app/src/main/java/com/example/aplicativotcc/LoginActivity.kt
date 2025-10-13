package com.example.aplicativotcc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.CheckBox
import android.content.SharedPreferences
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var checkBoxStayLogged: CheckBox
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        checkBoxStayLogged = findViewById(R.id.checkBox)
        
        val stayLogged = sharedPreferences.getBoolean("stayLogged", false)
        val currentUser = auth.currentUser
        if (stayLogged && currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    if (checkBoxStayLogged.isChecked) {
                        sharedPreferences.edit { putBoolean("stayLogged", true) }
                    } else {
                        sharedPreferences.edit { putBoolean("stayLogged", false) }
                    }
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao entrar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Conta criada!", Toast.LENGTH_SHORT).show()
                    if (checkBoxStayLogged.isChecked) {
                        sharedPreferences.edit { putBoolean("stayLogged", true) }
                    }
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao registrar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
