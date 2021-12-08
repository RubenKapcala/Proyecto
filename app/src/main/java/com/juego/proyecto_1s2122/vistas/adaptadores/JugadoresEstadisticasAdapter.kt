package com.juego.proyecto_1s2122.vistas.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.databinding.ActivityEstadisticasBinding
import com.juego.proyecto_1s2122.modelo.Jugador
import com.juego.proyecto_1s2122.modelo.BBDD.DbHelper

class JugadoresEstadisticasAdapter (val context: Context, val binding: ActivityEstadisticasBinding, private val dataSet: List<Jugador>
) : RecyclerView.Adapter<JugadoresEstadisticasAdapter.ViewHolder>() {

    //Crea un objeto con los parámetros de cada vista
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tv_nombreEstadisticas)
    }

    //Crea la vista para el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadoresEstadisticasAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.jugador_estadistica_item_list, parent, false)
        )
    }

    //Devuelve el tamaño de la lista
    override fun getItemCount() = dataSet.size

    //Da valores a los atributos del ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var texto = ""
        texto += dataSet[position].nombre + "(" + dataSet[position].alias + ")"
        holder.nombre.text = texto

        //Añade un onClickListener a cada vista
        holder.itemView.setOnClickListener{
            //Pone la selección del spinner en el primer lugar
            binding.spEstadisticas.setSelection(0)
            //Cambia el texto de la descripción de la búsqueda
            binding.tvDescripcionEstadistica.text = context.getString(R.string.partidas_de_jugador) + " " + dataSet[position].nombre

            binding.rvEstadisticas.setHasFixedSize(true)
            binding.rvEstadisticas.layoutManager = LinearLayoutManager(context)

            //Realiza una búsqueda en la BBDD de las partidas jugadas por ese jugador
            binding.rvEstadisticas.adapter = PartidasEstadisticasAdapter(context, DbHelper(context).obtenerPartidasDeJugador(position+1))
        }

    }
}