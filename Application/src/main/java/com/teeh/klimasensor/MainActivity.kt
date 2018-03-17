package com.teeh.klimasensor

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import com.teeh.klimasensor.bluetooth.DeviceListActivity

import com.teeh.klimasensor.common.activities.BaseActivity
import com.teeh.klimasensor.common.constants.Constants.REQUEST_CONNECT_DEVICE
import com.teeh.klimasensor.common.constants.Constants.REQUEST_SHOW_DBUTIL
import com.teeh.klimasensor.common.constants.Constants.REQUEST_SHOW_SETTINGS
import com.teeh.klimasensor.database.DatabaseService


class MainActivity : BaseActivity() {

    private val frame: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DatabaseService.instance.start(this)

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            val fragment = DataSynchronizer()
            transaction.replace(R.id.sample_content_fragment, fragment)
            transaction.commit()
        }
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onDestroy() {
        DatabaseService.instance.stop()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.connect_scan -> {
                // Launch the DeviceListActivity to see devices and do scan
                val serverIntent = Intent(this, DeviceListActivity::class.java)
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE)
                return true
            }
            R.id.db_util -> {
                val serverIntent = Intent(this, DatabaseActivity::class.java)
                startActivityForResult(serverIntent, REQUEST_SHOW_DBUTIL)
                return true
            }
            R.id.settings -> {
                val serverIntent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(serverIntent, REQUEST_SHOW_SETTINGS)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun useToolbar(): Boolean {
        return true
    }

    fun buttonShowAnalyzer(view: View) {
        val serverIntent = Intent(this, DataAnalyzerActivity::class.java)
        startActivityForResult(serverIntent, REQUEST_SHOW_ANALYZER)
    }

    fun buttonShowVisualizer(view: View) {
        val serverIntent = Intent(this, DataVisualizerEditorActivity::class.java)
        startActivityForResult(serverIntent, REQUEST_SHOW_VISUALIZER)
    }

    companion object {

        val REQUEST_SHOW_ANALYZER = 1
        val REQUEST_SHOW_VISUALIZER = 1

        val TAG = "MainActivity"
    }
}
