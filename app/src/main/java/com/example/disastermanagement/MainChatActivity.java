package com.example.disastermanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

//Bluetooth chatting activity

public class MainChatActivity extends AppCompatActivity implements LocationListener {


    TextView connectionStatus, availabledevicesTextView, messageDisplay, connectedToTextView, chat_item, availableDevicesTextView1;
    EditText textMessageBox;
    ImageView sendMessageButton;
    ListView listView, chatListView;
    Button connectButton, discovearbleButton;
    RelativeLayout relativeLayout;
    ProgressBar progressBar;
    ImageView getLocationImageView;
    private String deviceName;
    private ArrayList<String> chatArrayList;
    private ArrayAdapter<String> chatListAdapter;
    ChatArrayAdapter chatArrayAdapter;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] bluetoothDevicesArray;
    int deviceStatus;
    boolean clientDevice;
    LocationManager locationManager;
    LocationListener locationListener;
    FusedLocationProviderClient fusedLocationProviderClient;
    ProgressDialog progressDialog;
    ArrayList<String> locationList = new ArrayList<>();
    ArrayList<String> nameList = new ArrayList<>();


    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECIEVED = 5;
    int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final String APP_NAME = "D-Alert";
    private static final UUID uuid = UUID.fromString("700d03f7-b399-4503-8a88-4789ec912c7a");
    SendRecieve sendRecieve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);
        setTitle("Chat");
        init();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//enabling bluetooth adapter in mobile
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }

        chatArrayList = new ArrayList<>();
        chatListAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.chat_item_left, R.id.textView, chatArrayList);
        chatListView.setAdapter(chatListAdapter);
        implementListeners();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    private void implementListeners() {

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isEnabled()) {
                    //startProgressBar();
                    chatArrayList.clear();
                    connectedToTextView.setText("");
                    listView.setVisibility(View.VISIBLE);
                    Set<BluetoothDevice> bluetoothDeviceSet = bluetoothAdapter.getBondedDevices();
                    String[] strings = new String[bluetoothDeviceSet.size()];
                    bluetoothDevicesArray = new BluetoothDevice[bluetoothDeviceSet.size()];
                    int index = 0;
                    if (bluetoothDeviceSet.size() > 0) {
                        for (BluetoothDevice device : bluetoothDeviceSet) {
                            bluetoothDevicesArray[index] = device;
                            strings[index] = device.getName();
                            index++;
                        }

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
                        //chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.chat_item_left);
                        listView.setAdapter(arrayAdapter);
                        //stopProgressBar();
                    }
                } else {
                    Toast.makeText(MainChatActivity.this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });


        discovearbleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isEnabled()) {
                    chatArrayList.clear();
                    chatListAdapter.notifyDataSetChanged();
                    ServerClass serverClass = new ServerClass();
                    connectedToTextView.setText("");
                    serverClass.start();
                } else {
                    Toast.makeText(MainChatActivity.this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClientClass clientClass = new ClientClass(bluetoothDevicesArray[position]);
                clientClass.start();
                connectionStatus.setText("Connecting...");

            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = textMessageBox.getText().toString();
                if (deviceStatus == STATE_CONNECTED) {
                    if (string.length() > 0) {
                        sendRecieve.write(string.getBytes());
                        if (!clientDevice) {
                            chat_item.setGravity(Gravity.RIGHT);
                        }
                        chatArrayList.add("Me : " + string);
                        chatListAdapter.notifyDataSetChanged();
                        textMessageBox.setText("");
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainChatActivity.this);
                    builder.setTitle("Not Connected")
                            .setMessage("You are not connected to device")
                            .setPositiveButton("Ok", null);
                    builder.show();
                }
            }
        });

        getLocationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setTitle("Fetching your GPS coordinates");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();
                LocationManager locationManager1 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean providerEnabled = locationManager1.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (providerEnabled) {
                    Location location = locationManager1.getLastKnownLocation(locationManager1.NETWORK_PROVIDER);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onLocationChanged(location);
                        }
                    }, 3000);

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(MainChatActivity.this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location == null) {
            //Toast.makeText(this, "GPS Error", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Some error occurred in fetching your GPS coordinates. Please try again")
                    .setTitle("GPS Error")
                    .setPositiveButton("Ok", null);
            builder.show();
            progressDialog.dismiss();
        } else {
            String lat = String.valueOf(location.getLatitude());
            String longi = String.valueOf(location.getLongitude());
            if (lat != null && longi != null) {
                String msg = lat + ", " + longi;
                for (int i = 0; i < locationList.size(); i++) {
                    msg += "\n" + nameList.get(i) + ": Location: " + locationList.get(i);
                }
                textMessageBox.setText("Location: " + msg);
                progressDialog.dismiss();
            } else {
                Toast.makeText(this, "GPS turned off", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case STATE_LISTENING:
                    connectionStatus.setText("Listening...");
                    deviceStatus = STATE_LISTENING;
                    break;
                case STATE_CONNECTING:
                    connectionStatus.setText("Connecting..");
                    deviceStatus = STATE_CONNECTING;
                    break;

                case STATE_CONNECTED:
                    connectionStatus.setText("Connected");
                    deviceStatus = STATE_CONNECTED;
                    connectedToTextView.setText(deviceName);
                    connectedToTextView.setVisibility(View.VISIBLE);
                    break;

                case STATE_CONNECTION_FAILED:
                    connectionStatus.setText("Connection failed");
                    deviceStatus = STATE_CONNECTION_FAILED;
                    break;

                case STATE_MESSAGE_RECIEVED:
                    byte[] readBuffer = (byte[]) msg.obj;
                    String tmpMessage = new String(readBuffer, 0, msg.arg1);
                    checkForCoordinates(deviceName, tmpMessage);
                    chatArrayList.add(deviceName + " : " + tmpMessage);
                    chatListAdapter.notifyDataSetChanged();
                    break;
            }
            return false;
        }
    });

    private void checkForCoordinates(String deviceName, String message) {

        if (message.contains("Location:")) {
            String coordinates = message;
            String locs = coordinates.substring(10, coordinates.length());
            locationList.add(locs);
            nameList.add(deviceName);

            HashMap<String, Object> map1 = new HashMap<>();

            HashMap<String, Object> map = new HashMap<>();
            map.put("Device_Name", deviceName);
            map.put("Location", locs);

            for (int i = 0; i < locationList.size(); i++) {
                map.put("" + nameList.get(i), "" + locationList.get(i));
            }

            map1.put("Locations: ", "" + message);

            FirebaseDatabase.getInstance().getReference("Bluetooth_Locations")
                    .push()
                    .setValue(map1)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainChatActivity.this, R.style.AlertDialogStyle);
                            builder.setTitle("Locations Sent")
                                    .setMessage("All the locations are sent to the rescue teams")
                                    .setPositiveButton("Ok", null);
                            //builder.show();
                            Toast.makeText(MainChatActivity.this, "All locations sent to rescue teams", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }


    //bluetooth server class
    private class ServerClass extends Thread {

        private BluetoothServerSocket bluetoothServerSocket;

        public ServerClass() {
            try {
                bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket bluetoothSocket = null;
            while (bluetoothSocket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    clientDevice = false;
                    handler.sendMessage(message);
                    bluetoothSocket = bluetoothServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bluetoothSocket != null) {

                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    deviceName = bluetoothSocket.getRemoteDevice().getName();
                    handler.sendMessage(message);
                    sendRecieve = new SendRecieve(bluetoothSocket);
                    sendRecieve.start();

                    break;
                } else {
                    try {
                        Message message1 = Message.obtain();
                        message1.what = STATE_CONNECTION_FAILED;
                        handler.sendMessage(message1);
                        bluetoothSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    //bluetooth client class
    private class ClientClass extends Thread {

        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1) {
            device = device1;
            try {
                socket = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                deviceName = device.getName();
                clientDevice = true;
                handler.sendMessage(message);
                listView.setVisibility(View.INVISIBLE);
                sendRecieve = new SendRecieve(socket);
                sendRecieve.start();


            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                listView.setVisibility(View.INVISIBLE);
                handler.sendMessage(message);
                try {
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

    }

    //bluetooth Sending and recieving class
    private class SendRecieve extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputstream;
        private final OutputStream outputstream;

        public SendRecieve(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputstream = tempIn;
            outputstream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {

                try {
                    bytes = inputstream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECIEVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputstream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void init() {
        connectionStatus = findViewById(R.id.connectionStatus);
        availabledevicesTextView = findViewById(R.id.availabledevicesTextView);
        messageDisplay = findViewById(R.id.messageDisplay);
        textMessageBox = findViewById(R.id.textMessageBox);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        listView = findViewById(R.id.listView);
        //listView.setDivider(null);
        connectButton = findViewById(R.id.connectButton);
        discovearbleButton = findViewById(R.id.discovearbleButton);
        //progressBar = findViewById(R.id.horizontal_progress);
        chatListView = findViewById(R.id.chatListView);
        chatListView.setDivider(null);
        View inflatedView = getLayoutInflater().inflate(R.layout.chat_item_right, null);
        chat_item = (TextView) inflatedView.findViewById(R.id.msgr);
        chat_item.setGravity(Gravity.RIGHT);
        connectedToTextView = findViewById(R.id.connectedToTextView);
        getLocationImageView = findViewById(R.id.bluetoothLocation);
        //availableDevicesTextView1 = findViewById(R.id.availableDevicesTextView1);
        //relativeLayout = findViewById(R.id.relativeLayout1);
        progressDialog = new ProgressDialog(MainChatActivity.this, R.style.MyAlertDialogStyle);

    }


}