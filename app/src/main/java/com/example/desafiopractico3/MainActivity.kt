package com.example.desafiopractico3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.desafiopractico3.adapters.AlumnoAdapter
import com.example.desafiopractico3.databinding.ActivityMainBinding
import com.example.desafiopractico3.Interfaces.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AlumnoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)

        configurarRecyclerView()
        configurarSwipeRefresh()

        obtenerRecursosDesdeApi()
    }
    private fun configurarRecyclerView() {
        adapter = AlumnoAdapter(emptyList()) { url ->
            abrirEnlaceEnNavegador(url)
        }
        binding.rvRecursosAlumno.layoutManager = LinearLayoutManager(this)
        binding.rvRecursosAlumno.adapter = adapter
    }

    private fun abrirEnlaceEnNavegador(url: String) {
        try {
            var urlFormateada = url
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                urlFormateada = "https://$url"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlFormateada))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
        }
    }
    private fun configurarSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            obtenerRecursosDesdeApi()
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
                    mostrarError("Error al cargar datos")
                }
            } catch (e: Exception) {
                mostrarError("Error de red.")
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBarCentral.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }
    private suspend fun mostrarError(mensaje: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, mensaje, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_docente, menu) // Reutilizamos el mismo menú de cerrar sesión
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



}