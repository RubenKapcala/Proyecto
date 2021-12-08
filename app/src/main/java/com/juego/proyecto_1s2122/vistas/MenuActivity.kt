package com.juego.proyecto_1s2122.vistas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.databinding.ActivityMenuBinding
import com.juego.proyecto_1s2122.controlador.MiBluetooth

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding //Binding con los elementos gráficos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        comprobarBluetoothActivado()
        funcionalidadBotones()
    }

    //Comprueba si el dispositivo tiene la tecnología bluetooth y si está activa
    private fun comprobarBluetoothActivado() {
        if (!MiBluetooth.esBluetooth){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.sin_bluetooth)
            builder.setMessage(R.string.sin_bluetooth_descripcion)
            builder.setCancelable(false)
            builder.setPositiveButton(R.string.aceptar) { _, _ -> finish() }
            builder.show()
        }else{
            //Si tiene bluetooth comprueba si está activado y de no estarlo intenta activarlo
            MiBluetooth.activarBluetooth(this)
        }
    }

    //Da la funcionalidad a los botones
    private fun funcionalidadBotones() {
        //Mueve al usuario entre las diferentes activities
        binding.btnCrearPartida.setOnClickListener { startActivity(Intent(this, CrearPartidaActivity::class.java)) }
        binding.btnUnirsePartida.setOnClickListener { startActivity(Intent(this, BuscarPartidaActivity::class.java)) }
        binding.btnEstadisticas.setOnClickListener { startActivity(Intent(this, EstadisticasActivity::class.java)) }
    }

    //Controla la respuesta del usuario cuando de le pide activar el bluetooth
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MiBluetooth.REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode != RESULT_OK) {
                MiBluetooth.activarBluetooth(this) //Vuelve a pedir que se active
            }
        }
    }

}