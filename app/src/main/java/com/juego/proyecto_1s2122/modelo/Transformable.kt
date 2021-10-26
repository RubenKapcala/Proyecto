package com.juego.proyecto_1s2122.modelo

import com.google.gson.Gson

interface Transformable {
    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}