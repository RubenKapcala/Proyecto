package com.juego.proyecto_1s2122.modelo

import java.io.Serializable

data class Partida(
        var jugadores: MutableList<Jugador>,
        val juego: Juego,
        val nJugadores: Int,
        var fecha: String
):Serializable, Transformable

