package com.teeh.klimasensor

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView

import com.teeh.klimasensor.bluetooth.BluetoothConstants
import com.teeh.klimasensor.bluetooth.BluetoothService
import com.teeh.klimasensor.bluetooth.DeviceListActivity
import com.teeh.klimasensor.bluetooth.MessageHandler
import com.teeh.klimasensor.common.constants.Constants.REQUEST_CONNECT_DEVICE
import com.teeh.klimasensor.common.constants.Constants.REQUEST_ENABLE_BT
import com.teeh.klimasensor.common.utils.DateUtils
import com.teeh.klimasensor.databinding.FragmentDataSynchronizerBinding
import com.teeh.klimasensor.rest.SensorData
import com.teeh.klimasensor.rest.SensorDataService
import com.teeh.klimasensor.rest.ServerStatus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DataSynchronizerFragment : Fragment() {

    /**
     * String buffer for outgoing messages
     */
    private lateinit var mOutStringBuffer: StringBuffer

    /**
     * Adapters
     */
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var mRestAdapter: SensorDataService

    /**
     * Member object for the chat services
     */
    private var mBluetoothService: BluetoothService? = null

    private lateinit var buttonDownload: Button
    private lateinit var buttonUpdate: Button

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private val mHandler = MessageHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Get REST adapter
        mRestAdapter = SensorDataService(context!!)

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            val activity = activity
            Snackbar.make(activity!!.findViewById(android.R.id.content),
                    "Bluetooth is not available",
                    Snackbar.LENGTH_SHORT)
                    .show()

            activity!!.finish()
        }
    }


    override fun onStart() {
        super.onStart()
        // If BT is not on, request that it be enabled.
        // setupBluetooth() will then be called during onActivityResult
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        }

        if (mBluetoothAdapter!!.isEnabled) {
            setupBluetooth()
        }

        registerButtonListeners()

        progressBar = activity!!.findViewById<View>(R.id.progress_download) as ProgressBar
        progressText = activity!!.findViewById<View>(R.id.progress_download_text) as TextView
    }

    override fun onDestroy() {
        super.onDestroy()
        mBluetoothService!!.stop()
    }

    override fun onResume() {
        super.onResume()

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService!!.state == BluetoothConstants.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothService!!.start()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentDataSynchronizerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_data_synchronizer, container, false)
        binding.fragment = this
        return binding.root
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
    private fun setupBluetooth() {

        // Initialize the BluetoothService to perform bluetooth connections
        mBluetoothService = BluetoothService(mHandler, context!!)

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = StringBuffer("")
    }

    /**
     * Method called by button on the fragment
     * @param view
     */
    fun buttonConnect(view: View) {
        val serverIntent = Intent(activity!!, DeviceListActivity::class.java)
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE)
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
        if (mBluetoothService!!.state != BluetoothConstants.STATE_CONNECTED) {
            Snackbar.make(activity!!.findViewById(android.R.id.content),
                    R.string.not_connected,
                    Snackbar.LENGTH_SHORT)
                    .show()

            mRestAdapter.getStatus(getStatusCallback(
                    { mRestAdapter.getSensorData(getSensorDataCallback()) }
            ))
            return
        }

        mBluetoothService!!.write("getData".toByteArray())
    }

    /**
     * Update data from raspberry pi
     */
    private fun updateData() {
        // Check that we're actually connected before trying anything
        if (mBluetoothService!!.state != BluetoothConstants.STATE_CONNECTED) {
            Snackbar.make(activity!!.findViewById(android.R.id.content),
                    R.string.not_connected,
                    Snackbar.LENGTH_SHORT)
                    .show()

            mRestAdapter.getStatus(getStatusCallback(
                    { mRestAdapter.getSensorDataFrom(getSensorDataCallback()) }
            ))
            return
        }
        val latestTs = DateUtils.toString(TimeseriesService.instance.readLastFromDB().timestamp)
        val msg = "getDataUpdate;" + latestTs
        mBluetoothService!!.write(msg.toByteArray())
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private fun sendMessage(message: String) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService!!.state != BluetoothConstants.STATE_CONNECTED) {
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
            mBluetoothService!!.write(send)

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
                    setupBluetooth()
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
        mBluetoothService!!.connect(device)
    }

    fun getSensorDataCallback(): Callback<List<SensorData>> {
        return object : Callback<List<SensorData>> {
            override fun onResponse(call: Call<List<SensorData>>, response: Response<List<SensorData>>) {
                if (response.isSuccessful) {
                    if (!checkNotNull(response.body()).isEmpty()) {
                        val update = response.body()!!
                        TimeseriesService.instance.updateSensorTsAsync(update)
                                .invokeOnCompletion {
                            Snackbar.make(activity!!.findViewById(android.R.id.content),
                                    update.size.toString() + " records downloaded",
                                    Snackbar.LENGTH_SHORT)
                                    .show()
                        }
                    } else {
                        Snackbar.make(activity!!.findViewById(android.R.id.content),
                                R.string.download_empty,
                                Snackbar.LENGTH_SHORT)
                                .show()
                    }
                } else {
                    Log.e(DataSynchronizerFragment.TAG, "An error occured: " + response.errorBody())
                }
                return
            }

            override fun onFailure(call: Call<List<SensorData>>, t: Throwable) {
                Log.e(DataSynchronizerFragment.TAG, "Failure: " + t.message)
            }
        }
    }

    fun getStatusCallback(function: () -> Unit): Callback<ServerStatus> {
        return object : Callback<ServerStatus> {
            override fun onResponse(call: Call<ServerStatus>, response: Response<ServerStatus>) {
                Log.i(DataSynchronizerFragment.TAG, "Status: " + response.body().toString())
                if (isStatusOk(response)) {
                    function.invoke()
                } else {
                    Snackbar.make(activity!!.findViewById(android.R.id.content),
                             "ServerStatus not ok",
                            Snackbar.LENGTH_SHORT)
                            .show()
                    Log.e(DataSynchronizerFragment.TAG, "An error occured: " + response.errorBody())
                }
                return
            }

            override fun onFailure(call: Call<ServerStatus>, t: Throwable) {
                Log.e(DataSynchronizerFragment.TAG, "Failure: " + t.message)
            }
        }
    }

    private fun isStatusOk(response: Response<ServerStatus>): Boolean {
        return response.isSuccessful && "ok".equals(response.body()?.status)
    }

    companion object {

        private val TAG = "DataSynchronizerFragment"

    }

}
