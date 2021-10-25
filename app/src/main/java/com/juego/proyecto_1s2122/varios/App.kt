package com.juego.proyecto_1s2122.varios

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.juego.proyecto_1s2122.modelo.Partida

@SuppressLint("StaticFieldLeak")
object App {
    lateinit var activty: Activity
    var isHost = false
    var partida: Partida? = null
}