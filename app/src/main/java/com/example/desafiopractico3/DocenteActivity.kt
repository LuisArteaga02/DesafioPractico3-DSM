package com.example.desafiopractico3

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.desafiopractico3.adapters.RecursosAdapter
import com.example.desafiopractico3.databinding.ActivityDocenteBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocenteBinding
    private lateinit var adapter: RecursosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)


        configurarRecyclerView()
        configurarSwipeRefresh()
        configurarFab()

        obtenerRecursosDesdeApi()
    }



    private fun configurarRecyclerView() {
        adapter = RecursosAdapter(
            listaRecursos = emptyList(),
            onEditarClick = { recurso ->
                // TODO: Navegar a FormularioActivity enviando los datos del recurso a editar
                Toast.makeText(this, "Editar: ${recurso.titulo}", Toast.LENGTH_SHORT).show()
            },
            onEliminarClick = { recurso ->
                // TODO: Llamar al endpoint de DELETE de Retrofit
                Toast.makeText(this, "Eliminar: ${recurso.titulo}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvRecursos.layoutManager = LinearLayoutManager(this)
        binding.rvRecursos.adapter = adapter
    }

    private fun configurarSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            // Se ejecuta al arrastrar la lista hacia abajo
            obtenerRecursosDesdeApi()
        }
    }

    private fun configurarFab() {
        binding.fabAgregar.setOnClickListener {
            // TODO: Abrir FormularioActivity en modo "Crear nuevo"
            Toast.makeText(this, "Pantalla para agregar recurso", Toast.LENGTH_SHORT).show()
        }
    }

    // --- LÓGICA DE API Y DATOS ---

    private fun obtenerRecursosDesdeApi() {
        binding.progressBarCentral.visibility = View.VISIBLE

        // Usamos Corrutinas para no bloquear la interfaz principal
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Aquí va tu llamada a Retrofit. Ejemplo:
                // val response = RetrofitClient.apiService.getRecursos()

                // if (response.isSuccessful && response.body() != null) {
                //     val lista = response.body()!!
                //     withContext(Dispatchers.Main) {
                //         adapter.actualizarLista(lista)
                //     }
                // } else {
                //     mostrarError("Error en API: ${response.code()}")
                // }

            } catch (e: Exception) {
                mostrarError("Error de red: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBarCentral.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false // Detener animación
                }
            }
        }
    }

    private suspend fun mostrarError(mensaje: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@DocenteActivity, mensaje, Toast.LENGTH_LONG).show()
        }
    }

    // --- MENÚ Y CIERRE DE SESIÓN ---

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_docente, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                cerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        // Limpiamos el historial para que no pueda volver con el botón "Atrás"
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}