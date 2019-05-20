package com.mvcoder.pluginapplication;

import android.app.Application;
import android.util.Log;

public class PluginApp extends Application {

    private static final String TAG = PluginApp.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Plugin App run...");
    }
}
