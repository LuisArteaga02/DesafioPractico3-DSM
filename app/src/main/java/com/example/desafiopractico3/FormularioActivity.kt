package com.example.desafiopractico3

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.desafiopractico3.databinding.ActivityFormularioBinding
import com.example.desafiopractico3.models.Recurso
import com.example.desafiopractico3.Interfaces.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FormularioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormularioBinding

    // Si esta variable es nula, estamos creando. Si tiene datos, estamos editando.
    private var recursoAEditar: Recurso? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val decorView = window.decorView
        val rootView = decorView.findViewById<android.view.ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background



        configurarDropdownTipos()

        // Revisar si el Activity anterior nos mandó un recurso para editar
        @Suppress("DEPRECATION")
        recursoAEditar = intent.getSerializableExtra("recurso") as? Recurso

        if (recursoAEditar != null) {
            prepararModoEdicion(recursoAEditar!!)
        }

        binding.btnGuardar.setOnClickListener {
            validarYGuardar()
        }
    }

    private fun configurarDropdownTipos() {
        val tipos = arrayOf("VIDEO", "PDF", "DOCUMENTO", "ENLACE")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipos)
        binding.etTipo.setAdapter(adapter)
    }

    private fun prepararModoEdicion(recurso: Recurso) {
        binding.tvTituloFormulario.text = "Editar Recurso"
        binding.btnGuardar.text = "Actualizar Recurso"

        binding.etTitulo.setText(recurso.titulo)
        binding.etTipo.setText(recurso.tipo, false)
        binding.etEnlace.setText(recurso.enlace)
        binding.etDescripcion.setText(recurso.descripcion)
    }

    private fun validarYGuardar() {
        val titulo = binding.etTitulo.text.toString().trim()
        val tipo = binding.etTipo.text.toString().trim()
        val enlace = binding.etEnlace.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()

        if (titulo.isEmpty() || tipo.isEmpty() || enlace.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }


        binding.btnGuardar.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val nuevoRecurso = Recurso(
            id = recursoAEditar?.id ?: 0,
            titulo = titulo,
            tipo = tipo,
            enlace = enlace,
            descripcion = descripcion,
            imagen = recursoAEditar?.imagen ?: "" // Evita el error de parámetro faltante
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = if (recursoAEditar == null) {
                    RetrofitClient.apiService.crearRecurso(nuevoRecurso)
                } else {
                    // Pasamos el ID directamente como Int
                    RetrofitClient.apiService.actualizarRecurso(recursoAEditar!!.id, nuevoRecurso)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@FormularioActivity, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                        finish() // Cerramos esta pantalla y volvemos a la lista
                    } else {
                        Toast.makeText(this@FormularioActivity, "Error al guardar: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FormularioActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.btnGuardar.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
}