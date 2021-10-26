package com.juego.proyecto_1s2122.varios.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.modelo.Jugador

class JugadoresJuegoAdapter (private val dataSet: List<Jugador>,
) : RecyclerView.Adapter<JugadoresJuegoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tv_nombre)
        val adress: TextView = view.findViewById(R.id.tv_alias)
        val puntos: TextView = view.findViewById(R.id.tv_puntos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadoresJuegoAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.jugador_item_list, parent, false)
        )
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nombre.text = dataSet[position].nombre
        holder.adress.text = dataSet[position].alias
        holder.puntos.text = dataSet[position].puntos.toString()
    }

}