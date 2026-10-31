package com.example.desafiopractico3.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun iniciarSesion(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _loginState.value = LoginState.Error("Los campos no pueden estar vacíos")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                // 1. Autenticar con Firebase Auth
                val authResult = auth.signInWithEmailAndPassword(email, pass).await()
                val userId = authResult.user?.uid

                if (userId != null) {
                    // 2. Consultar el documento del usuario en Firestore
                    val document = db.collection("usuarios").document(userId).get().await()

                    if (document.exists()) {
                        val rol = document.getString("rol") ?: "estudiante"
                        _loginState.value = LoginState.Success(rol)
                    } else {
                        _loginState.value = LoginState.Error("El usuario no tiene un rol asignado")
                    }
                }
            } catch (e: Exception) {

                _loginState.value = LoginState.Error(e.localizedMessage ?: "Error de autenticación")
            }
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val rol: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}