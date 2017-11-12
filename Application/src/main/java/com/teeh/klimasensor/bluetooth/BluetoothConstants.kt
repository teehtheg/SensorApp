package com.teeh.klimasensor.bluetooth

import java.util.*

/**
 * Created by david on 21.10.2017.
 */
object BluetoothConstants {
    val TAG = "BluetoothService"

    // Name for the SDP record when creating server socket
    val NAME_SECURE = "BluetoothSecure"

    // Unique UUID for this application
    val MY_UUID_SECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    // Constants that indicate the current connection state
    val STATE_NONE = 0       // we're doing nothing
    val STATE_LISTEN = 1     // now listening for incoming connections
    val STATE_CONNECTING = 2 // now initiating an outgoing connection
    val STATE_CONNECTED = 3  // now connected to a remote device

    // Message types sent from the BluetoothService Handler
    val MESSAGE_STATE_CHANGE = 1
    val MESSAGE_READ = 2
    val MESSAGE_WRITE = 3
    val MESSAGE_DEVICE_NAME = 4
    val MESSAGE_TOAST = 5
    val MESSAGE_FILE = 6
    val MESSAGE_UPDATE = 7
    val MESSAGE_PROGRESS = 8

    // Key names received from the BluetoothService Handler
    val DEVICE_NAME = "device_name"
    val TOAST = "toast"

    val REAL_TEMP_TIME_TOLERANCE = 10
}