package com.example.aplicativotcc.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.aplicativotcc.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var edtTxtEmail: EditText
    private lateinit var edtTxtSenha: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegistrar: Button
    private lateinit var checkBoxManterLogado: CheckBox
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        edtTxtEmail = findViewById(R.id.edtTxtEmail)
        edtTxtSenha = findViewById(R.id.edtTxtSenha)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        checkBoxManterLogado = findViewById(R.id.checkBox)

        val manterLogado = sharedPreferences.getBoolean("manterLogado", false)
        val userAtual = auth.currentUser
        if (manterLogado && userAtual != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val email = edtTxtEmail.text.toString().trim()
            val senha = edtTxtSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener {
                    if (checkBoxManterLogado.isChecked) {
                        sharedPreferences.edit { putBoolean("manterLogado", true) }
                    } else {
                        sharedPreferences.edit { putBoolean("manterLogado", false) }
                    }
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao entrar: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnRegistrar.setOnClickListener {
            val email = edtTxtEmail.text.toString().trim()
            val senha = edtTxtSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener {
                    Toast.makeText(this, "Conta criada!", Toast.LENGTH_SHORT).show()
                    if (checkBoxManterLogado.isChecked) {
                        sharedPreferences.edit { putBoolean("manterLogado", true) }
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