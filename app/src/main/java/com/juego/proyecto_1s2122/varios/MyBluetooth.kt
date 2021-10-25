package com.juego.proyecto_1s2122.varios

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Message
import android.widget.Toast
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.modelo.Partida
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@SuppressLint("StaticFieldLeak")
object MyBluetooth {

    const val REQUEST_ENABLE_BT = 1

    const val STATE_LISTENING = 1
    const val STATE_CONNECTING = 2
    const val STATE_CONNECTED = 3
    const val STATE_CONNECTION_FAILED = 4
    const val STATE_MESSAGE_RECEIVED = 5
    const val STATE_MESSAGE_WRITE = 6

    private const val APP_NAME = "Proyecto"
    private val MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66")

    lateinit var appContext: Context
    var eresHost = false
    var sendReceive: SendReceive? = null
    var dispositivoServidor: SendReceive? = null
    var dispositivoCliente: SendReceive? = null
    var dispositivosConectados = mutableListOf<SendReceive>()
    var mensajeBluetooth: String = "peneeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
    var partida: Partida? = null


    private val _bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val bluetoothAdapter: BluetoothAdapter
        get() = _bluetoothAdapter!!

    val estaActivado: Boolean
        get() = bluetoothAdapter.isEnabled

    val esBluetooth: Boolean
        get() = _bluetoothAdapter != null


    private lateinit var broadcastReceiverBusqueda : BroadcastReceiver
    private lateinit var broadcastVisibilidad : BroadcastReceiver
    private lateinit var broadcastInicioBluetoothSeguro : BroadcastReceiver




