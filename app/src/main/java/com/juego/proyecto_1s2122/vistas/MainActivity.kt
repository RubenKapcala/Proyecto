package com.juego.proyecto_1s2122.vistas

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import com.juego.proyecto_1s2122.databinding.ActivityMainBinding
import com.juego.proyecto_1s2122.modelo.BBDD.DbHelper


class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding //Binding con los elementos gráficos


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mostrarBienvenida()
    }

    private fun mostrarBienvenida() {

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val an = metrics.widthPixels.toFloat() // ancho absoluto en pixels

        val al = metrics.heightPixels.toFloat() // alto absoluto en pixels


        //Crea una animación para cada atributo que queremos animar de la imagen de presentación
        val aniRota = ObjectAnimator.ofFloat(binding.imagenPresentacion, "rotation", 0f, 360f, 0f)
        aniRota.duration = 2000
        val aniAlfa = ObjectAnimator.ofFloat(binding.imagenPresentacion, View.ALPHA, 0f, 0.6f, 1f, 1f)
        aniAlfa.duration = 1500
        val aniX = ObjectAnimator.ofFloat(binding.imagenPresentacion, "x", 0f, an * 0.2f, 0f, an * 0.2f, an * 0.05f, an * 0.15f, an * 0.15f)
        aniX.duration = 2500
        val aniY = ObjectAnimator.ofFloat(binding.imagenPresentacion, "y", 0f, al * 0.2f, 0f, al * 0.2f, al * 0.05f, al * 0.15f, al * 0.15f)
        aniY.duration = 2500

        //Las unimos en un AnimationSet para iniciarlas a la vez
        val animSet = AnimatorSet()
        animSet.playTogether(aniRota, aniAlfa, aniX, aniY)
        animSet.start()

        //Programamos el listener para mostrar los elementos principales al acabar la animación
        animSet.addListener(onEnd = {
            val aniX = ObjectAnimator.ofFloat(binding.imagenPresentacion, "x", an * 0.15f, an * 0.1f)
            aniX.duration = 100
            val aniY = ObjectAnimator.ofFloat(binding.imagenPresentacion, "y", al * 0.15f, al * 0.1f)
            aniY.duration = 100

            val animSet = AnimatorSet()
            animSet.playTogether(aniX, aniY)
            animSet.start()

            animSet.addListener(onEnd = {
                binding.imagenCristalRoto.visibility = View.VISIBLE
                val aniAlfa = ObjectAnimator.ofFloat(binding.imagenCristalRoto, View.ALPHA, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f)
                aniAlfa.duration = 1100
                aniAlfa.start()

                aniAlfa.addListener(onEnd = {
                    if (DbHelper(this).obtenerUsuario() != null){
                        //Abre el menú
                        startActivity(Intent(this, MenuActivity::class.java))
                        finish() //Cierra la activity
                    }else{
                        //Abre la activity de crear usuario
                        startActivity(Intent(this, CrearUsuarioActivity::class.java))
                        finish() //Cierra la activity
                    }
                })
            })
        })
    }
}