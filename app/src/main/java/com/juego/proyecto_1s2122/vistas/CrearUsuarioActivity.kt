package com.juego.proyecto_1s2122.vistas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.juego.proyecto_1s2122.databinding.ActivityCrearUsuarioBinding
import com.juego.proyecto_1s2122.varios.BBDD.DbHelper

class CrearUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAceptar.setOnClickListener{
            DbHelper(this).guardarUsuario(binding.etNombre.text.toString(), binding.etAlias.text.toString())
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
    }
}