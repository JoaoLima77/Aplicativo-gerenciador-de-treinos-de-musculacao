package com.example.aplicativotcc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
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

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Conta criada!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao registrar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
