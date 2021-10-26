package com.juego.proyecto_1s2122.vistas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.juego.proyecto_1s2122.databinding.ActivityJuegoFrotarBinding

class JuegoFrotarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJuegoFrotarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJuegoFrotarBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}