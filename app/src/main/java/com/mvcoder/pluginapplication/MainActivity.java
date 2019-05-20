package com.mvcoder.pluginapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.loader.a.PluginAppCompatActivity;
import com.qihoo360.replugin.model.PluginInfo;

public class MainActivity extends PluginAppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView tvPluginInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        tvPluginInfo = findViewById(R.id.tvPluginVersion);

        PluginInfo pluginInfo = RePlugin.getPluginInfo("plugin1");
        if(pluginInfo != null){
            Log.i(TAG, "plugin name : " + pluginInfo.getName() + " , plugin version : " + pluginInfo.getVersion());
            tvPluginInfo.setText("pid : " + Process.myPid() + " , " + pluginInfo.getName() + " v" + pluginInfo.getVersion());
        }else{
            Log.i(TAG, "plugin null");
            tvPluginInfo.setText("pid : " + Process.myPid() + " ,plugin null");
        }

        bindUpdateService();
    }

    private void bindUpdateService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.mvcoder.hostapplication", "com.mvcoder.hostapplication.UpdateService"));
        boolean bindSuccess =  bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        Log.i(TAG, "bind success " + bindSuccess);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 10:
                    killPluginProcess();
                    break;
            }
        }
    };

    private void killPluginProcess() {
        updateMessenger = null;
        unbindService(serviceConnection);
        Log.i(TAG,"kill plugin process");
        //Process.killProcess(Process.myPid());
        setResult(RESULT_OK);
        finish();
    }

    private Messenger updateMessenger = new Messenger(mHandler);

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "service connected...");
            Messenger messenger = new Messenger(service);
            Message message = Message.obtain();
            message.what = 1;
            message.replyTo = updateMessenger;
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "service disconnected...");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
}
