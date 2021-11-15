package com.juego.proyecto_1s2122.vistas

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.databinding.ActivityJuegoBinding
import com.juego.proyecto_1s2122.modelo.Partida
import com.juego.proyecto_1s2122.varios.BBDD.DbHelper
import com.juego.proyecto_1s2122.varios.MiBluetooth
import com.juego.proyecto_1s2122.varios.adaptadores.JugadoresJuegoAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class JuegoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJuegoBinding
    private lateinit var partida: Partida

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJuegoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        partida = (intent.getSerializableExtra("partida") as Partida?)!!

        binding.rvPuntuacion.setHasFixedSize(true)
        binding.rvPuntuacion.layoutManager = LinearLayoutManager(this)
        adaptarPuntuacion()

        when(partida.juego){

        }
        if (MiBluetooth.eresServidor){
            binding.btnRojo.setOnClickListener{
                partida.jugadores[0].puntos ++
                MiBluetooth.enviarDatos(
                    MiBluetooth.ListaJugadores(partida.jugadores).toJson(),
                    MiBluetooth.TipoDatoTransmitido.LISTA_JUGADORES
                )
                adaptarPuntuacion()
            }
        }else{
            binding.btnRojo.setOnClickListener{
                val jugador = DbHelper(this).obtenerUsuario()!!
                MiBluetooth.enviarDatos(
                    MiBluetooth.Accion(jugador.nombre, jugador.alias, 1).toJson(),
                    MiBluetooth.TipoDatoTransmitido.ACCION
                )
            }
        }
        binding.btnRojo.isClickable = false
        binding.btnContinuar.setOnClickListener{ finish() }


        object : CountDownTimer(6000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                val segundos = millisUntilFinished /1000
                if (segundos > 3){
                    binding.tvTiempo.text = ""
                }else{
                    binding.tvTiempo.text = segundos.toString()
                }
            }

            override fun onFinish() {
                iniciarJuego()
            }
        }.start()

    }

    private fun iniciarJuego(){
        binding.tvTiempo.text = getText(R.string.go)
        binding.btnRojo.isClickable = true
        binding.btnRojo.setImageResource(R.drawable.animacion_boton_rojo)
        object : CountDownTimer(60000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                val segundos = millisUntilFinished /1000
                binding.tvTiempo.text = segundos.toString()
            }

            override fun onFinish() {
                terminarJuego()
            }
        }.start()
    }

    private fun terminarJuego(){
        binding.tvTiempo.text = getText(R.string.fin)
        binding.btnRojo.setImageResource(R.drawable.boton_rojo_pulsado)
        binding.btnRojo.isClickable = false
        MiBluetooth.desconectarDispositivos()
        binding.btnContinuar.visibility = View.VISIBLE

    }

    private fun adaptarPuntuacion(){
        binding.rvPuntuacion.adapter = JugadoresJuegoAdapter(this, partida.jugadores)

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventListaJugadores(listaJugadores: MiBluetooth.ListaJugadores) {
        partida.jugadores = listaJugadores.jugadores
        adaptarPuntuacion()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventAccion(accion: MiBluetooth.Accion) {
        for (i in partida.jugadores){
            if (i.nombre == accion.nombre && i.alias == accion.alias){
                i.puntos += accion.puntos
                break
            }
        }
        adaptarPuntuacion()
        MiBluetooth.enviarDatos(
            MiBluetooth.ListaJugadores(partida.jugadores).toJson(),
            MiBluetooth.TipoDatoTransmitido.LISTA_JUGADORES
        )
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