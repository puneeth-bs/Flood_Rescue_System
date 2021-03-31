package com.example.disastermanagement;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class ChatActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    BluetoothAdapter bluetoothAdapter;
    Intent bluetoothEnableIntent;
    int requestCodeForBluetoothEnable = 100;
    ListView pairedDeviceslistView;
    ListView availableDevicesListView;
    ProgressBar progressBar;
    TextView pressScanButtonTextView;
    TextView gettingNearByDevicesText;
    Button scanButton;
    ArrayList<String> stringArrayList = new ArrayList<>();
    ArrayAdapter<String> availableDevicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle("Chat");


        progressDialog = new ProgressDialog(ChatActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Checking bluetooth connectivity");
        progressDialog.setMessage("Please wait while we check for the requirements");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        checkBluetoothConnectivity();

        init();

        if(bluetoothAdapter.isEnabled()){

            getPairedDevices();
            pressScanButtonTextView.setVisibility(View.VISIBLE);


        }

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter.isEnabled()){
                    getAvailableDevices();
                }else{
                    Toast.makeText(ChatActivity.this, "Please turn on bluetooth", Toast.LENGTH_SHORT).show();
                }
            }
        });

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, intentFilter);
        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter1);

        availableDevicesAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, stringArrayList);
        availableDevicesListView.setAdapter(availableDevicesAdapter);


    }

    private void init() {
        pairedDeviceslistView = findViewById(R.id.pairedDevicesListView);
        availableDevicesListView = findViewById(R.id.availableDevicesListView);
        scanButton = findViewById(R.id.scanButton);
        pressScanButtonTextView = findViewById(R.id.pressScanButtonTextView);
        progressBar = findViewById(R.id.progressCircle);
        gettingNearByDevicesText = findViewById(R.id.gettingNearByDevicesText);

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                stringArrayList.add(device.getName());
                availableDevicesAdapter.notifyDataSetChanged();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                if(stringArrayList.size() == 0){
                    Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show();
                }else{
                    stopLoading();
                }
            }
        }
    };

    private void stopLoading(){
        pressScanButtonTextView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        gettingNearByDevicesText.setVisibility(View.INVISIBLE);
    }



    private void getAvailableDevices() {

        if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoveryIntent);
        }
        scanForDevices();
    }

    private void scanForDevices() {
        pressScanButtonTextView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        gettingNearByDevicesText.setVisibility(View.VISIBLE);

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }else{
            bluetoothAdapter.startDiscovery();
        }

    }

    private void getPairedDevices() {

        Set<BluetoothDevice> bluetoothDeviceSet = bluetoothAdapter.getBondedDevices();
        String [] strings = new String[bluetoothDeviceSet.size()];
        int index = 0;
        if(bluetoothDeviceSet.size() > 0){
            for(BluetoothDevice device : bluetoothDeviceSet){
                strings[index] = device.getName();
                index++;
            }

            ArrayAdapter<String> pairedDeviceArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
            pairedDeviceslistView.setAdapter(pairedDeviceArrayAdapter);
        }

    }

    private void checkBluetoothConnectivity() {
        if(bluetoothAdapter == null){

            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder.setMessage("Bluetooth is not supported in your device")
                    .setPositiveButton("Ok", null);
            builder.show();
        }else{
            if(!bluetoothAdapter.isEnabled()){
                startActivityForResult(bluetoothEnableIntent, requestCodeForBluetoothEnable);
                getPairedDevices();
            }else if(bluetoothAdapter.isEnabled()){
                progressDialog.dismiss();

            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == requestCodeForBluetoothEnable) {
            if (resultCode == RESULT_OK) {
                progressDialog.dismiss();
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                progressDialog.dismiss();
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}