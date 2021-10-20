package com.juego.proyecto_1s2122.varios

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import com.juego.proyecto_1s2122.R


@SuppressLint("StaticFieldLeak")
object MiBluetooth {

    const val REQUEST_ENABLE_BT = 1
    private lateinit var broadcastReceiverBusqueda : BroadcastReceiver
    private val miContext: Context? by lazy(LazyThreadSafetyMode.NONE) { App.activity }

    private val _adapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = miContext!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val adapter: BluetoothAdapter
        get() = _adapter!!

    val estaActivado: Boolean
        get() = adapter.isEnabled

    val esBluetooth: Boolean
        get() = _adapter != null

    fun activar(activity: Activity) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    fun iniciarConexionSegura(activity: Activity){

        if (!estaActivado){
            activar(activity)
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)

        val broadcastReceiver = object: BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                // Solicitamos la informacion extra del intent etiquetada como BluetoothAdapter.EXTRA_STATE
                // El segundo parametro indicara el valor por defecto que se obtendra si el dato extra no existe
                val estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                if (estado == BluetoothAdapter.STATE_OFF) {
                    activar(activity)
                }
            }
        }

        activity.registerReceiver(broadcastReceiver, intentFilter)
    }

    interface FuncionFinal{
        fun alFinalizar()
    }

    fun visibilizar(activity: Activity, funcionFinal: FuncionFinal) {
         val intentFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
         val broadcastVisibilidad = object: BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {

                when (intent.action) {
                    BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                        when (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)){

                            BluetoothAdapter.SCAN_MODE_CONNECTABLE, BluetoothAdapter.SCAN_MODE_NONE ->{
                                funcionFinal.alFinalizar()
                            }
                        }
                    }
                }
            }
        }
        activity.registerReceiver(broadcastVisibilidad, intentFilter)

        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        activity.startActivity(discoverableIntent)
    }


    interface BuscadorBluetooth{
        fun alEncontrar(dispositivo: BluetoothDevice)
        fun alTerminar()
    }

    fun buscarDispisitivos(context: Context, buscador: BuscadorBluetooth) {

        if (!adapter.isDiscovering){
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND)

            broadcastReceiverBusqueda = object: BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        BluetoothDevice.ACTION_FOUND -> {
                            //Extraemos el dispositivo del intent
                            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                            buscador.alEncontrar(device)
                        }
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            buscador.alTerminar()
                        }
                    }
                }
            }
            context.registerReceiver(broadcastReceiverBusqueda, intentFilter)
            adapter.startDiscovery()
        }else{
            Toast.makeText(context, R.string.ya_buscando, Toast.LENGTH_LONG).show()
        }

    }

    fun finalizarBusqueda(context: Context) {
        try {
            context.unregisterReceiver(broadcastReceiverBusqueda)
        } catch (e: Exception) { }
    }


    fun listarPareados(): List<BluetoothDevice> {
        return adapter.bondedDevices.toList()
    }

}