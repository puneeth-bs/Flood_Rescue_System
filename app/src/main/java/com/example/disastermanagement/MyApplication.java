package com.example.disastermanagement;

import android.app.Application;

//activity for checking internet connection

public class MyApplication extends Application {

    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized MyApplication getInstance(){
        return mInstance;
    }

    public void setConnectivityListener(ConnectivtyReceiver.ConnectivityReceiverListener listener){
        ConnectivtyReceiver.connectivityReceiverListener = listener;

    }
}
