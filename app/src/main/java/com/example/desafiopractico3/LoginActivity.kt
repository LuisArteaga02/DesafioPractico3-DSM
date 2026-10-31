package com.example.desafiopractico3
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.desafiopractico3.ViewModel.LoginViewModel
import kotlinx.coroutines.launch
import com.example.desafiopractico3.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    // Asumiendo que usas ViewBinding
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()
            viewModel.iniciarSesion(email, pass)
        }
        binding.tvIrRegistro.setOnClickListener {
            intent = Intent(this@LoginActivity, RegistroActivity::class.java)
            startActivity(intent)
        }

        observarEstadoLogin()
    }
    override fun onStart() {
        super.onStart()
        val usuarioActual = auth.currentUser

        if (usuarioActual != null) {
            binding.progressBar.visibility = View.VISIBLE
            redirigirSegunRol(usuarioActual.uid)
        }
    }

    private fun redirigirSegunRol(uid: String) {

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { documento ->
                binding.progressBar.visibility = View.GONE

                if (documento != null && documento.exists()) {
                    val rol = documento.getString("rol") ?: ""

                    if (rol.equals("Docente", ignoreCase = true)) {
                        startActivity(Intent(this, DocenteActivity::class.java))
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                    }


                    finish()
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE

            }
    }


    private fun observarEstadoLogin() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginViewModel.LoginState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                    is LoginViewModel.LoginState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnLogin.isEnabled = false
                    }
                    is LoginViewModel.LoginState.Success -> {
                        binding.progressBar.visibility = View.GONE

                        // Enrutamiento condicional según el laboratorio
                        val intent = if (state.rol == "docente") {
                            Intent(this@LoginActivity, DocenteActivity::class.java)
                           // Intent(this@LoginActivity, MainActivity::class.java)
                        } else {

                            Intent(this@LoginActivity, MainActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    }
                    is LoginViewModel.LoginState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}