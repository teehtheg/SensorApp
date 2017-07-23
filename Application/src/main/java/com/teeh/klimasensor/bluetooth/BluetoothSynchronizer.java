package com.teeh.klimasensor.bluetooth;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.teeh.klimasensor.DatabaseService;
import com.teeh.klimasensor.KlimasensorDbActivity;
import com.teeh.klimasensor.R;
import com.teeh.klimasensor.SettingsActivity;
import com.teeh.klimasensor.TimeseriesService;
import com.teeh.klimasensor.common.Constants;
import com.teeh.klimasensor.common.ts.ValueType;

import java.util.List;

import static android.R.attr.button;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothSynchronizer extends Fragment {

    private static final String TAG = "BluetoothSynchronizer";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_SHOW_GRAPH = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_SHOW_ANALYZER = 4;
    private static final int REQUEST_SHOW_DBUTIL = 5;
    private static final int REQUEST_SHOW_SETTINGS = 6;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothService mChatService = null;

    private Button buttonDownload;
    private Button buttonUpdate;

    private ProgressBar progressBar;
    private TextView progressText;

    private static final String FILENAME = "timeseries.csv";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Snackbar.make(getActivity().findViewById(android.R.id.content),
                    "Bluetooth is not available",
                    Snackbar.LENGTH_SHORT)
                    .show();

            activity.finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }

        registerButtonListeners();

        progressBar = (ProgressBar)getActivity().findViewById(R.id.progress_download);
        progressText = (TextView)getActivity().findViewById(R.id.progress_download_text);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bluetooth_synchronizer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    private void registerButtonListeners() {
        buttonDownload = (Button) getActivity().findViewById(R.id.button_download);
        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadData();
            }
        });

        buttonUpdate = (Button) getActivity().findViewById(R.id.button_update);
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothService to perform bluetooth connections
        mChatService = new BluetoothService(mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Method called by button on the fragment
     * @param view
     */
    public void buttonDownload(View view) {
        downloadData();
    }

    /**
     * Method called by button on the fragment
     * @param view
     */
    public void buttonUpdate(View view) {
        updateData();
    }

    /**
     * Download data from raspberry pi
     */
    private void downloadData() {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Snackbar.make(getActivity().findViewById(android.R.id.content),
                    R.string.not_connected,
                    Snackbar.LENGTH_SHORT)
                    .show();

            return;
        }

        mChatService.write("getData".getBytes());
    }

    /**
     * Update data from raspberry pi
     */
    private void updateData() {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Snackbar.make(getActivity().findViewById(android.R.id.content),
                    R.string.not_connected,
                    Snackbar.LENGTH_SHORT)
                    .show();

            return;
        }

        String latestTs = TimeseriesService.getInstance()
                .getSensorTs()
                .getTs(ValueType.TEMPERATURE)
                .getLatestTimestampString();

        String msg = "getDataUpdate;" + latestTs;
        mChatService.write(msg.getBytes());
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Snackbar.make(getActivity().findViewById(android.R.id.content),
                    R.string.not_connected,
                    Snackbar.LENGTH_SHORT)
                    .show();

            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            WriteToDBTask writeTask;
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content),
                                "Connected to " + mConnectedDeviceName,
                                Snackbar.LENGTH_SHORT)
                                .show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content),
                                msg.getData().getString(Constants.TOAST),
                                Snackbar.LENGTH_SHORT)
                                .show();
                    }
                    break;
                case Constants.MESSAGE_FILE:
                    List<String> file = (List<String>) msg.obj;
                    DatabaseService.getInstance().clearSensorData();
                    writeTask = new WriteToDBTask();
                    writeTask.execute(file);

                    if (null != activity) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content),
                                file.size() + " records downloaded",
                                Snackbar.LENGTH_SHORT)
                                .show();
                    }
                    break;
                case Constants.MESSAGE_UPDATE:
                    List<String> update = (List<String>) msg.obj;
                    writeTask = new WriteToDBTask();
                    writeTask.execute(update);

                    if (null != activity) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content),
                                update.size() + " records downloaded",
                                Snackbar.LENGTH_SHORT)
                                .show();
                    }
                    break;
                case Constants.MESSAGE_PROGRESS:
                    String progress = (String)msg.obj;

                    String[] progressParts = progress.split("/");
                    Integer curPkg = Integer.valueOf(progressParts[0]);
                    Integer totPkg = Integer.valueOf(progressParts[1]);

                    displayProgress(curPkg, totPkg);
                    break;
            }
        }
    };

    private void displayProgress(Integer curPkg, Integer totPkg) {
        if (progressText.getVisibility() != View.VISIBLE) {
            progressText.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(totPkg);
        }

        String progress = curPkg.toString() + "/" + totPkg.toString();
        progressText.setText(progress);
        progressBar.setProgress(curPkg);

        if (curPkg.equals(totPkg)) {
            progressText.setText("complete!");
            progressBar.setVisibility(View.INVISIBLE);
            progressText.setVisibility(View.INVISIBLE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");

                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                            R.string.bt_not_enabled_leaving,
                            Snackbar.LENGTH_SHORT)
                            .show();

                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            }
            case R.id.download_data: {
                downloadData();
                return true;
            }
            case R.id.update_data: {
                updateData();
                return true;
            }
            case R.id.db_util: {
                Intent serverIntent = new Intent(getActivity(), KlimasensorDbActivity.class);
                startActivityForResult(serverIntent, REQUEST_SHOW_DBUTIL);
                return true;
            }
            case R.id.settings: {
                Intent serverIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivityForResult(serverIntent, REQUEST_SHOW_SETTINGS);
                return true;
            }
        }
        return false;
    }

    private class WriteToDBTask extends AsyncTask<List<String>, Void, Boolean> {

        protected Boolean doInBackground(List<String>... update) {
            for(List<String> elem : update) {
                TimeseriesService.getInstance().writeToDB(elem);
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {

        }
    }

}
