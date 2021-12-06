package com.juego.proyecto_1s2122.vistas.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.modelo.Jugador
import com.juego.proyecto_1s2122.modelo.BBDD.DbHelper

class JugadoresJuegoAdapter (private val context: Context, private val dataSet: MutableList<Jugador>) : RecyclerView.Adapter<JugadoresJuegoAdapter.ViewHolder>() {

    private var jugador: Jugador? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tv_nombreJuego)
        val alias: TextView = view.findViewById(R.id.tv_aliasJuego)
        val puntos: TextView = view.findViewById(R.id.tv_puntosJuego)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadoresJuegoAdapter.ViewHolder {
        jugador = DbHelper(context).obtenerUsuario()
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.jugador_con_puntos_item_list, parent, false)
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
        holder.alias.text = listaJugadores[position].alias
        holder.puntos.text = listaJugadores[position].puntos.toString()
    }

}