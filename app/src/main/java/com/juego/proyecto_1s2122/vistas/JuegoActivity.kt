package com.juego.proyecto_1s2122.vistas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.juego.proyecto_1s2122.databinding.ActivityJuegoBinding

class JuegoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJuegoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJuegoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}