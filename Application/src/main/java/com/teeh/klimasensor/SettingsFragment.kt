package com.teeh.klimasensor

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.teeh.klimasensor.common.activities.BaseActivity

/**
 * Created by teeh on 12.02.2017.
 */

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_settings, container, false)
    }

    override fun onStart() {
        super.onStart()
    }
}
