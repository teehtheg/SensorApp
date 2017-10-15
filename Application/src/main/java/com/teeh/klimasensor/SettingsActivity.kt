package com.teeh.klimasensor

import android.os.Bundle

import com.teeh.klimasensor.common.activities.BaseActivity

/**
 * Created by teeh on 12.02.2017.
 */

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onStart() {
        super.onStart()
    }
}