    @SuppressLint("HandlerLeak")
    val mHandler = object : Handler() {
        override fun handleMessage(msg_type: Message) {
            super.handleMessage(msg_type)
            when (msg_type.what) {

                STATE_MESSAGE_RECEIVED -> {
                    val readbuf = msg_type.obj as ByteArray
                    val stringRecieved = String(readbuf, 0, msg_type.arg1)

                    Toast.makeText(appContext, stringRecieved, Toast.LENGTH_SHORT)
                    //do some task based on recieved string
                }

                STATE_MESSAGE_WRITE -> Toast.makeText(appContext, "mensaje enviado ok", Toast.LENGTH_SHORT).show()

                STATE_CONNECTED -> Toast.makeText(appContext, "Connected", Toast.LENGTH_SHORT).show()

                STATE_LISTENING -> Toast.makeText(appContext, "listening", Toast.LENGTH_SHORT).show()

                STATE_CONNECTING -> Toast.makeText(appContext, "Connecting...", Toast.LENGTH_SHORT).show()

                STATE_CONNECTION_FAILED -> Toast.makeText(appContext, "No socket found", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private class ServerClass : Thread() {
        private var serverSocket: BluetoothServerSocket? = null
        override fun run() {
            var socket: BluetoothSocket? = null
            while (socket == null) {
                try {
                    val message = Message.obtain()
                    message.what = STATE_CONNECTING
                    mHandler.sendMessage(message)
                    socket = serverSocket!!.accept()
                } catch (e: IOException) {
                    e.printStackTrace()
                    val message = Message.obtain()
                    message.what = STATE_CONNECTION_FAILED
                    mHandler.sendMessage(message)
                }
                if (socket != null) {
                    val message = Message.obtain()
                    message.what = STATE_CONNECTED
                    mHandler.sendMessage(message)
                    sendReceive = SendReceive(socket)
                    sendReceive!!.start()
                    break
                }
            }
        }

        init {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    class ClientClass(private val device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null
        override fun run() {
            try {
                socket!!.connect()
                val message = Message.obtain()
                message.what = STATE_CONNECTED
                mHandler.sendMessage(message)
                sendReceive = SendReceive(socket)
                sendReceive!!.start()
            } catch (e: IOException) {
                e.printStackTrace()
                val message = Message.obtain()
                message.what = STATE_CONNECTION_FAILED
                mHandler.sendMessage(message)
            }
        }

        init {
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    class SendReceive(private val bluetoothSocket: BluetoothSocket?) : Thread() {
        private val inputStream: InputStream?
        private val outputStream: OutputStream?
        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            while (true) {
                try {
                    bytes = inputStream!!.read(buffer)
                    mHandler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget()
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
    }
/*
    class ServerClass : Thread() {
        private val serverSocket: BluetoothServerSocket?

        init {
            var tmp: BluetoothServerSocket? = null
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID)
            } catch (e: IOException) {
            }
            serverSocket = tmp

        }

        override fun run() {
            var socket: BluetoothSocket?
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                socket = try {
                    val message = Message.obtain()
                    message.what = STATE_LISTENING
                    mHandler.sendMessage(message)
                    serverSocket!!.accept()

                } catch (e: IOException) {
                    val message = Message.obtain()
                    message.what = STATE_CONNECTION_FAILED
                    mHandler.sendMessage(message)
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    val message = Message.obtain()
                    message.what = STATE_CONNECTED
                    mHandler.sendMessage(message)

                    dispositivoCliente = Send_Recive(socket)
                    dispositivoCliente!!.start()
                    eresHost = true
                    break
                }
            }
        }
    }

    class ClientClass(bluetoothDevice: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket?
        private val device: BluetoothDevice

        init {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            var tmp: BluetoothSocket? = null
            device = bluetoothDevice

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
            }
            socket = tmp
        }

        override fun run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery()
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mHandler.obtainMessage(STATE_CONNECTING).sendToTarget()
                socket!!.connect()
                mHandler.obtainMessage(STATE_CONNECTED).sendToTarget()

                dispositivoServidor = Send_Recive(socket)
                dispositivoServidor!!.start()
                eresHost = false

            } catch (connectException: IOException) {
                // Unable to connect; close the socket and get out
                connectException.printStackTrace()
                mHandler.obtainMessage(STATE_CONNECTION_FAILED).sendToTarget()

                try {
                    socket!!.close()
                } catch (closeException: IOException) {
                }
                return
            }

            // Do work to manage the connection (in a separate thread)
        }

        /** Will cancel an in-progress connection, and close the socket  */
        fun cancel() {
            try {
                socket!!.close()
            } catch (e: IOException) {
            }
        }

    }

    class Send_Recive(private val mmSocket: BluetoothSocket) : Thread() {
        private val inStream: InputStream?
        private val outStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
            }
            inStream = tmpIn
            outStream = tmpOut
        }


        override fun run() {
            val buffer = ByteArray(1024) // buffer store for the stream
            var bytes = 0 // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    mHandler.obtainMessage(STATE_CONNECTION_FAILED)
                    // Read from the InputStream
                    if (inStream != null) {
                        bytes = inStream.read(buffer)
                        // Send the obtained bytes to the UI activity
                        mHandler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget()
                    }
                } catch (e: IOException) {
                    break
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        fun write(bytes: ByteArray?) {

            try {
                outStream?.write(bytes)
                mHandler.obtainMessage(STATE_MESSAGE_WRITE).sendToTarget()
                Toast.makeText(appContext, "paso 1", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
            }
        }
    }


 */
    fun enviarMensaje(texto: String){

        val string: String = texto
        sendReceive!!.write(string.toByteArray())
    }



    fun activar(activity: Activity) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    fun inicioBluetoothSeguro(activity: Activity){

        if (!estaActivado){
            activar(activity)
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)

        broadcastInicioBluetoothSeguro = object: BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                // Solicitamos la informacion extra del intent etiquetada como BluetoothAdapter.EXTRA_STATE
                // El segundo parametro indicara el valor por defecto que se obtendra si el dato extra no existe
                val estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                if (estado == BluetoothAdapter.STATE_OFF) {
                    activar(activity)
                }
            }
        }

        activity.registerReceiver(broadcastInicioBluetoothSeguro, intentFilter)
    }

    interface FuncionFinalVisibilizar{
        fun alFinalizar()
    }

    fun visibilizar(activity: Activity, funcionFinalVisibilizar: FuncionFinalVisibilizar) {
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        broadcastVisibilidad = object: BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {

                when (intent.action) {
                    BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                        when (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)) {

                            BluetoothAdapter.SCAN_MODE_CONNECTABLE, BluetoothAdapter.SCAN_MODE_NONE -> {
                                funcionFinalVisibilizar.alFinalizar()
                            }
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


    interface BuscadorBluetooth{
        fun alIniciar()
        fun alEncontrar(dispositivo: BluetoothDevice)
        fun alTerminar(lista: Set<BluetoothDevice>)
    }

    fun buscarDispisitivos(context: Context, buscador: BuscadorBluetooth) {

        if (!bluetoothAdapter.isDiscovering){
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND)

            broadcastReceiverBusqueda = object: BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                            buscador.alIniciar()
                        }
                        BluetoothDevice.ACTION_FOUND -> {
                            //Extraemos el dispositivo del intent
                            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                            buscador.alEncontrar(device)
                        }
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            buscador.alTerminar(bluetoothAdapter.bondedDevices)
                        }
                    }
                }
            }
            context.registerReceiver(broadcastReceiverBusqueda, intentFilter)
            bluetoothAdapter.startDiscovery()
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
        return bluetoothAdapter.bondedDevices.toList()
    }
}