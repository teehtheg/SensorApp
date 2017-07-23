package com.teeh.klimasensor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.teeh.klimasensor.bluetooth.BluetoothSynchronizer;
import com.teeh.klimasensor.common.activities.BaseActivity;


public class MainActivity extends BaseActivity {

    public static final int REQUEST_SHOW_ANALYZER = 1;
    public static final int REQUEST_SHOW_VISUALIZER = 1;

    public static final String TAG = "MainActivity";

    private FrameLayout frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothSynchronizer fragment = new BluetoothSynchronizer();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        DatabaseService.getInstance().start(this);
    }

    @Override
    public void onDestroy() {
        DatabaseService.getInstance().stop();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    protected boolean useToolbar() {
        return true;
    }

    public void buttonShowAnalyzer(View view) {
        Intent serverIntent = new Intent(this, DataAnalyzerActivity.class);
        startActivityForResult(serverIntent, REQUEST_SHOW_ANALYZER);
    }

    public void buttonShowVisualizer(View view) {
        Intent serverIntent = new Intent(this, DataVisualizerEditorActivity.class);
        startActivityForResult(serverIntent, REQUEST_SHOW_VISUALIZER);
    }
}
