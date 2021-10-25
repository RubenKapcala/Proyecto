package com.juego.proyecto_1s2122.varios

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

object MiBluetooth {
    
    var REQUEST_ENABLE_BLUETOOTH = 1

    enum class Estado{STATE_LISTENING, STATE_CONNECTING, STATE_CONNECTED, STATE_CONNECTION_FAILED}
    enum class Evento{INICIAR_PARTIDA}
    enum class TipoDatoTransmitido{PARTIDA, EVENTO, TEXTO}
    class MensajeBluetooth(val mensaje: String)
    class NuevoDispositivoVinculado(val dispositivo: BluetoothDevice)

    var conexionServidor: SendReceive? = null
    var conexionesCliente: MutableList<SendReceive?> = mutableListOf()
    var bluetoothAdapter: BluetoothAdapter? = null
    private var eresServidor = false

    private const val APP_NAME = "BTChat"
    private val MY_UUID = UUID.fromString("bf34a98b-1971-4d0d-a010-592c9c009860")



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
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID)
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
                    EventBus.getDefault().post(NuevoDispositivoVinculado(socket.remoteDevice))
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
                    val tempMsg = String(buffer, 0, bytes)
                    EventBus.getDefault().post(MensajeBluetooth(tempMsg))

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

    fun enviarDatos(mensaje: String){
        if (eresServidor){
            for (i in conexionesCliente){
                i!!.write(mensaje.toByteArray())
            }
        }else{
            conexionServidor!!.write(mensaje.toByteArray())
        }
    }

    fun obtenerEnlazados(): Set<BluetoothDevice>?{
        return bluetoothAdapter!!.bondedDevices
    }

    interface BuscarDispositivosInterface{
        fun alEmpezar()
        fun alEncontrar(device: BluetoothDevice)
        fun alTerminar()
        fun siYaEstaBuscando()
    }

    fun buscarDispisitivos(activity: Activity, funciones: BuscarDispositivosInterface) {

        if (!bluetoothAdapter?.isDiscovering!!){
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
                            funciones.alEncontrar(device)

                        }
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            funciones.alTerminar()
                        }
                    }
                }
            }
            activity.registerReceiver(broadcastReceiverBusqueda, intentFilter)
            bluetoothAdapter!!.startDiscovery()
        }else{
            funciones.siYaEstaBuscando()
        }

    }

    interface VisibilizarDispositivoInterface{
        fun alEmpezar()
        fun alTerminar()
    }

    fun visibilizar(activity: Activity, funcion: VisibilizarDispositivoInterface) {
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        val broadcastVisibilidad = object: BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {

                when (intent.action) {
                    BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                        if (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR) == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                            funcion.alEmpezar()
                        } else {
                            funcion.alTerminar()
                        }
                    }
                }
            }
        }
        activity.registerReceiver(broadcastVisibilidad, intentFilter)

        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        activity.startActivity(discoverableIntent)

        val server = ServerClass()
        server.start()
    }
}