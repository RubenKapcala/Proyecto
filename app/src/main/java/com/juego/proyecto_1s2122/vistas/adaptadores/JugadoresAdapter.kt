package com.juego.proyecto_1s2122.vistas.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.modelo.Jugador

class JugadoresAdapter (private val dataSet: List<Jugador>,
) : RecyclerView.Adapter<JugadoresAdapter.ViewHolder>() {

    //Crea un objeto con los parámetros de cada vista
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tv_nombreDispositivo)
        val adress: TextView = view.findViewById(R.id.tv_aliasDispositivo)
    }

    //Crea la vista para el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadoresAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.dispositivo_item_list, parent, false)
        )
    }

    //Devuelve el tamaño de la lista
    override fun getItemCount() = dataSet.size

    //Da valores a los atributos del ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nombre.text = dataSet[position].nombre
        holder.adress.text = dataSet[position].alias
    }

}