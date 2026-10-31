package com.example.desafiopractico3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.desafiopractico3.R
import com.example.desafiopractico3.databinding.ItemRecursoAlumnoBinding
import com.example.desafiopractico3.models.Recurso

class AlumnoAdapter(
    private var listaRecursos: List<Recurso>,
    private val favoritosIds: Set<Int>,
    private val onAbrirEnlaceClick: (String) -> Unit,
    private val onFavoritoClick: (Recurso, Boolean) -> Unit,
    private val onRatingChange: (Recurso, Float) -> Unit
) : RecyclerView.Adapter<AlumnoAdapter.AlumnoViewHolder>() {

    inner class AlumnoViewHolder(private val binding: ItemRecursoAlumnoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recurso: Recurso) {
            binding.tvTitulo.text = recurso.titulo
            binding.tvTipo.text = recurso.tipo
            binding.tvDescripcion.text = recurso.descripcion

            val promedio = if (recurso.ratingCount > 0) {
                recurso.ratingSum.toFloat() / recurso.ratingCount
            } else {
                0f
            }
            binding.ratingBar.rating = promedio
            binding.tvRatingCount.text = "(${recurso.ratingCount})"

            val esFavorito = favoritosIds.contains(recurso.id)
            if (esFavorito) {
                binding.btnFavorito.setImageResource(R.drawable.ic_favorite) // Corazón lleno
            } else {
                binding.btnFavorito.setImageResource(R.drawable.ic_favorite_border) // Corazón vacío
            }


            binding.btnAbrirEnlace.setOnClickListener {
                onAbrirEnlaceClick(recurso.enlace)
            }
            binding.btnFavorito.setOnClickListener {
                val nuevoEstado = !favoritosIds.contains(recurso.id)
                onFavoritoClick(recurso, nuevoEstado)
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