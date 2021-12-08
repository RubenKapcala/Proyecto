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
import com.juego.proyecto_1s2122.modelo.BBDD.DbHelper
import com.juego.proyecto_1s2122.controlador.MiBluetooth
import com.juego.proyecto_1s2122.vistas.adaptadores.JugadoresJuegoAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.properties.Delegates


class JuegoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJuegoBinding //Binding con los elementos gráficos

    //Inicializa las variables
    private lateinit var partida: Partida
    private lateinit var funciones: Funciones
    private var direccionDerecha = true
    private var posicionGlobo_X = 0
    private var posicionGlobo_Y = 0
    private var ancho_iv = 0
    private var alto_iv = 0
    private var globoRojo = false
    private var radioGlobo by Delegates.notNull<Int>()

    //Diferentes comportamientos para cada juego
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

    //Dependiendo del minijuego elegido establecerá un comportamiento u otro para la zona de juego
    private fun cargarJuego() {
        when(partida.juego.nombre){

            getString(R.string.pulsar) -> {

                binding.btnRojo.visibility = View.VISIBLE //Pone visible el botón

                funciones = object : Funciones {
                    override fun configurarJuego() {
                        //Da funcionalidad al botón
                        binding.btnRojo.setOnClickListener {
                            ganarPuntos(1) //El jugador gana 1 punto
                        }
                        binding.btnRojo.isClickable = false //Deshabilita el botón
                    }

                    override fun iniciarJuego() {
                        binding.btnRojo.isClickable = true //Habilita el botón
                        binding.btnRojo.setImageResource(R.drawable.animacion_boton_rojo) //Cambia la imagen
                    }

                    override fun terminarJuego() {
                        binding.btnRojo.setImageResource(R.drawable.boton_rojo_pulsado) //Cambia la imagen
                        binding.btnRojo.isClickable = false //Deshabilita el botón
                    }

                    override fun cadaTick() {}
                }


            }

            getString(R.string.frotar) -> {

                funciones = object : Funciones {
                    @SuppressLint("ClickableViewAccessibility")
                    override fun configurarJuego() {
                        binding.ivPistaFrotar.visibility = View.INVISIBLE //Pone invisible la barra
                        binding.ivPistaFrotar.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
                            when (motionEvent.action) {

                                MotionEvent.ACTION_MOVE -> {
                                    //Si el desplazamiento está dentro de la barra
                                    if (motionEvent.getY(motionEvent.actionIndex).toInt() in 0..alto_iv) {
                                        //Calula la posición del dedo en relación al porcentaje del tamaño de la barra
                                        val posicion = ((motionEvent.getX(motionEvent.actionIndex) * 100) / ancho_iv).toInt()

                                        when (posicion) {

                                            //Si está por encima del 80%
                                            in 80..100 -> {
                                                if (direccionDerecha) {
                                                    direccionDerecha = false
                                                    binding.ivPistaFrotar.rotation += 180F
                                                } else {
                                                    direccionDerecha = true
                                                    binding.ivPistaFrotar.rotation -= 180F
                                                }
                                                ganarPuntos(1) //El jugador gana 1 punto
                                            }
                                        }
                                    }
                                }
                                MotionEvent.ACTION_UP -> {
                                    ganarPuntos(-2) //El jugador pierde 2 puntos
                                }

                            }
                            return@OnTouchListener true
                        })
                        binding.ivPistaFrotar.isEnabled = false
                    }

                    override fun iniciarJuego() {
                        binding.ivPistaFrotar.visibility = View.VISIBLE //Pone visible la barra
                        ancho_iv = binding.ivPistaFrotar.width //Guarda el ancho
                        alto_iv = binding.ivPistaFrotar.height //Guarda el alto
                        binding.ivPistaFrotar.isEnabled = true
                    }

                    override fun terminarJuego() {
                        binding.ivPistaFrotar.isEnabled = false
                        binding.ivPistaFrotar.visibility = View.GONE //Quita la vista de la barra
                    }

                    override fun cadaTick() {
                        binding.ivPistaFrotar.rotation += 3F //Rota la barra
                    }
                }
            }

            getString(R.string.globos) -> {
                binding.ivGlobos.visibility = View.VISIBLE //Pone visible el fondo de nubes

                funciones = object : Funciones {
                    @SuppressLint("ClickableViewAccessibility")
                    override fun configurarJuego() {
                        binding.ivGlobos.setOnTouchListener(View.OnTouchListener { view, motionEvent ->

                            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                                val posicion_X = motionEvent.getX(motionEvent.actionIndex).toInt()
                                val posicion_Y = motionEvent.getY(motionEvent.actionIndex).toInt()

                                //Si la pulsación esta a menor distancia del radio de la posición del globo
                                if (posicion_X in posicionGlobo_X - radioGlobo..posicionGlobo_X + radioGlobo) {
                                    if (posicion_Y in posicionGlobo_Y - radioGlobo..posicionGlobo_Y + radioGlobo) {
                                        if (globoRojo) {
                                            ganarPuntos(-3) //El jugador pierde 3 puntos
                                        } else {
                                            ganarPuntos(1) //El jugador gana 1 punto
                                        }
                                    }
                                }
                                dibujarGlobo() //Dibuja un nuevo globo

                            }
                            return@OnTouchListener true
                        })
                        binding.ivGlobos.isEnabled = false
                    }

                    override fun iniciarJuego() {
                        ancho_iv = binding.ivGlobos.width //Guarda el ancho
                        alto_iv = binding.ivGlobos.height //Guarda el alto
                        radioGlobo = (10*ancho_iv)/100 ////Guarda el radio del globo
                        dibujarGlobo() //Dibuja un nuevo globo
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

    //Marca el inicio del minijuego. Vuelve a poner el cronometro en el tiempo establecido y lo inicia
    private fun iniciarJuego(){
        funciones.iniciarJuego() //Inicia el juego seleccionado

        //Inicia la cuenta atrás de 60 segundos
        object : CountDownTimer(60000, 25){
            override fun onTick(millisUntilFinished: Long) {
                val segundos = millisUntilFinished /1000
                binding.tvTiempo.text = segundos.toString() //Muestra el tiempo restante
                funciones.cadaTick() //Realiza las acciones de cada Tick del juego seleccionado
            }

            override fun onFinish() {
                terminarJuego() //Finaliza el juego seleccionado
            }
        }.start()
    }

    //Marca el final del minijuego. Desconecta los dispositivos para dejar libres los sockets y guarda la partida en la BBDD
    private fun terminarJuego(){
        binding.tvTiempo.text = getText(R.string.fin) //Muestra el fin de la partida
        funciones.terminarJuego() //Realiza las acciones de finalizar partida del juego seleccionado
        MiBluetooth.desconectarDispositivos() //Desconecta los dispositivos
        DbHelper(this).guardarPartida(partida) //Guarda la partida en la BBDD
        binding.btnContinuar.visibility = View.VISIBLE //Muestra un botón para volver al menú

    }

    //Manda la información de que los puntos han cambiado al resto de jugadores mediante la clase MiBluetooth
    private fun ganarPuntos(puntos: Int){
        if (MiBluetooth.eresServidor){
            partida.jugadores[0].puntos += puntos //Suma un punto al jugador

            //Envía la lista de jugadores al resto de dispositivos con los datos actualizados
            MiBluetooth.enviarDatos(
                    MiBluetooth.ListaJugadores(partida.jugadores).toJson(),
                    MiBluetooth.TipoDatoTransmitido.LISTA_JUGADORES
            )
        }else{
            val jugador = DbHelper(this).obtenerUsuario()!! //Obtiene los datos del jugador

            //Envía la acción realizada al servidor
            MiBluetooth.enviarDatos(
                    MiBluetooth.Accion(jugador.nombre, jugador.alias, puntos).toJson(),
                    MiBluetooth.TipoDatoTransmitido.ACCION
            )
        }
        adaptarPuntuacion()
    }

    //Refresca la lista de jugadores y su puntuación con los datos actualizados
    private fun adaptarPuntuacion(){
        binding.rvPuntuacion.adapter = JugadoresJuegoAdapter(this, partida.jugadores)

    }

    //Devuelve un número aleatorio dentro del rango que se pasa por variable
    private fun numeroAleatorio(valores: IntRange) : Int {
        val r = Random()
        return r.nextInt(valores.last + 1 - valores.first) + valores.first
    }

    //Se llama solo durante el minijuego Globos. Dibuja un globo en una posición válida y aleatoria dentro de la zona de juego
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

    //Adapta la informacion de la puntuación recivida por bluetooth
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventListaJugadores(listaJugadores: MiBluetooth.ListaJugadores) {
        partida.jugadores = listaJugadores.jugadores
        adaptarPuntuacion()
    }

    //Recoge el evento lanzado en MiBluetooth con la información de que un jugador ha realizado una acción
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventAccion(accion: MiBluetooth.Accion) {
        for (i in partida.jugadores){
            if (i.nombre == accion.nombre && i.alias == accion.alias){
                i.puntos += accion.puntos
                break
            }
        }
        adaptarPuntuacion()

        //Envía la información actualizada al resto de dispositivos
        MiBluetooth.enviarDatos(
                MiBluetooth.ListaJugadores(partida.jugadores).toJson(),
                MiBluetooth.TipoDatoTransmitido.LISTA_JUGADORES
        )
    }

    //Se registra en EventBus
    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)

    }

    //Cancela el registro en EventBus
    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }
}


