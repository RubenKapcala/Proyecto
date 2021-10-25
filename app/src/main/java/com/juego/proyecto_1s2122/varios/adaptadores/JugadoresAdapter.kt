package com.juego.proyecto_1s2122.varios.adaptadores

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.modelo.Jugador
import com.juego.proyecto_1s2122.varios.MiBluetooth

class JugadoresAdapter (private val dataSet: List<Jugador>,
) : RecyclerView.Adapter<JugadoresAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tv_nombreDispositivo)
        val adress: TextView = view.findViewById(R.id.tv_direccionDispositivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadoresAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.dispositivo_item_list, parent, false)
        )
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nombre.text = dataSet[position].nombre
        holder.adress.text = dataSet[position].addressDevice
    }

}