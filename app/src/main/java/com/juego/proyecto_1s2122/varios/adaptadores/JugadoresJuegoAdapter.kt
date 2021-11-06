package com.juego.proyecto_1s2122.varios.adaptadores

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.modelo.Jugador
import com.juego.proyecto_1s2122.varios.BBDD.DbHelper

class JugadoresJuegoAdapter (private val context: Context, private val dataSet: MutableList<Jugador>) : RecyclerView.Adapter<JugadoresJuegoAdapter.ViewHolder>() {

    private var jugador: Jugador? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tv_nombre)
        val adress: TextView = view.findViewById(R.id.tv_alias)
        val puntos: TextView = view.findViewById(R.id.tv_puntos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadoresJuegoAdapter.ViewHolder {
        jugador = DbHelper(context).obtenerUsuario()
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.jugador_item_list, parent, false)
        )
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val listaJugadores = mutableListOf<Jugador>()
        listaJugadores.addAll(dataSet)
        listaJugadores.sortWith { o1, o2 -> o2.puntos.compareTo(o1.puntos) }
        if (listaJugadores[position].nombre == jugador?.nombre
            && listaJugadores[position].alias == jugador?.alias){
            holder.nombre.setTextColor(context.resources.getColor(R.color.colorred))
        }
        holder.nombre.text = listaJugadores[position].nombre
        holder.adress.text = listaJugadores[position].alias
        holder.puntos.text = listaJugadores[position].puntos.toString()
    }

}