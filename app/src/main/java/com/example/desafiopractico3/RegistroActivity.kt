package com.example.desafiopractico3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.desafiopractico3.databinding.ActivityRegistroBinding
import com.example.desafiopractico3.utils.AuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private val authManager = AuthManager() // Instanciamos tu clase de utilidades

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrar.setOnClickListener { validarYRegistrar() }

        binding.tvIrLogin.setOnClickListener {
            finish() // Cierra la pantalla y vuelve al login
        }
    }

    private fun validarYRegistrar() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // 1. Validaciones básicas de la UI
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Las contraseñas no coinciden"
            return
        }

        // Determinar el rol desde los RadioButtons
        val rolSeleccionado = if (binding.rbDocente.isChecked) "docente" else "alumno"

        // Bloquear botones mientras carga
        binding.btnRegistrar.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE


        authManager.registrarUsuario(email, password, rolSeleccionado) { exito, mensaje ->


            runOnUiThread {
                binding.btnRegistrar.isEnabled = true
                binding.progressBar.visibility = View.GONE

                if (exito) {
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()


                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {

                    if (mensaje.contains("12 caracteres")) {
                        mostrarErrorPassword()
                    } else {
                        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun mostrarErrorPassword() {
        AlertDialog.Builder(this)
            .setTitle("Contraseña débil")
            .setMessage("Tu contraseña debe tener:\n" +
                    "- Mínimo 12 caracteres.\n" +
                    "- Al menos una letra mayúscula.\n" +
                    "- Al menos una letra minúscula.\n" +
                    "- Al menos un número.\n" +
                    "- Al menos un carácter especial (! @#$%^&*).")
            .setPositiveButton("Entendido", null)
            .show()
    }
}