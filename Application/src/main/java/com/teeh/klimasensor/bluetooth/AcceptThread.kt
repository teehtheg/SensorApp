package com.teeh.klimasensor.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException

/**
 * This thread runs while listening for incoming connections. It behaves
 * like a server-side client. It runs until a connection is accepted
 * (or until cancelled).
 */
class AcceptThread(private val service: BluetoothService) : Thread() {

    // The local server socket
    private lateinit var mmServerSocket: BluetoothServerSocket
    private val mSocketType: String

    init {
        var tmp: BluetoothServerSocket? = null
        mSocketType = "InSecure"

        // Create a new listening server socket
        try {
            tmp = service.adapter.listenUsingInsecureRfcommWithServiceRecord(BluetoothConstants.NAME_INSECURE,
                    service.uuid)

        } catch (e: IOException) {
            Log.e(BluetoothConstants.TAG, "Socket Type: " + mSocketType + "listen() failed", e)
        }
        mmServerSocket = tmp!!
    }

    override fun run() {
        Log.d(BluetoothConstants.TAG, "Socket Type: " + mSocketType +
                " BEGIN mAcceptThread" + this)
        name = "AcceptThread" + mSocketType

        var socket: BluetoothSocket? = null

        // Listen to the server socket if we're not connected
        while (service.state != BluetoothConstants.STATE_CONNECTED) {
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mmServerSocket.accept()
            } catch (e: IOException) {
                Log.e(BluetoothConstants.TAG, "Socket Type: " + mSocketType + "accept() failed", e)
                break
            }

            // If a connection was accepted
            if (socket != null) {
                Log.i(BluetoothConstants.TAG, "Socket is created.")
                synchronized(this) {
                    when (service.state) {
                        BluetoothConstants.STATE_LISTEN, BluetoothConstants.STATE_CONNECTING ->
                            // Situation normal. Start the connected thread.
                            service.connected(socket!!, socket!!.remoteDevice,
                                    mSocketType)
                        BluetoothConstants.STATE_NONE, BluetoothConstants.STATE_CONNECTED ->
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket!!.close()
                            } catch (e: IOException) {
                                Log.e(BluetoothConstants.TAG, "Could not close unwanted socket", e)
                            }
                        else -> Log.e(BluetoothConstants.TAG, "Unknown Bluetooth connection state")

                    }
                }
            }
        }
        Log.i(BluetoothConstants.TAG, "END mAcceptThread, socket Type: " + mSocketType)

    }

    fun cancel() {
        Log.d(BluetoothConstants.TAG, "Socket Type" + mSocketType + "cancel " + this)
        try {
            mmServerSocket.close()
        } catch (e: IOException) {
            Log.e(BluetoothConstants.TAG, "Socket Type" + mSocketType + "close() of server failed", e)
        }

    }
}