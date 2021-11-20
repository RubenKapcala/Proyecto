package com.juego.proyecto_1s2122.modelo

import java.io.Serializable

data class Jugador(
        val nombre: String,
        val alias: String,
        var puntos: Int,
        var id: Int? = null
): Serializable, Transformable

