package com.juego.proyecto_1s2122.vistas

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
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
import java.util.*
import kotlin.properties.Delegates


class JuegoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJuegoBinding
    private lateinit var partida: Partida
    private lateinit var funciones: Funciones
    private var direccionDerecha = true
    private var posicionGlobo_X = 0
    private var posicionGlobo_Y = 0
    private var ancho_iv = 0
    private var alto_iv = 0
    private var globoRojo = false
    private var radioGlobo by Delegates.notNull<Int>()


    interface Funciones{
        fun configurarJuego()
        fun iniciarJuego()
        fun terminarJuego()
        fun cadaTick()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJuegoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        partida = (intent.getSerializableExtra("partida") as Partida?)!!

        binding.rvPuntuacion.setHasFixedSize(true)
        binding.rvPuntuacion.layoutManager = LinearLayoutManager(this)
        adaptarPuntuacion()

        cargarJuego()

        funciones.configurarJuego()

        binding.btnContinuar.setOnClickListener{ finish() }


        //Comienza la cuenta atrás para iniciar el juego
        object : CountDownTimer(6000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                val segundos = millisUntilFinished /1000
                if (segundos > 3){
                    binding.tvTiempo.text = ""
                }else{
                    binding.tvTiempo.text = segundos.toString()
                }
            }

            override fun onFinish() {//Al terminar inicia el juego
                binding.tvTiempo.text = getText(R.string.go)
                iniciarJuego()
            }
        }.start()

    }

    private fun cargarJuego() {
        when(partida.juego.nombre){

            getString(R.string.pulsar) -> {

                binding.btnRojo.visibility = View.VISIBLE

                funciones = object : Funciones {
                    override fun configurarJuego() {
                        binding.btnRojo.setOnClickListener {
                            ganarPuntos(1)
                        }
                        binding.btnRojo.isClickable = false
                    }

                    override fun iniciarJuego() {
                        binding.btnRojo.isClickable = true
                        binding.btnRojo.setImageResource(R.drawable.animacion_boton_rojo)
                    }

                    override fun terminarJuego() {
                        binding.btnRojo.setImageResource(R.drawable.boton_rojo_pulsado)
                        binding.btnRojo.isClickable = false
                    }

                    override fun cadaTick() {}
                }


            }

            getString(R.string.frotar) -> {

                funciones = object : Funciones {
                    @SuppressLint("ClickableViewAccessibility")
                    override fun configurarJuego() {
                        binding.ivPistaFrotar.visibility = View.INVISIBLE
                        binding.ivPistaFrotar.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
                            when (motionEvent.action) {

                                MotionEvent.ACTION_MOVE -> {
                                    if (motionEvent.getY(motionEvent.actionIndex).toInt() in 0..alto_iv) {
                                        val posicion = ((motionEvent.getX(motionEvent.actionIndex) * 100) / ancho_iv).toInt()

                                        when (posicion) {

                                            in 80..100 -> {
                                                if (direccionDerecha) {
                                                    direccionDerecha = false
                                                    binding.ivPistaFrotar.rotation += 180F
                                                } else {
                                                    direccionDerecha = true
                                                    binding.ivPistaFrotar.rotation -= 180F
                                                }
                                                ganarPuntos(1)
                                            }
                                        }
                                    }
                                }
                                MotionEvent.ACTION_UP -> {
                                    ganarPuntos(-2)
                                }

                            }
                            return@OnTouchListener true
                        })
                        binding.ivPistaFrotar.isEnabled = false
                    }

                    override fun iniciarJuego() {
                        binding.ivPistaFrotar.visibility = View.VISIBLE
                        ancho_iv = binding.ivPistaFrotar.width
                        alto_iv = binding.ivPistaFrotar.height
                        binding.ivPistaFrotar.isEnabled = true
                    }

                    override fun terminarJuego() {
                        binding.ivPistaFrotar.isEnabled = false
                        binding.ivPistaFrotar.visibility = View.GONE
                    }

                    override fun cadaTick() {
                        binding.ivPistaFrotar.rotation += 3F
                    }
                }
            }

            getString(R.string.globos) -> {
                binding.ivGlobos.visibility = View.VISIBLE

                funciones = object : Funciones {
                    @SuppressLint("ClickableViewAccessibility")
                    override fun configurarJuego() {
                        binding.ivGlobos.setOnTouchListener(View.OnTouchListener { view, motionEvent ->

                            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                                val posicion_X = motionEvent.getX(motionEvent.actionIndex).toInt()
                                val posicion_Y = motionEvent.getY(motionEvent.actionIndex).toInt()

                                if (posicion_X in posicionGlobo_X - radioGlobo..posicionGlobo_X + radioGlobo) {
                                    if (posicion_Y in posicionGlobo_Y - radioGlobo..posicionGlobo_Y + radioGlobo) {
                                        if (globoRojo) {
                                            ganarPuntos(-3)
                                        } else {
                                            ganarPuntos(1)
                                        }
                                    }
                                }
                                dibujarGlobo()

                            }
                            return@OnTouchListener true
                        })
                        binding.ivGlobos.isEnabled = false
                    }

                    override fun iniciarJuego() {
                        ancho_iv = binding.ivGlobos.width
                        alto_iv = binding.ivGlobos.height
                        radioGlobo = (10*ancho_iv)/100
                        dibujarGlobo()
                        binding.ivGlobos.isEnabled = true
                    }

                    override fun terminarJuego() {
                        binding.ivGlobos.isEnabled = false
                    }

                    override fun cadaTick() {}
                }
            }
        }
    }

    private fun iniciarJuego(){
        funciones.iniciarJuego()
        object : CountDownTimer(60000, 25){
            override fun onTick(millisUntilFinished: Long) {
                val segundos = millisUntilFinished /1000
                binding.tvTiempo.text = segundos.toString()
                funciones.cadaTick()
            }

            override fun onFinish() {
                terminarJuego()
            }
        }.start()
    }

    private fun terminarJuego(){
        binding.tvTiempo.text = getText(R.string.fin)
        funciones.terminarJuego()
        MiBluetooth.desconectarDispositivos()
        DbHelper(this).guardarPartida(partida)
        binding.btnContinuar.visibility = View.VISIBLE

    }

    private fun ganarPuntos(puntos: Int){
        if (MiBluetooth.eresServidor){
            partida.jugadores[0].puntos += puntos
            MiBluetooth.enviarDatos(
                    MiBluetooth.ListaJugadores(partida.jugadores).toJson(),
                    MiBluetooth.TipoDatoTransmitido.LISTA_JUGADORES
            )
        }else{
            val jugador = DbHelper(this).obtenerUsuario()!!
            MiBluetooth.enviarDatos(
                    MiBluetooth.Accion(jugador.nombre, jugador.alias, puntos).toJson(),
                    MiBluetooth.TipoDatoTransmitido.ACCION
            )
        }
        adaptarPuntuacion()
    }

    private fun adaptarPuntuacion(){
        binding.rvPuntuacion.adapter = JugadoresJuegoAdapter(this, partida.jugadores)

    }

    private fun numeroAleatorio(valores: IntRange) : Int {
        val r = Random()
        return r.nextInt(valores.last + 1 - valores.first) + valores.first
    }

    private fun dibujarGlobo(){
        //Crea el canvas
        var bitmapPrincipal = Bitmap.createBitmap(ancho_iv, alto_iv, Bitmap.Config.ARGB_8888)
        bitmapPrincipal = bitmapPrincipal.copy(bitmapPrincipal.config, true)
        val canvas = Canvas(bitmapPrincipal)

        //Elige aleatoriamente si el globo es rojo o verde
        val bitmapSecundario: Bitmap
        if (numeroAleatorio(0..5) == 0){
            globoRojo = true
            bitmapSecundario = BitmapFactory.decodeResource(resources, R.drawable.globo_rojo)
        }else{
            globoRojo = false
            bitmapSecundario = BitmapFactory.decodeResource(resources, R.drawable.globo_verde)
        }

        //Pone el globo en una posición aleatoria dentro del canvas
        posicionGlobo_X = numeroAleatorio(radioGlobo..ancho_iv-radioGlobo)
        posicionGlobo_Y = numeroAleatorio(radioGlobo..alto_iv-radioGlobo)

        //Dibuja el globo en el canvas
        val dest = Rect(posicionGlobo_X - radioGlobo, posicionGlobo_Y - radioGlobo, posicionGlobo_X + radioGlobo, posicionGlobo_Y + (2*radioGlobo))
        canvas.drawBitmap(bitmapSecundario, null, dest, null)

        binding.ivGlobos.setImageBitmap(bitmapPrincipal)
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


