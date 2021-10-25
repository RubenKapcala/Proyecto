package com.juego.proyecto_1s2122.vistas

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.databinding.ActivityBuacarPartidaBinding
import com.juego.proyecto_1s2122.varios.MiBluetooth
import com.juego.proyecto_1s2122.varios.adaptadores.DispositivosAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BuscarPartidaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuacarPartidaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuacarPartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvPartidas.setHasFixedSize(true)
        binding.rvPartidas.layoutManager = LinearLayoutManager(this)

        buscarDispositivos()

        binding.btnAtras.setOnClickListener { finish()}
        binding.btnReintentar.setOnClickListener {
            buscarDispositivos()
        }

    }

    private fun buscarDispositivos(){

        val dispositivosEncontrados = ArrayList<BluetoothDevice>()

        MiBluetooth.buscarDispisitivos(this, object: MiBluetooth.BuscarDispositivosInterface{

            override fun alEmpezar() {
                dispositivosEncontrados.clear()
                binding.rvPartidas.adapter?.notifyDataSetChanged()
                binding.pbBuscarDispositivos.visibility = View.VISIBLE
            }

            override fun alEncontrar(dispositivo: BluetoothDevice) {
                if (!dispositivosEncontrados.contains(dispositivo)){
                    dispositivosEncontrados.add(dispositivo)
                    binding.rvPartidas.adapter = DispositivosAdapter(dispositivosEncontrados)
                }
            }

            override fun alTerminar() {
                binding.pbBuscarDispositivos.visibility = View.GONE
            }

            override fun siYaEstaBuscando() {
                Toast.makeText(this@BuscarPartidaActivity, R.string.ya_buscando, Toast.LENGTH_LONG).show()
            }


        })
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventEstado(estado: MiBluetooth.Estado) {

        when(estado){
            MiBluetooth.Estado.STATE_CONNECTING -> Toast.makeText(this, "Connecting", Toast.LENGTH_LONG).show()
            MiBluetooth.Estado.STATE_CONNECTION_FAILED -> Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show()
            MiBluetooth.Estado.STATE_CONNECTED ->{
                Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show()
                val intent = Intent(this, SalaEsperaActivity::class.java)
                startActivity(intent)
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMensaje(bluetoothMensaje: MiBluetooth.MensajeBluetooth) {
        Toast.makeText(this, bluetoothMensaje.mensaje, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        MiBluetooth.bluetoothAdapter?.cancelDiscovery()
        EventBus.getDefault().unregister(this)
    }
}