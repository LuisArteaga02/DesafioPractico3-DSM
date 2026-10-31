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
import com.example.desafiopractico3.models.Recurso
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AlumnoAdapter
    private val favoritosDelUsuario = mutableSetOf<Int>()
    private val votosDelUsuario = mutableMapOf<Int, Float>()

    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
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
        adapter = AlumnoAdapter(
            listaRecursos = emptyList(),
            favoritosIds = favoritosDelUsuario,
            votosPersonales = votosDelUsuario,
            onAbrirEnlaceClick = { url -> abrirEnlaceEnNavegador(url) },
            onFavoritoClick = { recurso, marcarComoFavorito ->
                gestionarFavorito(recurso, marcarComoFavorito)
            },
            onRatingChange = { recurso, nuevaCalificacion ->
                enviarCalificacionApi(recurso, nuevaCalificacion.toFloat())
            }
        )
        binding.rvRecursosAlumno.layoutManager = LinearLayoutManager(this)
        binding.rvRecursosAlumno.adapter = adapter

        cargarFavoritosDeFirebase()
    }
    private fun enviarCalificacionApi(recurso: Recurso, estrellas: Float) {
        if (votosDelUsuario.containsKey(recurso.id)) return

        binding.progressBarCentral.visibility = View.VISIBLE

        val recursoActualizado = recurso.copy(
            ratingSum = recurso.ratingSum + estrellas.toInt(),
            ratingCount = recurso.ratingCount + 1
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.actualizarRecurso(recurso.id, recursoActualizado)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val datosVoto = hashMapOf(
                            "idRecurso" to recurso.id,
                            "estrellas" to estrellas
                        )
                        db.collection("usuarios").document(uid).collection("votos")
                            .document(recurso.id.toString())
                            .set(datosVoto)
                            .addOnSuccessListener {
                                votosDelUsuario[recurso.id] = estrellas // Lo guardamos en local
                                Toast.makeText(this@MainActivity, "¡Voto registrado!", Toast.LENGTH_SHORT).show()
                                obtenerRecursosDesdeApi() // Refrescamos la pantalla
                            }
                    } else {
                        Toast.makeText(this@MainActivity, "Error al enviar voto", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBarCentral.visibility = View.GONE
                }
            }
        }
    }
    private fun cargarDatosDeFirebase() {
        if (uid.isEmpty()) return

        // Cargar Favoritos
        db.collection("usuarios").document(uid).collection("favoritos")
            .get().addOnSuccessListener { docs ->
                favoritosDelUsuario.clear()
                docs.forEach { favoritosDelUsuario.add(it.getLong("idRecurso")!!.toInt()) }
                adapter.notifyDataSetChanged()
            }

        // Cargar Votos Personales
        db.collection("usuarios").document(uid).collection("votos")
            .get().addOnSuccessListener { docs ->
                votosDelUsuario.clear()
                docs.forEach { doc ->
                    val id = doc.getLong("idRecurso")?.toInt()
                    val estrellas = doc.getDouble("estrellas")?.toFloat()
                    if (id != null && estrellas != null) votosDelUsuario[id] = estrellas
                }
                adapter.notifyDataSetChanged()
            }
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
    private fun cargarFavoritosDeFirebase() {
        if (uid.isEmpty()) return


        db.collection("usuarios").document(uid).collection("favoritos")
            .get()
            .addOnSuccessListener { documentos ->
                favoritosDelUsuario.clear()
                for (doc in documentos) {
                    val idRecurso = doc.getLong("idRecurso")?.toInt()
                    if (idRecurso != null) favoritosDelUsuario.add(idRecurso)
                }
                adapter.notifyDataSetChanged()
            }
    }
    private fun gestionarFavorito(recurso: Recurso, guardar: Boolean) {
        val docRef = db.collection("usuarios").document(uid)
            .collection("favoritos").document(recurso.id.toString())

        if (guardar) {
            val datosFavorito = hashMapOf(
                "idRecurso" to recurso.id,
                "titulo" to recurso.titulo
            )
            docRef.set(datosFavorito).addOnSuccessListener {
                favoritosDelUsuario.add(recurso.id)
                adapter.notifyDataSetChanged()
            }
        } else {
            docRef.delete().addOnSuccessListener {
                favoritosDelUsuario.remove(recurso.id)
                adapter.notifyDataSetChanged()
            }
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
        menuInflater.inflate(R.menu.menu_alumno, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_ver_favoritos -> {
                startActivity(Intent(this, FavoritosActivity::class.java))
                true
            }
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