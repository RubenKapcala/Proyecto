package com.juego.proyecto_1s2122.vistas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.juego.proyecto_1s2122.databinding.ActivityCrearPartidaBinding
import com.juego.proyecto_1s2122.modelo.Juego
import com.juego.proyecto_1s2122.modelo.Jugador
import com.juego.proyecto_1s2122.modelo.Partida
import com.juego.proyecto_1s2122.varios.MiBluetooth
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*

class CrearPartidaActivity : AppCompatActivity() {

    val JUGADORES_MAXIMOS = 10
    val JUGADORES_MINIMOS = 2
    private lateinit var binding: ActivityCrearPartidaBinding
    private lateinit var listaJuegos: List<Juego>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var juegoElegido = 0
        var nJugadores = 2

        listaJuegos = obtenerListaJuegos()
        ajustarVista(juegoElegido)


        //Damos funcionalidad a los botones

        binding.btnJuegoAnterior.setOnClickListener{
            if (juegoElegido == 0){
                juegoElegido = listaJuegos.size -1
            }else{
                juegoElegido --
            }
            ajustarVista(juegoElegido)
        }

        binding.btnJuegoSuguiente.setOnClickListener{
            if (juegoElegido == listaJuegos.size -1){
                juegoElegido = 0
            }else{
                juegoElegido ++
            }
            ajustarVista(juegoElegido)
        }

        binding.btnMenosJugadores.setOnClickListener{
            if (nJugadores > JUGADORES_MINIMOS)
                nJugadores --
            binding.tvJugadores.text = nJugadores.toString()
        }

        binding.btnMasJugadores.setOnClickListener{
            if (nJugadores < JUGADORES_MAXIMOS)
                nJugadores ++
            binding.tvJugadores.text = nJugadores.toString()
        }


        binding.btnAtras.setOnClickListener {finish()}

        binding.btnConfirmar.setOnClickListener{
            val partida = crearPartida(juegoElegido, nJugadores)
            val intent = Intent(this, SalaEsperaActivity::class.java)
            intent.putExtra("partida", partida)
            startActivity(intent)
        }
    }


    private fun ajustarVista(juegoElegido: Int){
        binding.tvJuego.text = listaJuegos[juegoElegido].nombre
        binding.tvDescripcionJuego.text = listaJuegos[juegoElegido].descripcion
    }


    private fun crearPartida(juegoElegido: Int, jugadores: Int): Partida {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("ES"))
        val currentDate = sdf.format(Date())
        return Partida(mutableListOf(Jugador("la polla", 0, 0, "el que te foca")), listaJuegos[juegoElegido], jugadores, currentDate)
    }

    //esto ya lo ire haciendo


    private fun obtenerListaJuegos(): MutableList<Juego> {
        return mutableListOf(
                Juego(0, "pulsar", 0, "El que pulse más rápido gana"),
                Juego(1, "perseguir", 0, "mata al resto"),
                Juego(2, "frotar", 0, "el dedo mas rápido"),
        )
    }


}