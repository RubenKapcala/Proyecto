package com.juego.proyecto_1s2122.vistas.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.modelo.Partida

class PartidasEstadisticasAdapter (val context: Context,private val dataSet: List<Partida>
) : RecyclerView.Adapter<PartidasEstadisticasAdapter.ViewHolder>() {

    //Crea un objeto con los parámetros de cada vista
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fecha: TextView = view.findViewById(R.id.tv_fechaEstadisticas)
        val juego: TextView = view.findViewById(R.id.tv_juegoEstadisticas)
        val rv_jugadores: RecyclerView = view.findViewById(R.id.rv_jugadoresPartida)
    }

    //Crea la vista para el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartidasEstadisticasAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.partida_estadistica_item_list, parent, false)
        )
    }

    //Devuelve el tamaño de la lista
    override fun getItemCount() = dataSet.size

    //Da valores a los atributos del ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fecha.text = dataSet[position].fecha
        holder.juego.text = dataSet[position].juego.nombre
        holder.rv_jugadores.setHasFixedSize(true)
        holder.rv_jugadores.layoutManager = LinearLayoutManager(context)
        holder.rv_jugadores.adapter = JugadoresJuegoAdapter(context, dataSet[position].jugadores)

    }
}