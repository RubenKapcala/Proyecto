package com.juego.proyecto_1s2122.vistas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import androidx.recyclerview.widget.LinearLayoutManager
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.databinding.ActivityJuegoPulsarBinding
import com.juego.proyecto_1s2122.modelo.Partida
import com.juego.proyecto_1s2122.varios.BBDD.DbHelper
import com.juego.proyecto_1s2122.varios.MiBluetooth
import com.juego.proyecto_1s2122.varios.adaptadores.JugadoresAdapter
import com.juego.proyecto_1s2122.varios.adaptadores.JugadoresJuegoAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class JuegoPulsarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJuegoPulsarBinding
    private lateinit var partida: Partida
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJuegoPulsarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        partida = (intent.getSerializableExtra("partida") as Partida?)!!

        binding.rvPuntuacion.setHasFixedSize(true)
        binding.rvPuntuacion.layoutManager = LinearLayoutManager(this)
        binding.rvPuntuacion.adapter = JugadoresJuegoAdapter(partida.jugadores)

        if (MiBluetooth.eresServidor){
            binding.btnRojo.setOnClickListener{
                partida.jugadores[0].puntos ++
                MiBluetooth.enviarDatos(MiBluetooth.ListaJugadores(partida.jugadores).toJson(), MiBluetooth.TipoDatoTransmitido.LISTA_JUGADORES)
                binding.rvPuntuacion.adapter = JugadoresJuegoAdapter(partida.jugadores)
            }
        }else{
            binding.btnRojo.setOnClickListener{
                val jugador = DbHelper(this).obtenerUsuario()!!
                MiBluetooth.enviarDatos(MiBluetooth.Accion(jugador.nombre, jugador.alias, 1).toJson(), MiBluetooth.TipoDatoTransmitido.ACCION)
            }
        }

        binding.btnRojo.isClickable = false

        val tiempoInicio = object : CountDownTimer(6000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                val segundos = millisUntilFinished /1000
                if (segundos > 3){
                    binding.tvTiempo.text = ""
                }else{
                    binding.tvTiempo.text = segundos.toString()
                }
            }

            override fun onFinish() {
                binding.tvTiempo.text = getText(R.string.go)
                binding.btnRojo.isClickable = true
                binding.btnRojo.setImageResource(R.drawable.animacion_boton_rojo)
                val tiempoFin = object : CountDownTimer(60000, 1000){
                    override fun onTick(millisUntilFinished: Long) {
                        val segundos = millisUntilFinished /1000
                        binding.tvTiempo.text = segundos.toString()
                    }

                    override fun onFinish() {
                        binding.tvTiempo.text = getText(R.string.fin)
                        binding.btnRojo.setImageResource(R.drawable.boton_rojo_pulsado)
                        binding.btnRojo.isClickable = false
                    }
                }
                tiempoFin.start()
            }
        }
        tiempoInicio.start()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventListaJugadores(listaJugadores: MiBluetooth.ListaJugadores) {
        binding.rvPuntuacion.adapter = JugadoresJuegoAdapter(listaJugadores.jugadores)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventAccion(accion: MiBluetooth.Accion) {
        for (i in partida.jugadores){
            if (i.nombre == accion.nombre && i.alias == accion.alias){
                i.puntos += accion.puntos
                break
            }
        }

        binding.rvPuntuacion.adapter = JugadoresJuegoAdapter(partida.jugadores)
        MiBluetooth.enviarDatos(MiBluetooth.ListaJugadores(partida.jugadores).toJson(), MiBluetooth.TipoDatoTransmitido.LISTA_JUGADORES)
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