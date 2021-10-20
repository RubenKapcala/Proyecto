package com.juego.proyecto_1s2122.modelo

import android.bluetooth.BluetoothDevice
import java.io.Serializable

data class Jugador(
        val nombre: String,
        var puntos: Int,
        var posicion: Int,
        val addressDevice: String
): Serializable
