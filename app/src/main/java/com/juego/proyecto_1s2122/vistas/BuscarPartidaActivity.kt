package com.juego.proyecto_1s2122.vistas

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        funcionalidadBotones()
        buscarDispositivos()

    }

    private fun funcionalidadBotones() {
        binding.btnAtras.setOnClickListener { finish()}
        binding.btnReintentar.setOnClickListener { buscarDispositivos() }
    }


    private fun buscarDispositivos(){

        val dispositivosEncontrados = ArrayList<BluetoothDevice>()


        MiBluetooth.buscarDispisitivos(this, object: MiBluetooth.BuscarDispositivosInterface{

            override fun alEmpezar() {
                dispositivosEncontrados.clear()
                binding.rvPartidas.adapter?.notifyDataSetChanged()
                binding.pbBuscarDispositivos.visibility = View.VISIBLE
            }

            override fun alEncontrar(device: BluetoothDevice) {
                if (!dispositivosEncontrados.contains(device)){
                    dispositivosEncontrados.add(device)
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
            MiBluetooth.Estado.STATE_CONNECTING -> Toast.makeText(
                this,
                "Connecting",
                Toast.LENGTH_LONG
            ).show()
            MiBluetooth.Estado.STATE_CONNECTION_FAILED -> Toast.makeText(
                this,
                "Connection Failed",
                Toast.LENGTH_LONG
            ).show()
            MiBluetooth.Estado.STATE_CONNECTED -> {
                Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show()
                val intent = Intent(this, SalaEsperaActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> {}
        }
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MiBluetooth.REQUEST_BLUETOOTH_SCAN_23 -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getText(R.string.permiso_aceptado), Toast.LENGTH_SHORT).show()
                    buscarDispositivos()
                } else {
                    Toast.makeText(this, getText(R.string.permisos_necesasios), Toast.LENGTH_SHORT).show()
                }
            }
        }
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