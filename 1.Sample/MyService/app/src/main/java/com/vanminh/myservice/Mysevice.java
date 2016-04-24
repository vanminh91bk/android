package com.vanminh.myservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by vanmi on 11/15/2015.
 */
public class Mysevice extends Service {
    private static final boolean DEBUG = true;
    private final String TAG = "Myservice";
    private final String TAG_ACTIVITY = "Mysevice";

    @Override
    public IBinder onBind(Intent intent) {
        showLog("IBinder");
        return null;
    }


    @Override
    public void onCreate() {
        showLog("onCreate");
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        showLog("onDestroy");
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showLog("onStartCommand");
        return START_STICKY;
    }

    private void showLog(String mes) {
        if (DEBUG) {
            Log.d(TAG, TAG_ACTIVITY + ": " + mes);
        }
    }
}
