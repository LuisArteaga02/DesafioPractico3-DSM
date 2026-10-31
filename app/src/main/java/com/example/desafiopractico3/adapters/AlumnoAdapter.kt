package com.example.desafiopractico3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.desafiopractico3.databinding.ItemRecursoAlumnoBinding
import com.example.desafiopractico3.models.Recurso

class AlumnoAdapter(
    private var listaRecursos: List<Recurso>,
    private val onAbrirEnlaceClick: (String) -> Unit
) : RecyclerView.Adapter<AlumnoAdapter.AlumnoViewHolder>() {

    inner class AlumnoViewHolder(private val binding: ItemRecursoAlumnoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recurso: Recurso) {
            binding.tvTitulo.text = recurso.titulo
            binding.tvTipo.text = recurso.tipo
            binding.tvDescripcion.text = recurso.descripcion

            binding.btnAbrirEnlace.setOnClickListener {
                onAbrirEnlaceClick(recurso.enlace)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumnoViewHolder {
        val binding = ItemRecursoAlumnoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlumnoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlumnoViewHolder, position: Int) {
        holder.bind(listaRecursos[position])
    }

    override fun getItemCount(): Int = listaRecursos.size

    fun actualizarLista(nuevaLista: List<Recurso>) {
        listaRecursos = nuevaLista
        notifyDataSetChanged()
    }
}