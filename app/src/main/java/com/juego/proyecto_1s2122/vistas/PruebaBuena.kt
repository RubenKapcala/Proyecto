package com.juego.proyecto_1s2122.vistas

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.widget.ArrayAdapter;
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.databinding.ActivityPruebaBuenaBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class PruebaBuena : AppCompatActivity() {

    companion object{
        const val REQUEST_ENABLE_BT = 1
        val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val MESSAGE_READ = 0
        const val MESSAGE_WRITE = 1
        const val CONNECTING = 2
        const val CONNECTED = 3
        const val NO_SOCKET_FOUND = 4

        var bluetoothAdapter: BluetoothAdapter? = null
        var mHandler: Handler? = null

        var bluetooth_message = "jooooder"

    }

    private var lv_paired_devices: ListView? = null
    private var set_pairedDevices: Set<BluetoothDevice>? = null
    private var adapter_paired_devices: ArrayAdapter<String>? = null
    private lateinit var binding: ActivityPruebaBuenaBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPruebaBuenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inicializarHandler()
        initialize_layout()
        initialize_bluetooth()
        start_accepting_connection()
        initialize_clicks()
    }

    fun inicializarHandler(){
        @SuppressLint("HandlerLeak")
        mHandler = object : Handler() {
            override fun handleMessage(msg_type: Message) {
                super.handleMessage(msg_type)
                when (msg_type.what) {

                    MESSAGE_READ -> {
                        val readbuf = msg_type.obj as ByteArray
                        val string_recieved = String(readbuf)

                        Toast.makeText(applicationContext, string_recieved, Toast.LENGTH_SHORT)
                        //do some task based on recieved string
                    }

                    MESSAGE_WRITE -> {
                        if (msg_type.obj != null) {
                            val connectedThread = SendRecive(msg_type.obj as BluetoothSocket)
                            connectedThread.write(bluetooth_message.toByteArray())
                        }
                    }

                    CONNECTED -> Toast.makeText(applicationContext, "Connected", Toast.LENGTH_SHORT).show()

                    CONNECTING -> Toast.makeText(applicationContext, "Connecting...", Toast.LENGTH_SHORT).show()

                    NO_SOCKET_FOUND -> Toast.makeText(applicationContext, "No socket found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun initialize_layout() {
        adapter_paired_devices = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item)
        binding.lvPairedDevices.adapter = adapter_paired_devices
    }

    fun start_accepting_connection() {
        //call this on button click as suited by you
        val acceptThread = ServerClass()
        acceptThread.start()
        Toast.makeText(applicationContext, "accepting", Toast.LENGTH_SHORT).show()
    }

    fun initialize_clicks() {
        lv_paired_devices?.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val objects: Array<Any> = set_pairedDevices!!.toTypedArray()
            val device = objects[position] as BluetoothDevice
            val connectThread = ClientClass(device)
            connectThread.start()
            Toast.makeText(applicationContext, "device choosen " + device.name, Toast.LENGTH_SHORT).show()
        }
    }

    fun initialize_bluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(
                applicationContext,
                "Your Device doesn't support bluetooth. you can play as Single player",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

        //Add these permisions before
//        <uses-permission android:name="android.permission.BLUETOOTH" />
//        <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
//        <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
//        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
        if (!bluetoothAdapter?.isEnabled!!) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            set_pairedDevices = bluetoothAdapter?.bondedDevices
            if (set_pairedDevices?.size!! > 0) {
                for (device in set_pairedDevices!!) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    adapter_paired_devices!!.add(device.name + "\n" + device.address)
                }
            }
        }
    }


    class ServerClass : Thread() {
        private val serverSocket: BluetoothServerSocket?

        init {
            var tmp: BluetoothServerSocket? = null
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("NAME", MY_UUID)
            } catch (e: IOException) {
            }
            serverSocket = tmp
        }

        override fun run() {
            var socket: BluetoothSocket?
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                socket = try {
                    serverSocket!!.accept()
                } catch (e: IOException) {
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    mHandler?.obtainMessage(CONNECTED)?.sendToTarget()
                }
            }
        }
    }

    private class ClientClass(device: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmDevice: BluetoothDevice

        init {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            var tmp: BluetoothSocket? = null
            mmDevice = device

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
            }
            mmSocket = tmp
        }

        override fun run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter?.cancelDiscovery()
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mHandler?.obtainMessage(CONNECTING)?.sendToTarget()
                mmSocket!!.connect()
            } catch (connectException: IOException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket!!.close()
                } catch (closeException: IOException) {
                }
                return
            }

            // Do work to manage the connection (in a separate thread)
//            bluetooth_message = "Initial message"
//            mHandler.obtainMessage(MESSAGE_WRITE,mmSocket).sendToTarget();
        }

        /** Will cancel an in-progress connection, and close the socket  */
        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
            }
        }

    }

    private class SendRecive(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

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
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }


        override fun run() {
            val buffer = ByteArray(2) // buffer store for the stream
            var bytes = 0 // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    if (mmInStream != null) {
                        bytes = mmInStream.read(buffer)
                    }
                    // Send the obtained bytes to the UI activity
                    mHandler?.obtainMessage(MESSAGE_READ, bytes, -1, buffer)?.sendToTarget()
                } catch (e: IOException) {
                    break
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        fun write(bytes: ByteArray?) {
            try {
                mmOutStream?.write(bytes)
            } catch (e: IOException) {
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

}