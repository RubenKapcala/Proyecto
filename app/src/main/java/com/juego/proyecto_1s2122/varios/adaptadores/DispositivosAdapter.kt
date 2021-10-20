package com.juego.proyecto_1s2122.varios.adaptadores

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.juego.proyecto_1s2122.R

class DispositivosAdapter (private val dataSet: List<BluetoothDevice>,
) : RecyclerView.Adapter<DispositivosAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tv_nombreDispositivo)    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DispositivosAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.dispositivo_item_list, parent, false)
        )
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nombre.text = dataSet[position].name + dataSet[position].address
    }
}