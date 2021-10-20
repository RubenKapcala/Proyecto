package com.juego.proyecto_1s2122.vistas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.juego.proyecto_1s2122.databinding.ActivitySalaEsperaBinding
import com.juego.proyecto_1s2122.varios.App
import com.juego.proyecto_1s2122.varios.MiBluetooth

class SalaEsperaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalaEsperaBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalaEsperaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnAtras.setOnClickListener { finish() }
        binding.btnConfirmar.setOnClickListener{ startActivity(Intent(this, JuegoActivity::class.java)) }

        Toast.makeText(this, App.partida!!.nJugadores.toString(), Toast.LENGTH_LONG).show()



        if (App.partida != null) {
            visibilizarDispositivo()
        }

    }

    fun visibilizarDispositivo(){
        MiBluetooth.visibilizar(this, object: MiBluetooth.FuncionFinal{
            override fun alFinalizar() {
                Toast.makeText(this@SalaEsperaActivity, App.partida!!.nJugadores.toString(), Toast.LENGTH_LONG).show()

                if (App.partida!!.jugadores.size < App.partida!!.nJugadores){
                    visibilizarDispositivo()
                }
            }

        })
    }


}