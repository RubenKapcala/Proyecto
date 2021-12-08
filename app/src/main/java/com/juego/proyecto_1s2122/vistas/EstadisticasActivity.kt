package com.juego.proyecto_1s2122.vistas


import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.databinding.ActivityEstadisticasBinding
import com.juego.proyecto_1s2122.modelo.BBDD.DbHelper
import com.juego.proyecto_1s2122.vistas.adaptadores.JugadoresEstadisticasAdapter
import com.juego.proyecto_1s2122.vistas.adaptadores.PartidasEstadisticasAdapter


class EstadisticasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEstadisticasBinding //Binding con los elementos gráficos
    private val consultas = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstadisticasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarSpinner()

        binding.rvEstadisticas.setHasFixedSize(true)
        binding.rvEstadisticas.layoutManager = LinearLayoutManager(this)

        funcionalidadBotones()

    }

    //Da la funcionalidad a los botones
    private fun funcionalidadBotones() {

        binding.btnAtrasEstadisticas.setOnClickListener{ finish() } //Cierra la activity

        //Carga los datos de la búsqueda dependiendo de la opción elegida
        binding.spEstadisticas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                when(consultas[position]){

                    getString(R.string.jugadores) ->{
                        binding.tvDescripcionEstadistica.text = getString(R.string.todos_jugadores)
                        binding.rvEstadisticas.adapter = JugadoresEstadisticasAdapter(
                                this@EstadisticasActivity,
                                binding,
                                DbHelper(this@EstadisticasActivity).obtenerJugadores())
                    }

                    getString(R.string.partidas) ->{
                        binding.tvDescripcionEstadistica.text = getString(R.string.todas_partidas)
                        binding.rvEstadisticas.adapter = PartidasEstadisticasAdapter(
                                this@EstadisticasActivity,
                                DbHelper(this@EstadisticasActivity).obtenerPartidas())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nada fue seleccionado.
            }
        }
    }

    //Establece el spinner con los datos y los atributos deseados
    private fun configurarSpinner() {
        consultas.add(getString(R.string.nada_seleccionado))
        consultas.add(getString(R.string.jugadores))
        consultas.add(getString(R.string.partidas))
        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, consultas)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spEstadisticas.adapter = dataAdapter    }
}