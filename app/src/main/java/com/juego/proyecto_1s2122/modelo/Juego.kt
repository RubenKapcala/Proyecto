package com.juego.proyecto_1s2122.modelo

import java.io.Serializable

data class Juego(
        val id: Int,
        val nombre: String,
        val puntiacion: Int,
        val descripcion: String
):Serializable
