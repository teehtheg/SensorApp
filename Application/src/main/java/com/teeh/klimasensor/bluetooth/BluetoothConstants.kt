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
    val MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")

    // Constants that indicate the current connection state
    val STATE_NONE = 0       // we're doing nothing
    val STATE_LISTEN = 1     // now listening for incoming connections
    val STATE_CONNECTING = 2 // now initiating an outgoing connection
    val STATE_CONNECTED = 3  // now connected to a remote device
}