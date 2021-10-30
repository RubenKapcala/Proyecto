package com.juego.proyecto_1s2122.varios

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.modelo.Jugador
import com.juego.proyecto_1s2122.modelo.Partida
import com.juego.proyecto_1s2122.modelo.Transformable
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


object MiBluetooth {

    var REQUEST_ENABLE_BLUETOOTH = 1
    var REQUEST_BLUETOOTH_SCAN_31 = 2
    var REQUEST_BLUETOOTH_SCAN_23 = 3

    enum class Estado{STATE_LISTENING, STATE_CONNECTING, STATE_CONNECTED, STATE_CONNECTION_FAILED}
    enum class Evento: Transformable{INICIAR_PARTIDA, PAUSAR_PARTIDA, FINALIZAR_PARTIDA}
    enum class TipoDatoTransmitido{PARTIDA, LISTA_JUGADORES, JUGADOR, ACCION, EVENTO, TEXTO}
    class Accion(val nombre: String, val alias: String, val puntos: Int): Transformable
    class ListaJugadores(val jugadores: MutableList<Jugador>): Transformable

    var conexionServidor: SendReceive? = null
    var conexionesCliente: MutableList<SendReceive?> = mutableListOf()
    var bluetoothAdapter: BluetoothAdapter? = null
    var eresServidor = false

    private const val APP_NAME = "BTGame"
    private val MY_UUID = UUID.fromString("bf34a98b-1971-4d0d-a010-592c9c009860")
    private const val separador = "€¬"


    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    }

    val esBluetooth: Boolean
        get() = bluetoothAdapter != null
    
    fun activarBluetooth(activity: Activity){
        if (!bluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH)
        }
    }

    class ServerClass : Thread() {
        private var serverSocket: BluetoothServerSocket? = null

        init {
            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                        APP_NAME,
                        MY_UUID
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            eresServidor = true
        }

        override fun run() {
            var socket: BluetoothSocket? = null
            while (socket == null) {
                try {
                    EventBus.getDefault().post(Estado.STATE_LISTENING)
                    socket = serverSocket!!.accept()
                } catch (e: IOException) {
                    e.printStackTrace()
                    EventBus.getDefault().post(Estado.STATE_CONNECTION_FAILED)
                }
                if (socket != null) {
                    EventBus.getDefault().post(Estado.STATE_CONNECTED)
                    val sendService = SendReceive(socket)
                    conexionesCliente.add(sendService)
                    sendService.start()
                    serverSocket?.close()
                    break
                }
            }
        }
    }

    class ClientClass(device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null

        init {
            EventBus.getDefault().post(Estado.STATE_CONNECTING)
            bluetoothAdapter?.cancelDiscovery()
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            eresServidor = false
        }

        override fun run() {
            try {
                socket!!.connect()
                EventBus.getDefault().post(Estado.STATE_CONNECTED)
                conexionServidor = SendReceive(socket)
                conexionServidor!!.start()
            } catch (e: IOException) {
                e.printStackTrace()
                EventBus.getDefault().post(Estado.STATE_CONNECTION_FAILED)
            }
        }
    }

    class SendReceive(bluetoothSocket: BluetoothSocket?) : Thread() {
        private val inputStream: InputStream?
        private val outputStream: OutputStream?

        init {
            var tempIn: InputStream? = null
            var tempOut: OutputStream? = null
            try {
                tempIn = bluetoothSocket!!.inputStream
                tempOut = bluetoothSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            inputStream = tempIn
            outputStream = tempOut
        }

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            while (true) {
                try {
                    bytes = inputStream!!.read(buffer)
                    var tempMsg = String(buffer, 0, bytes)

                    val datosMensaje = tempMsg.split(separador)
                    val tipo = TipoDatoTransmitido.values()[datosMensaje[0].toInt()]
                    tempMsg = datosMensaje[1]
                    //val tipo = TipoDatoTransmitido.values()[tempMsg.subSequence(0, 1).toString().toInt()]
                    //tempMsg = tempMsg.drop(1)
                    Log.i("enviado------->", tempMsg)
                    when(tipo){
                        TipoDatoTransmitido.PARTIDA -> {
                            val partida = Gson().fromJson(tempMsg, Partida::class.java)
                            EventBus.getDefault().post(partida)
                        }
                        TipoDatoTransmitido.LISTA_JUGADORES -> {
                            val lista = Gson().fromJson(tempMsg, ListaJugadores::class.java)
                            EventBus.getDefault().post(lista)
                        }
                        TipoDatoTransmitido.JUGADOR -> {
                            val jugador = Gson().fromJson(tempMsg, Jugador::class.java)
                            EventBus.getDefault().post(jugador)
                        }
                        TipoDatoTransmitido.ACCION -> {
                            val accion = Gson().fromJson(tempMsg, Accion::class.java)
                            EventBus.getDefault().post(accion)
                        }
                        TipoDatoTransmitido.EVENTO -> {
                            val evento = Gson().fromJson(tempMsg, Evento::class.java)
                            EventBus.getDefault().post(evento)
                        }
                        else -> {}
                    }



                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun write(bytes: ByteArray?) {
            try {
                outputStream!!.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun enviarDatos(mensaje: String, tipo: TipoDatoTransmitido){
        val stringBuffer = StringBuffer()
        stringBuffer.append(tipo.ordinal.toString())
        stringBuffer.append(separador)
        stringBuffer.append(mensaje)
        stringBuffer.append(separador)
        if (eresServidor){
            for (i in conexionesCliente){
                i!!.write(stringBuffer.toString().toByteArray())
            }
        }else{
            conexionServidor!!.write(stringBuffer.toString().toByteArray())
        }
    }

    interface BuscarDispositivosInterface{
        fun alEmpezar()
        fun alEncontrar(device: BluetoothDevice)
        fun alTerminar()
        fun siYaEstaBuscando()
    }


    fun buscarDispisitivos(activity: Activity, funciones: BuscarDispositivosInterface) {

        if (!bluetoothAdapter?.isDiscovering!!){

            comprobarPermisos(activity)

            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND)

            val broadcastReceiverBusqueda = object: BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                            funciones.alEmpezar()
                        }
                        BluetoothDevice.ACTION_FOUND -> {
                            //Extraemos el dispositivo del intent
                            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                            val bluetoothClass: BluetoothClass = device.bluetoothClass
                            when (bluetoothClass.majorDeviceClass) {
                                BluetoothClass.Device.Major.PHONE -> {
                                    funciones.alEncontrar(device)
                                }
                            }
                        }
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            funciones.alTerminar()
                        }
                    }
                }
            }
            activity.registerReceiver(broadcastReceiverBusqueda, intentFilter)

        }else{
            funciones.siYaEstaBuscando()
        }

    }

    private fun comprobarPermisos(activity: Activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            @RequiresApi(31)
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        activity, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN
                ), REQUEST_BLUETOOTH_SCAN_31)
            }

            if (bluetoothAdapter!!.startDiscovery()){
            }else{
                Toast.makeText(activity, activity.getText(R.string.permisos_necesasios), Toast.LENGTH_LONG).show()
            }

        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        activity, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                ), REQUEST_BLUETOOTH_SCAN_23)
            }

            if (bluetoothAdapter!!.startDiscovery()){
            }else{
                Toast.makeText(activity, activity.getText(R.string.permisos_necesasios), Toast.LENGTH_LONG).show()
            }
        }else{
            bluetoothAdapter!!.startDiscovery()
        }
    }

    fun visibilizar(activity: Activity) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        activity.startActivity(discoverableIntent)

        val server = ServerClass()
        server.start()
    }
}