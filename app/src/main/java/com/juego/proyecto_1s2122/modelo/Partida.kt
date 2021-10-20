package com.juego.proyecto_1s2122.modelo

import java.io.Serializable
import java.util.*

data class Partida(
        var jugadores: MutableList<Jugador>,
        val juego: Juego,
        val nJugadores: Int,
        var tiempo: Date
):Serializable
