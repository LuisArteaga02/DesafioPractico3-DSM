package com.example.desafiopractico3.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun registrarUsuario(email: String, pass: String, rol: String, onResult: (Boolean, String) -> Unit) {

        val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*])[A-Za-z\\d!@#\$%^&*]{12,}\$"
        if (!Pattern.matches(passwordPattern, pass)) {
            onResult(false, "La contraseña debe tener 12 caracteres, mayúscula, minúscula, número y carácter especial.")
            return
        }

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        val userData = hashMapOf(
                            "email" to email,
                            "rol" to rol,
                            "favoritos" to emptyList<Int>()
                        )

                        db.collection("usuarios").document(userId)
                            .set(userData)
                            .addOnSuccessListener { onResult(true, "Registro exitoso") }
                            .addOnFailureListener { e -> onResult(false, "Error al guardar rol: ${e.message}") }
                    }
                } else {
                    onResult(false, "Error en registro: ${task.exception?.message}")
                }
            }
    }
}