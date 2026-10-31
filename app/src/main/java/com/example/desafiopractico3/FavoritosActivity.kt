package com.example.desafiopractico3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.desafiopractico3.adapters.AlumnoAdapter
import com.example.desafiopractico3.databinding.ActivityFavoritosBinding
import com.example.desafiopractico3.models.Recurso
import com.example.desafiopractico3.Interfaces.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FavoritosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritosBinding
    private lateinit var adapter: AlumnoAdapter

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val favoritosDelUsuario = mutableSetOf<Int>()
    private val votosDelUsuario = mutableMapOf<Int, Float>()
    private var todosLosRecursos = listOf<Recurso>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón de regresar en la barra superior
        binding.topAppBar.setNavigationOnClickListener { finish() }

        configurarRecyclerView()
        cargarDatos()
    }

    private fun configurarRecyclerView() {
        adapter = AlumnoAdapter(
            listaRecursos = emptyList(),
            favoritosIds = favoritosDelUsuario,
            votosPersonales = votosDelUsuario,
            onAbrirEnlaceClick = { url -> abrirEnlaceEnNavegador(url) },
            onFavoritoClick = { recurso, _ -> quitarFavorito(recurso) }, // En esta pantalla solo quitamos
            onRatingChange = { recurso, estrellas -> enviarCalificacionApi(recurso, estrellas) }
        )
        binding.rvFavoritos.layoutManager = LinearLayoutManager(this)
        binding.rvFavoritos.adapter = adapter
    }

    private fun cargarDatos() {
        if (uid.isEmpty()) return
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Obtener favoritos y votos de Firebase (usamos await() para que sea secuencial en la corrutina)
                val favSnapshot = db.collection("usuarios").document(uid).collection("favoritos").get().await()
                favoritosDelUsuario.clear()
                favSnapshot.documents.forEach { doc -> doc.getLong("idRecurso")?.toInt()?.let { favoritosDelUsuario.add(it) } }

                val votosSnapshot = db.collection("usuarios").document(uid).collection("votos").get().await()
                votosDelUsuario.clear()
                votosSnapshot.documents.forEach { doc ->
                    val id = doc.getLong("idRecurso")?.toInt()
                    val estrellas = doc.getDouble("estrellas")?.toFloat()
                    if (id != null && estrellas != null) votosDelUsuario[id] = estrellas
                }

                // 2. Obtener recursos globales de Render
                val response = RetrofitClient.apiService.getRecursos()
                if (response.isSuccessful) {
                    todosLosRecursos = response.body() ?: emptyList()
                    filtrarYMostrar()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@FavoritosActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show() }
            } finally {
                withContext(Dispatchers.Main) { binding.progressBar.visibility = View.GONE }
            }
        }
    }

    private suspend fun filtrarYMostrar() {
        withContext(Dispatchers.Main) {
            // Filtramos conservando únicamente los recursos cuyo ID está en el Set de favoritos
            val listaFiltrada = todosLosRecursos.filter { favoritosDelUsuario.contains(it.id) }

            adapter.actualizarLista(listaFiltrada)

            // Mostrar texto si está vacío
            binding.tvVacio.visibility = if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun quitarFavorito(recurso: Recurso) {
        db.collection("usuarios").document(uid)
            .collection("favoritos").document(recurso.id.toString())
            .delete()
            .addOnSuccessListener {
                favoritosDelUsuario.remove(recurso.id)
                // Refiltramos la lista localmente para que la tarjeta desaparezca al instante
                lifecycleScope.launch { filtrarYMostrar() }
            }
    }

    private fun enviarCalificacionApi(recurso: Recurso, estrellas: Float) {
        if (votosDelUsuario.containsKey(recurso.id)) return
        binding.progressBar.visibility = View.VISIBLE
        val recursoActualizado = recurso.copy(ratingSum = recurso.ratingSum + estrellas.toInt(), ratingCount = recurso.ratingCount + 1)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.actualizarRecurso(recurso.id, recursoActualizado)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        db.collection("usuarios").document(uid).collection("votos").document(recurso.id.toString())
                            .set(hashMapOf("idRecurso" to recurso.id, "estrellas" to estrellas))
                            .addOnSuccessListener {
                                votosDelUsuario[recurso.id] = estrellas
                                adapter.notifyDataSetChanged()
                            }
                    }
                }
            } catch (e: Exception) {
                // Ignorar error de red en el scope de este ejemplo
            } finally {
                withContext(Dispatchers.Main) { binding.progressBar.visibility = View.GONE }
            }
        }
    }

    private fun abrirEnlaceEnNavegador(url: String) {
        try {
            var urlFormateada = url
            if (!url.startsWith("http://") && !url.startsWith("https://")) urlFormateada = "https://$url"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlFormateada)))
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir", Toast.LENGTH_SHORT).show()
        }
    }
}