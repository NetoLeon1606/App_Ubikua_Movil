package com.example.ep2_

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ep2_.model.LugarFavorito

class FavoritosAdapter(
    private var listaLugares: List<LugarFavorito>,
    private val alHacerClick: (LugarFavorito) -> Unit,
    private val alEliminarClick: (LugarFavorito) -> Unit
) : RecyclerView.Adapter<FavoritosAdapter.FavoritoViewHolder>() {

    class FavoritoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvItemNombre)
        val tvCategoria: TextView = view.findViewById(R.id.tvItemCategoria)
        val tvDireccion: TextView = view.findViewById(R.id.tvItemDireccion)
        val tvCoordenadas: TextView = view.findViewById(R.id.tvItemCoordenadas)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnItemEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorito, parent, false)
        return FavoritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {
        val lugar = listaLugares[position]

        holder.tvNombre.text = lugar.nombreLugar
        holder.tvCategoria.text = "Categoría: ${lugar.categoria}"
        holder.tvDireccion.text = lugar.direccion
        holder.tvCoordenadas.text = "Lat: ${lugar.latitud}, Lon: ${lugar.longitud}"

        holder.itemView.setOnClickListener { alHacerClick(lugar) }
        holder.btnEliminar.setOnClickListener { alEliminarClick(lugar) }
    }

    override fun getItemCount(): Int = listaLugares.size

    fun actualizarLista(nuevaLista: List<LugarFavorito>) {
        listaLugares = nuevaLista
        notifyDataSetChanged()
    }
}