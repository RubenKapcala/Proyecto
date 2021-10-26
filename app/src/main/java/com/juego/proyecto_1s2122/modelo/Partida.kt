package com.juego.proyecto_1s2122.modelo

import com.google.gson.Gson
import java.io.Serializable
import java.util.*

data class Partida(
        var jugadores: MutableList<Jugador>,
        val juego: Juego,
        val nJugadores: Int,
        var fecha: String
):Serializable, Transformable

