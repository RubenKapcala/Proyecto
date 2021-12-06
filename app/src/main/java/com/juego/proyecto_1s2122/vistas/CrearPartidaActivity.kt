package com.juego.proyecto_1s2122.vistas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.juego.proyecto_1s2122.databinding.ActivityCrearPartidaBinding
import com.juego.proyecto_1s2122.modelo.Juego
import com.juego.proyecto_1s2122.modelo.Partida
import com.juego.proyecto_1s2122.modelo.BBDD.DbHelper
import java.text.SimpleDateFormat
import java.util.*

class CrearPartidaActivity : AppCompatActivity() {

    val JUGADORES_MAXIMOS = 8
    val JUGADORES_MINIMOS = 2
    private var nJugadores = JUGADORES_MINIMOS
    private var juegoElegido = 0
    private lateinit var binding: ActivityCrearPartidaBinding
    private lateinit var listaJuegos: List<Juego>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPartidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listaJuegos = DbHelper(this).obtenerJuegos()
        ajustarVista(juegoElegido)

        funcionalidadBotones()
    }

    private fun funcionalidadBotones() {

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
            finish()
        }
    }

    private fun ajustarVista(juegoElegido: Int){
        binding.tvJuego.text = listaJuegos[juegoElegido].nombre
        binding.tvDescripcionJuego.text = listaJuegos[juegoElegido].descripcion
    }

    private fun crearPartida(juegoElegido: Int, jugadores: Int): Partida {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("ES"))
        val currentDate = sdf.format(Date())
        return Partida(mutableListOf(DbHelper(this).obtenerUsuario()!!), listaJuegos[juegoElegido], jugadores, currentDate)
    }

}