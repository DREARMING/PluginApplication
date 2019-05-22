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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mvcoder.hostapplication.IHostService;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.loader.a.PluginAppCompatActivity;
import com.qihoo360.replugin.model.PluginInfo;

public class MainActivity extends PluginAppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView tvPluginInfo;
    private Button btKill;
    private Button btUpdate;
    private Button btException;
    private boolean destroy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        tvPluginInfo = findViewById(R.id.tvPluginVersion);
        btKill = findViewById(R.id.btKill);
        btUpdate = findViewById(R.id.btUpdate);
        btException = findViewById(R.id.btException);
        btKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                killPluginProcess();
            }
        });
        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePlugin();
            }
        });
        btException.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeException();
            }
        });

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

    private void makeException(){
        String str = "hello world - replugin";
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();

    }

    private void bindUpdateService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.mvcoder.hostapplication", "com.mvcoder.hostapplication.HostService"));
        boolean bindSuccess =  bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        Log.i(TAG, "bind success " + bindSuccess);
        if(!bindSuccess){
            mHandler.sendEmptyMessageDelayed(11, 10);
        }
    }


    private void updatePlugin(){
        /*String pluginDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/plugintest/";
        File dirFile = new File(pluginDir);
        if(!dirFile.exists()){
            dirFile.mkdirs();
        }
        PluginInfo pluginInfo = RePlugin.getPluginInfo("plugin1");
        int version = pluginInfo.getVersion();
        String filePath = dirFile.getAbsolutePath() + "/plugin_v" + (version + 1) +  ".apk";
        File apkFile = new File(filePath);
        if(!apkFile.exists()){
            Log.i(TAG, "file : "+apkFile.getAbsolutePath() + " not exist");
            return;
        }
        PluginInfo pluginInfo1 = RePlugin.install(filePath);
        if(pluginInfo1 != null){
            Log.i(TAG, "install sucess");
        }
        Log.i(TAG, "plugin info: \n" + pluginInfo1);*/
        if(iHostService != null){
            try {
                iHostService.updatePlugin("", "plugin1");
                setResult(RESULT_OK);
                finish();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }


    private IHostService iHostService = null;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 10:
                    killPluginProcess();
                    break;
                case 11:
                    bindUpdateService();
                    break;
            }
        }
    };

    private void killPluginProcess() {
        if(iHostService != null){
            try {
                iHostService.killProcess();

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        updateMessenger = null;
        setResult(RESULT_OK);
        finish();

        //Process.killProcess(Process.myPid());
    }

    private Messenger updateMessenger = new Messenger(mHandler);

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "service connected...");
           /* Messenger messenger = new Messenger(service);
            Message message = Message.obtain();
            message.what = 1;
            message.replyTo = updateMessenger;
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
            iHostService =  IHostService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "service disconnected...");
            iHostService = null;
            if(!destroy) {
                //需要判断是否仔自主断开
                mHandler.sendEmptyMessageDelayed(11, 10);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "ondestroy");
        destroy = true;
        unbindService(serviceConnection);
    }
}
