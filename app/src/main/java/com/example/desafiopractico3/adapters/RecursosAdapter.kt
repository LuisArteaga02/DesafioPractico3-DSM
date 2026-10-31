package com.example.desafiopractico3.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.desafiopractico3.databinding.ItemRecursoDocenteBinding
import com.example.desafiopractico3.models.Recurso


class RecursosAdapter(
    private var listaRecursos: List<Recurso>,
    private val onEditarClick: (Recurso) -> Unit,
    private val onEliminarClick: (Recurso) -> Unit
) : RecyclerView.Adapter<RecursosAdapter.RecursoViewHolder>() {


    inner class RecursoViewHolder(private val binding: ItemRecursoDocenteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recurso: Recurso) {
            binding.tvTitulo.text = recurso.titulo
            binding.tvTipo.text = recurso.tipo
            binding.tvDescripcion.text = recurso.descripcion

            binding.btnEditar.setOnClickListener {
                onEditarClick(recurso)
            }

            binding.btnEliminar.setOnClickListener {
                onEliminarClick(recurso)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecursoViewHolder {
        val binding = ItemRecursoDocenteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecursoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecursoViewHolder, position: Int) {
        holder.bind(listaRecursos[position])
    }

    // Le dice al RecyclerView cuántos elementos hay en total
    override fun getItemCount(): Int = listaRecursos.size

    // Función útil para refrescar la lista cuando la API responde
    fun actualizarLista(nuevaLista: List<Recurso>) {
        listaRecursos = nuevaLista
        notifyDataSetChanged()
    }
}