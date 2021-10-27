package com.juego.proyecto_1s2122.modelo

import com.google.gson.Gson
import com.juego.proyecto_1s2122.varios.MiBluetooth
import java.io.Serializable

data class Jugador(
        val nombre: String,
        val alias: String,
        var puntos: Int
): Serializable, Transformable

