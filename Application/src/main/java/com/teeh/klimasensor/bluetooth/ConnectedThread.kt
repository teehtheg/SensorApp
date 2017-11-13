package com.teeh.klimasensor.bluetooth

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
class ConnectedThread(private val service: BluetoothService, private val socket: BluetoothSocket) : Thread() {
    private var mmSocket: BluetoothSocket
    private lateinit var mmInStream: InputStream
    private lateinit var mmOutStream: OutputStream

    private var currentPkg: Int? = null
    private var totalPkgs: Int? = null
    private val isSequence = false

    init {
        mmSocket = socket

        Log.d(BluetoothConstants.TAG, "create ConnectedThread")

        // Get the BluetoothSocket input and output streams
        try {
            mmInStream = mmSocket.inputStream
            mmOutStream = mmSocket.outputStream
        } catch (e: IOException) {
            Log.e(BluetoothConstants.TAG, "temp sockets not created", e)
        }
    }

    override fun run() {
        Log.i(BluetoothConstants.TAG, "BEGIN mConnectedThread")
        val buffer = ByteArray(1024)
        var file: MutableList<String> = ArrayList()
        var bytes: Int
        var isFile = false
        var msgType: String? = null

        // Keep listening to the InputStream while connected
        while (service.state == BluetoothConstants.STATE_CONNECTED) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer)

                var msgContent: String? = null
                val str = String(buffer, 0, bytes)

                if (str.contains(";")) {
                    val msgParts = str.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    msgType = msgParts[0]
                    val msgHeader = msgParts[1]
                    msgContent = msgParts[2]


                    if (msgHeader.contains("/")) {
                        val msgHeaderParts = msgHeader.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        currentPkg = Integer.valueOf(msgHeaderParts[0])
                        totalPkgs = Integer.valueOf(msgHeaderParts[1])
                        Log.i(BluetoothConstants.TAG, "received pkg: $currentPkg/$totalPkgs")
                        val progress = currentPkg.toString() + "/" + totalPkgs
                        service.handler.obtainMessage(BluetoothConstants.MESSAGE_PROGRESS, progress)
                                .sendToTarget()
                        isFile = true
                    }
                } else {
                    isFile = false
                }

                if (isFile && currentPkg!! <= totalPkgs!!) {

                    file.add(msgContent!!)

                    if (currentPkg!!.compareTo(totalPkgs!!) == 0) {

                        if ("u" == msgType) {
                            service.handler.obtainMessage(BluetoothConstants.MESSAGE_UPDATE, file)
                                    .sendToTarget()
                        } else if ("d" == msgType) {
                            service.handler.obtainMessage(BluetoothConstants.MESSAGE_FILE, file)
                                    .sendToTarget()
                        }

                        Log.i(BluetoothConstants.TAG, "File transfer complete!")
                        currentPkg = null
                        totalPkgs = null
                        isFile = false
                        msgType = null
                        file = ArrayList()
                    }

                    // confirm
                    acknowledge()
                } else {
                    if (currentPkg != null && totalPkgs != null && currentPkg!! < totalPkgs!!) {
                        Log.e(BluetoothConstants.TAG, "Could not complete file transfer!")
                    }
                }

                // Send the obtained bytes to the UI Activity
                //mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                //        .sendToTarget();
            } catch (e: IOException) {
                Log.e(BluetoothConstants.TAG, "disconnected", e)
                connectionLost()
                break
            }

        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    fun write(buffer: ByteArray) {
        try {
            mmOutStream.write(buffer)

            // Share the sent message back to the UI Activity
            service.handler.obtainMessage(BluetoothConstants.MESSAGE_WRITE, -1, -1, buffer)
                    .sendToTarget()
        } catch (e: IOException) {
            Log.e(BluetoothConstants.TAG, "Exception during write", e)
        }

    }

    /**
     * Acknowledge received pkg
     */
    fun acknowledge() {
        try {
            mmOutStream.write("ok".toByteArray())
        } catch (e: IOException) {
            Log.e(BluetoothConstants.TAG, "Exception during write", e)
        }

    }

    fun cancel() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
            Log.e(BluetoothConstants.TAG, "close() of connect socket failed", e)
        }

    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        // Send a failure message back to the Activity
        val msg = service.handler.obtainMessage(BluetoothConstants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(BluetoothConstants.TOAST, "Device connection was lost")
        msg.data = bundle
        service.handler.sendMessage(msg)

        // Start the service over to restart listening mode
        service.start()
    }
}
