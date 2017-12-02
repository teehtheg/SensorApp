package com.teeh.klimasensor.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView

import com.teeh.klimasensor.DatabaseActivity
import com.teeh.klimasensor.R
import com.teeh.klimasensor.SettingsActivity
import com.teeh.klimasensor.TimeseriesService
import com.teeh.klimasensor.common.ts.ValueType
import com.teeh.klimasensor.common.utils.DateUtils

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
class BluetoothSynchronizer : Fragment() {

    /**
     * String buffer for outgoing messages
     */
    private lateinit var mOutStringBuffer: StringBuffer

    /**
     * Local Bluetooth adapter
     */
    private var mBluetoothAdapter: BluetoothAdapter? = null

    /**
     * Member object for the chat services
     */
    private var mChatService: BluetoothService? = null

    private lateinit var buttonDownload: Button
    private lateinit var buttonUpdate: Button

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private val mWriteListener = TextView.OnEditorActionListener { view, actionId, event ->
        // If the action is a key-up event on the return key, send the message
        if (actionId == EditorInfo.IME_NULL && event.action == KeyEvent.ACTION_UP) {
            val message = view.text.toString()
            sendMessage(message)
        }
        true
    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private val mHandler = MessageHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            val activity = activity
            Snackbar.make(getActivity()!!.findViewById(android.R.id.content),
                    "Bluetooth is not available",
                    Snackbar.LENGTH_SHORT)
                    .show()

            activity!!.finish()
        }
    }


    override fun onStart() {
        super.onStart()
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        }

        setupChat()

        registerButtonListeners()

        progressBar = activity!!.findViewById<View>(R.id.progress_download) as ProgressBar
        progressText = activity!!.findViewById<View>(R.id.progress_download_text) as TextView
    }

    override fun onDestroy() {
        super.onDestroy()
        mChatService!!.stop()
    }

    override fun onResume() {
        super.onResume()

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService!!.state == BluetoothConstants.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService!!.start()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bluetooth_synchronizer, container, false)
    }

    private fun registerButtonListeners() {
        buttonDownload = activity!!.findViewById<View>(R.id.button_download) as Button
        buttonDownload.setOnClickListener { downloadData() }

        buttonUpdate = activity!!.findViewById<View>(R.id.button_update) as Button
        buttonUpdate.setOnClickListener { updateData() }
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private fun setupChat() {
        Log.d(TAG, "setupChat()")

        // Initialize the BluetoothService to perform bluetooth connections
        mChatService = BluetoothService(mHandler)

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = StringBuffer("")
    }

    /**
     * Method called by button on the fragment
     * @param view
     */
    fun buttonDownload(view: View) {
        downloadData()
    }

    /**
     * Method called by button on the fragment
     * @param view
     */
    fun buttonUpdate(view: View) {
        updateData()
    }

    /**
     * Download data from raspberry pi
     */
    private fun downloadData() {
        // Check that we're actually connected before trying anything
        if (mChatService!!.state != BluetoothConstants.STATE_CONNECTED) {
            Snackbar.make(activity!!.findViewById(android.R.id.content),
                    R.string.not_connected,
                    Snackbar.LENGTH_SHORT)
                    .show()

            return
        }

        mChatService!!.write("getData".toByteArray())
    }

    /**
     * Update data from raspberry pi
     */
    private fun updateData() {
        // Check that we're actually connected before trying anything
        if (mChatService!!.state != BluetoothConstants.STATE_CONNECTED) {
            Snackbar.make(activity!!.findViewById(android.R.id.content),
                    R.string.not_connected,
                    Snackbar.LENGTH_SHORT)
                    .show()

            return
        }
        val latestTs = DateUtils.toString(TimeseriesService.instance.readLastFromDB().timestamp)
        val msg = "getDataUpdate;" + latestTs
        mChatService!!.write(msg.toByteArray())
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private fun sendMessage(message: String) {
        // Check that we're actually connected before trying anything
        if (mChatService!!.state != BluetoothConstants.STATE_CONNECTED) {
            Snackbar.make(activity!!.findViewById(android.R.id.content),
                    R.string.not_connected,
                    Snackbar.LENGTH_SHORT)
                    .show()

            return
        }

        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothService to write
            val send = message.toByteArray()
            mChatService!!.write(send)

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0)
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    fun setStatus(resId: Int) {
        val activity = activity ?: return
        val actionBar = activity.actionBar ?: return
        actionBar.setSubtitle(resId)
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    fun setStatus(subTitle: CharSequence) {
        val activity = activity ?: return
        val actionBar = activity.actionBar ?: return
        actionBar.subtitle = subTitle
    }

    fun displayProgress(curPkg: Int?, totPkg: Int?) {
        if (progressText.visibility != View.VISIBLE) {
            progressText.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            progressBar.max = totPkg!!
        }

        val progress = curPkg!!.toString() + "/" + totPkg!!.toString()
        progressText.text = progress
        progressBar.progress = curPkg

        if (curPkg == totPkg) {
            progressText.text = "complete!"
            progressBar.visibility = View.INVISIBLE
            progressText.visibility = View.INVISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CONNECT_DEVICE ->
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false)
                }
            REQUEST_ENABLE_BT ->
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat()
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled")

                    Snackbar.make(activity!!.findViewById(android.R.id.content),
                            R.string.bt_not_enabled_leaving,
                            Snackbar.LENGTH_SHORT)
                            .show()

                    activity!!.finish()
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An [Intent] with [DeviceListActivity.EXTRA_DEVICE_ADDRESS] extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private fun connectDevice(data: Intent?, secure: Boolean) {
        // Get the device MAC address
        val address = data!!.extras!!
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
        // Get the BluetoothDevice object
        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        // Attempt to connect to the device
        mChatService!!.connect(device)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.connect_scan -> {
                // Launch the DeviceListActivity to see devices and do scan
                val serverIntent = Intent(activity, DeviceListActivity::class.java)
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE)
                return true
            }
            R.id.download_data -> {
                downloadData()
                return true
            }
            R.id.update_data -> {
                updateData()
                return true
            }
            R.id.db_util -> {
                val serverIntent = Intent(activity, DatabaseActivity::class.java)
                startActivityForResult(serverIntent, REQUEST_SHOW_DBUTIL)
                return true
            }
            R.id.settings -> {
                val serverIntent = Intent(activity, SettingsActivity::class.java)
                startActivityForResult(serverIntent, REQUEST_SHOW_SETTINGS)
                return true
            }
        }
        return false
    }

    companion object {

        private val TAG = "BluetoothSynchronizer"

        // Intent request codes
        private val REQUEST_CONNECT_DEVICE = 1
        private val REQUEST_SHOW_GRAPH = 2
        private val REQUEST_ENABLE_BT = 3
        private val REQUEST_SHOW_ANALYZER = 4
        private val REQUEST_SHOW_DBUTIL = 5
        private val REQUEST_SHOW_SETTINGS = 6

        private val FILENAME = "timeseries.csv"
    }

}
