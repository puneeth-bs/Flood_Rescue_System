package com.example.disastermanagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

//Splash Activity

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 1000;
    TextView progressText;
    BroadcastReceiver broadcastReceiver;
    Intent loginActivityIntent;
    public static SplashActivity splashActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressText = findViewById(R.id.progressText);
        splashActivity = this;

        //init();
        //checkInternetConnection(this);
        broadcastReceiver = new NetworkChangeReciever();
        registerNetworkBroadCastReceiver();


    }

    protected void registerNetworkBroadCastReceiver(){
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected void unregisterNetwork(){
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
    }



    private void checkInternetConnection(Context context) {

        boolean isConnected = ConnectivtyReceiver.isConnected(context);
        moveToLoginActivity(isConnected);
    }



    private void moveToLoginActivity(boolean isConnected){
        if(isConnected){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    unregisterNetwork();
                    startActivity(intent);
                    finish();
                }
            },SPLASH_TIME_OUT);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this, R.style.AlertDialogStyle);
            builder.setMessage("Please turn on your internet")
                    .setCancelable(false)
                    .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            //onNetworkConnectionChanged(isConnected);
                        }
                    });
            builder.show();
        }
    }


    public boolean isConnectedToInternet() {
        //progressText.setText("Connecting to internet...");
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                Toast.makeText(this, "Device connected to wifi", Toast.LENGTH_SHORT).show();
                //progressText.setText("Connected to WIFI network");
                return true;
            }
            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                Toast.makeText(this, "Device connected to mobile network", Toast.LENGTH_SHORT).show();
                //progressText.setText("Connected to mobile network");
                return true;
        }
      }
        return false;
    }





}