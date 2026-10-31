package com.example.desafiopractico3

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.desafiopractico3.Interfaces.RetrofitClient
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
    }

    override fun onResume() {
        super.onResume()
        obtenerRecursosDesdeApi()
    }



    private fun configurarRecyclerView() {
        adapter = RecursosAdapter(
            listaRecursos = emptyList(),
            onEditarClick = { recurso ->
                // MODO EDITAR: Abrimos el formulario enviando el objeto completo
                val intent = Intent(this, FormularioActivity::class.java)
                intent.putExtra("recurso", recurso)
                startActivity(intent)
            },
            onEliminarClick = { recurso ->
                // MODO ELIMINAR: Mostramos un diálogo de confirmación antes de borrar
                mostrarDialogoEliminar(recurso.id, recurso.titulo)
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
            // MODO CREAR: Abrimos el formulario vacío
            val intent = Intent(this, FormularioActivity::class.java)
            startActivity(intent)
        }
    }

    // --- LÓGICA DE API Y DATOS ---



    private suspend fun mostrarError(mensaje: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@DocenteActivity, mensaje, Toast.LENGTH_LONG).show()
        }
    }

    private fun obtenerRecursosDesdeApi() {

        binding.progressBarCentral.visibility = View.VISIBLE


        lifecycleScope.launch(Dispatchers.IO) {
            try {

                val response = RetrofitClient.apiService.getRecursos()

                if (response.isSuccessful) {

                    val lista = response.body() ?: emptyList()


                    withContext(Dispatchers.Main) {
                        adapter.actualizarLista(lista)
                    }
                } else {

                    mostrarError("Error en API: Código ${response.code()}")
                }

            } catch (e: Exception) {

                mostrarError("Error de red: Verifica tu conexión o el estado de Render.")
            } finally {

                withContext(Dispatchers.Main) {
                    binding.progressBarCentral.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun mostrarDialogoEliminar(id: Int, titulo: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar recurso")
            .setMessage("¿Estás seguro de que deseas eliminar '$titulo'?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                eliminarRecursoEnApi(id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarRecursoEnApi(id: Int) {
        binding.progressBarCentral.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.eliminarRecurso(id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DocenteActivity, "Recurso eliminado", Toast.LENGTH_SHORT).show()
                        obtenerRecursosDesdeApi() // Recargamos la lista
                    } else {
                        Toast.makeText(this@DocenteActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                mostrarError("Error de conexión.")
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBarCentral.visibility = View.GONE
                }
            }
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