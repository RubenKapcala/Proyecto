package com.juego.proyecto_1s2122.vistas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.juego.proyecto_1s2122.databinding.ActivityJuegoExplotarBinding

class JuegoExplotarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJuegoExplotarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJuegoExplotarBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}