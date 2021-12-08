package com.juego.proyecto_1s2122.vistas

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.databinding.ActivitySalaEsperaBinding
import com.juego.proyecto_1s2122.modelo.Jugador
import com.juego.proyecto_1s2122.modelo.Partida
import com.juego.proyecto_1s2122.modelo.BBDD.DbHelper
import com.juego.proyecto_1s2122.controlador.MiBluetooth
import com.juego.proyecto_1s2122.vistas.adaptadores.JugadoresAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SalaEsperaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalaEsperaBinding //Binding con los elementos gráficos

    private var partida: Partida? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalaEsperaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvListaJugadores.setHasFixedSize(true)
        binding.rvListaJugadores.layoutManager = LinearLayoutManager(this)

        binding.btnAtras.setOnClickListener { finish() }

        partida = intent.getSerializableExtra("partida") as Partida?


        if (partida != null) {
            visibilizarDispositivo()
            ajustarVistasAPartida(partida!!)
        }

    }

    //Si es el dispositivo servidor pone el bluetooth en modo visible
    private fun visibilizarDispositivo(){
        MiBluetooth.visibilizar(this)
    }

    //Adapta las vistas a la partida
    private fun ajustarVistasAPartida(partida: Partida){
        //Carga los jugadores que se han unido a la partida en el reciclerView
        binding.rvListaJugadores.adapter = JugadoresAdapter(partida.jugadores)
        binding.tvNombreJuego.text = partida.juego.nombre //Carga el nombre del juego
        binding.tvDescripcionJuego.text = partida.juego.descripcion //Carga la descripción del juego
        //Carga el número de jugadores unidos respecto al total
        val texto = "" + partida.jugadores.size + "/" + partida.nJugadores + " " + getString(R.string.jugadores)
        binding.tvNJugadores.text = texto
    }

    //Inicia el juego
    private fun iniciarPartida() {
        val intent = Intent(this, JuegoActivity::class.java)
        intent.putExtra("partida", partida)
        startActivity(intent) //Inicia la activity del juego
        finish() //Cierra la activity

    }

    //Recibe los cambios de estado de la conexión bluetooth
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventEstado(estado: MiBluetooth.Estado) {

        when(estado){
            MiBluetooth.Estado.STATE_LISTENING -> Toast.makeText(this, "Listening", Toast.LENGTH_LONG).show()
            MiBluetooth.Estado.STATE_CONNECTION_FAILED -> Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show()
            MiBluetooth.Estado.STATE_CONNECTED -> Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    //Recibe la información que que un nuevo jugador se ha conectado
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventNuevoJugadorVinculado(jugador: Jugador) {
        partida?.jugadores?.add(jugador) //Añade el jugador a la partida
        ajustarVistasAPartida(partida!!) //Ajusta la vista

        //Envia los datos del estado de la partida al resto de jugadores
        MiBluetooth.enviarDatos(partida!!.toJson(), MiBluetooth.TipoDatoTransmitido.PARTIDA)
        if (partida!!.jugadores.size >= partida!!.nJugadores){

            //Pone visible el mensaje para avisar que comienza la partida
            binding.tvListo.visibility = View.VISIBLE

            //Comienza la cuenta atrás para iniciar el juego
            object : CountDownTimer(2000, 500){
                override fun onTick(millisUntilFinished: Long) {
                    binding.tvListo.text = binding.tvListo.text.toString() + "."
                }

                override fun onFinish() {//Al terminar inicia el juego
                    MiBluetooth.enviarDatos(MiBluetooth.Evento.INICIAR_PARTIDA.toJson(), MiBluetooth.TipoDatoTransmitido.EVENTO)
                    iniciarPartida()
                }
            }.start()

        }else{
            visibilizarDispositivo() //Vuelve a visivilizar el dispositivo para un nuevo jugador
        }
    }

    //Ajusta las vistas a la partida que recibe por bluetooth
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventPartida(partida: Partida) {
        this.partida = partida
        ajustarVistasAPartida(partida)
    }

    //Recibe la petición de iniciar partida
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventIniciarPartida(evento: MiBluetooth.Evento) {
        if (evento == MiBluetooth.Evento.INICIAR_PARTIDA){
            iniciarPartida()
        }
    }

    //Se registra en EventBus
    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        if (partida == null) {
            MiBluetooth.enviarDatos(DbHelper(this).obtenerUsuario()!!.toJson(), MiBluetooth.TipoDatoTransmitido.JUGADOR)
        }
    }

    //Cancela el registro en EventBus
    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

}