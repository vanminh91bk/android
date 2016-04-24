package com.vanminh.myservice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final boolean DEBUG = true;
    private final String TAG = "Myservice";
    private final String TAG_ACTIVITY = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void showLog(String mes) {
        if (DEBUG) {
            Log.d(TAG, TAG_ACTIVITY + ": " + mes);
        }
    }

    public void startService(View v){
        showLog("startService");
        startService(new Intent(this, Mysevice.class));
    }
}
