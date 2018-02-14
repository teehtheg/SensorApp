package com.teeh.klimasensor

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout

import com.teeh.klimasensor.common.activities.BaseActivity
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
