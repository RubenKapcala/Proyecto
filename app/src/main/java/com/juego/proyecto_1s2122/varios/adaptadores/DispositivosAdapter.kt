package com.juego.proyecto_1s2122.varios.adaptadores

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.varios.MiBluetooth

class DispositivosAdapter (private val dataSet: List<BluetoothDevice>,
) : RecyclerView.Adapter<DispositivosAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tv_nombreDispositivo)
        val adress: TextView = view.findViewById(R.id.tv_aliasDispositivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DispositivosAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.dispositivo_item_list, parent, false)
        )
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nombre.text = dataSet[position].name
        holder.adress.text = dataSet[position].address
        holder.itemView.setOnClickListener{
            val cliente = MiBluetooth.ClientClass(dataSet[position])
            cliente.start()
        }

    }
}