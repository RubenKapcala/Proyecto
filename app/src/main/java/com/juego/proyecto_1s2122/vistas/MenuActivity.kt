package com.juego.proyecto_1s2122.vistas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.databinding.ActivityMenuBinding
import com.juego.proyecto_1s2122.varios.BBDD.DbHelper
import com.juego.proyecto_1s2122.varios.MiBluetooth

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (!MiBluetooth.esBluetooth){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.sin_bluetooth)
            builder.setMessage(R.string.sin_bluetooth_descripcion)
            builder.setCancelable(false)
            builder.setPositiveButton(R.string.aceptar) { _, _ -> finish() }
            builder.show()
        }else{
            MiBluetooth.activarBluetooth(this)
        }


        binding.btnCrearPartida.setOnClickListener { startActivity(Intent(this, CrearPartidaActivity::class.java)) }
        binding.btnUnirsePartida.setOnClickListener { startActivity(Intent(this, BuscarPartidaActivity::class.java)) }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MiBluetooth.REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode != RESULT_OK) {
                MiBluetooth.activarBluetooth(this)
            }
        }
    }

}