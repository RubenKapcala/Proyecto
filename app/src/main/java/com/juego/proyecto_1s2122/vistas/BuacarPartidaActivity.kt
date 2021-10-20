package com.juego.proyecto_1s2122.vistas

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.juego.proyecto_1s2122.databinding.ActivityBuacarPartidaBinding
import com.juego.proyecto_1s2122.varios.MiBluetooth
import com.juego.proyecto_1s2122.varios.adaptadores.DispositivosAdapter

class BuacarPartidaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuacarPartidaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuacarPartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvPartidas.setHasFixedSize(true)
        binding.rvPartidas.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.rvPartidas.layoutManager = LinearLayoutManager(this)

        buscarDispositivos()

        binding.btnAtras.setOnClickListener { finish()}
        binding.btnReintentar.setOnClickListener {
            buscarDispositivos()
        }

    }

    private fun buscarDispositivos(){

        val dispositivosEncontrados = ArrayList<BluetoothDevice>()

        MiBluetooth.buscarDispisitivos(this, object: MiBluetooth.BuscadorBluetooth{

            override fun alEncontrar(dispositivo: BluetoothDevice) {
                Toast.makeText(this@BuacarPartidaActivity, "busqueda", Toast.LENGTH_LONG).show()
                dispositivosEncontrados.add(dispositivo)
                binding.rvPartidas.adapter = DispositivosAdapter(dispositivosEncontrados)
            }

            override fun alTerminar() {
                Toast.makeText(this@BuacarPartidaActivity, "Terminada busqueda", Toast.LENGTH_LONG).show()
            }

        })
    }


    override fun onPause() {
        super.onPause()
        MiBluetooth.finalizarBusqueda(this)
    }
}