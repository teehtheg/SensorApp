package com.teeh.klimasensor.bluetooth


import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import com.teeh.klimasensor.R
import com.teeh.klimasensor.common.Constants
import com.teeh.klimasensor.database.DatabaseService

class MessageHandler(private val synchronizer: BluetoothSynchronizer) : Handler() {

    private var mConnectedDeviceName: String? = null

    override fun handleMessage(msg: Message) {

        val writeTask: WriteToDBTask
        val activity = synchronizer.activity

        when (msg.what) {
            Constants.MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                BluetoothConstants.STATE_CONNECTED -> synchronizer.setStatus(synchronizer.getString(R.string.title_connected_to, mConnectedDeviceName))
                BluetoothConstants.STATE_CONNECTING -> synchronizer.setStatus(R.string.title_connecting)
                BluetoothConstants.STATE_LISTEN, BluetoothConstants.STATE_NONE -> synchronizer.setStatus(R.string.title_not_connected)
            }
            Constants.MESSAGE_WRITE -> {
                val writeBuf = msg.obj as ByteArray
                val writeMessage = String(writeBuf)
            }
            Constants.MESSAGE_READ -> {
                val readBuf = msg.obj as ByteArray
                val readMessage = String(readBuf, 0, msg.arg1)
            }
            Constants.MESSAGE_DEVICE_NAME -> {
                // save the connected device's name
                mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
                if (null != activity) {
                    Snackbar.make(synchronizer.activity.findViewById(android.R.id.content),
                            "Connected to " + mConnectedDeviceName!!,
                            Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
            Constants.MESSAGE_TOAST -> if (null != activity) {
                Snackbar.make(synchronizer.activity.findViewById(android.R.id.content),
                        msg.data.getString(Constants.TOAST)!!,
                        Snackbar.LENGTH_SHORT)
                        .show()
            }
            Constants.MESSAGE_FILE -> {
                val file = msg.obj as List<String>
                DatabaseService.instance.clearSensorData()
                writeTask = WriteToDBTask()
                writeTask.execute(file)

                if (null != activity) {
                    Snackbar.make(synchronizer.activity.findViewById(android.R.id.content),
                            file.size.toString() + " records downloaded",
                            Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
            Constants.MESSAGE_UPDATE -> {
                val update = msg.obj as List<String>
                writeTask = WriteToDBTask()
                writeTask.execute(update)

                if (null != activity) {
                    Snackbar.make(synchronizer.activity.findViewById(android.R.id.content),
                            update.size.toString() + " records downloaded",
                            Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
            Constants.MESSAGE_PROGRESS -> {
                val progress = msg.obj as String

                val progressParts = progress.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val curPkg = Integer.valueOf(progressParts[0])
                val totPkg = Integer.valueOf(progressParts[1])

                synchronizer.displayProgress(curPkg, totPkg)
            }
        }
    }
}