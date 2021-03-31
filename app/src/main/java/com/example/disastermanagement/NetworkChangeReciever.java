package com.example.disastermanagement;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import static androidx.core.content.ContextCompat.startActivity;

//Activity for monitoring internet connection

public class NetworkChangeReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            if(isOnline(context)){
                Toast.makeText(context, "Connected to internet", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "Not connected to internet", Toast.LENGTH_SHORT).show();
            }



            Activity activity = (Activity) context;
            if (activity instanceof SplashActivity) {
                //Toast.makeText(activity, "Splash Activity", Toast.LENGTH_SHORT).show();
                if (isOnline(context)) {
                    Toast.makeText(context, "Connected to internet", Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(context, LoginActivity.class);
                    context.startActivity(intent1);
                    ((SplashActivity) context).finish();
                } else {
                    //Toast.makeText(context, "No Internet connection", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setTitle("Do you have an access to internet")
                            .setMessage("Is it possible for you to connect to internet ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
                                    builder.setMessage("Please turn on your internet")
                                            .setCancelable(false)
                                            .setPositiveButton("Ok", null);
                                    builder.show();
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(activity, "Offline presistance enabled", Toast.LENGTH_SHORT).show();
                            ProgressDialog progressDialog = new ProgressDialog(context, R.style.MyAlertDialogStyle);
                            progressDialog.setTitle("Enabling firebase persistance");
                            progressDialog.setMessage("Redirecting you to chat activity");
                            progressDialog.show();
                            Intent intent2 = new Intent(context, MainChatActivity.class);
                            context.startActivity(intent2);
                            progressDialog.dismiss();
                            ((SplashActivity) context).finish();
                        }
                    });
                    builder1.show();

                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public boolean isOnline(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo.getType() == connectivityManager.TYPE_MOBILE) {
                return true;
            } else if (networkInfo.getType() == connectivityManager.TYPE_WIFI) {
                return true;
            }


        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


}