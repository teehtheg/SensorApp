package com.teeh.klimasensor

import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.teeh.klimasensor.common.activities.BaseActivity
import com.teeh.klimasensor.common.extension.bind
import com.teeh.klimasensor.database.DatabaseService


class MainActivity : BaseActivity() {

    private val bottomNavigationView: BottomNavigationView by bind(R.id.bottom_nav)
    private val fragmentContainer: LinearLayout by bind(R.id.fragment_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DatabaseService.instance.start(this)

        bottomNavigationView.setOnNavigationItemSelectedListener { it -> gotoActivity(it) }

        //val layoutParams = bottomNavigationView.getLayoutParams() as CoordinatorLayout.LayoutParams
        //layoutParams.behavior = BottomNavigationViewBehavior()

        replaceContentFragment(DataSynchronizerFragment())
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onDestroy() {
        DatabaseService.instance.stop()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Depending on the orientation of the screen, hide or show bottom navigation bar.
        // The bottom margin of the linear layout has to be adjusted, because otherwise the navigation bar would overlap with it.
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomNavigationView.visibility = View.GONE
            setBottomMargin(0)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            bottomNavigationView.visibility = View.VISIBLE
            setBottomMargin(56)
        }
    }

    private fun gotoActivity(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_data -> {
                replaceContentFragment(DataSynchronizerFragment())
            }
            R.id.nav_storage -> {
                replaceContentFragment(DatabaseFragment())
            }
            R.id.nav_analyze -> {
                replaceContentFragment(DataAnalyzerFragment())
            }
            R.id.nav_settings -> {
                replaceContentFragment(SettingsFragment())
            }
            R.id.nav_visualize -> {
                replaceContentFragment(DataVisualizerEditorFragment())
            }
        }
        return true
    }

    private fun replaceContentFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    private fun setBottomMargin(dp: Int) {
        var p = fragmentContainer.layoutParams as ViewGroup.MarginLayoutParams
        val d = this.getResources().getDisplayMetrics().density
        p.bottomMargin = (dp * d).toInt() // margin in pixels
        fragmentContainer.layoutParams = p
    }

    companion object {
        val TAG = "MainActivity"
    }
}
