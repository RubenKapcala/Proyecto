package com.juego.proyecto_1s2122.vistas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.juego.proyecto_1s2122.databinding.ActivitySalaEsperaBinding
import com.juego.proyecto_1s2122.modelo.Jugador
import com.juego.proyecto_1s2122.modelo.Partida
import com.juego.proyecto_1s2122.varios.MiBluetooth
import com.juego.proyecto_1s2122.varios.adaptadores.JugadoresAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SalaEsperaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalaEsperaBinding

    private var partida: Partida? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalaEsperaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvListaJugadores.setHasFixedSize(true)
        binding.rvListaJugadores.layoutManager = LinearLayoutManager(this)

        binding.btnAtras.setOnClickListener { finish() }
        binding.btnConfirmar.setOnClickListener{ startActivity(Intent(this, JuegoActivity::class.java)) }

        partida = intent.getSerializableExtra("partida") as Partida?


        if (partida != null) {
            visibilizarDispositivo()
            binding.rvListaJugadores.adapter = JugadoresAdapter(partida!!.jugadores)
        }

    }

    fun visibilizarDispositivo(){
        MiBluetooth.visibilizar(this, object : MiBluetooth.VisibilizarDispositivoInterface {

            override fun alEmpezar() {}

            override fun alTerminar() {
                Toast.makeText(this@SalaEsperaActivity, partida!!.nJugadores.toString(), Toast.LENGTH_LONG).show()

            }

        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventEstado(estado: MiBluetooth.Estado) {

        when(estado){
            MiBluetooth.Estado.STATE_LISTENING -> Toast.makeText(this, "Listening", Toast.LENGTH_LONG).show()
            MiBluetooth.Estado.STATE_CONNECTION_FAILED -> Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show()
            MiBluetooth.Estado.STATE_CONNECTED -> {
                Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show()

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventNuevoDispositivoVinculado(nuevoDispositivo: MiBluetooth.NuevoDispositivoVinculado) {
        if (partida!!.jugadores.size < partida!!.nJugadores -1){
            partida?.jugadores?.add(Jugador(nuevoDispositivo.dispositivo.name, 0, 0, nuevoDispositivo.dispositivo.address))
            binding.rvListaJugadores.adapter = JugadoresAdapter(partida!!.jugadores)
            val gson = Gson()
            val json: String = gson.toJson(partida)
            MiBluetooth.enviarDatos(json)
            visibilizarDispositivo()
        }else{
            val intent = Intent(this, JuegoActivity::class.java)
            startActivity(intent)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMensaje(bluetoothMensaje: MiBluetooth.MensajeBluetooth) {
        Toast.makeText(this, bluetoothMensaje.mensaje, Toast.LENGTH_LONG).show()
        val partida = Gson().fromJson(bluetoothMensaje.mensaje, Partida::class.java)
        binding.rvListaJugadores.adapter = JugadoresAdapter(partida!!.jugadores)

    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }
}