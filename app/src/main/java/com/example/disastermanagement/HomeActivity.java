package com.example.disastermanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, 100 );


        }

        initBluetoothAdapter();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment selectedFargment = null;
                switch (item.getItemId()){
                    case R.id.nav_home:
                        selectedFargment = new HomeFragment();
                        break;

                    case R.id.nav_location:
                        selectedFargment = new MapFragment();
                        break;
                    case R.id.nav_chat:
                        selectedFargment = new ChatFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFargment).commit();
                return true;
            }
        });



    }

    private void initBluetoothAdapter() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth off", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.top_menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.account_item){
            return true;
        }

        if(id == R.id.logout_item){
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.bluetooth){
            enableBluetooth();
            return true;
        }

        if(id == R.id.available_devices){
            showAvailableDevices();
            return true;
        }
        return true;
    }

    private void showAvailableDevices() {

    }


















    private void enableBluetooth() {

        if(bluetoothAdapter.isEnabled()){
            Toast.makeText(this, "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
        }else{
            bluetoothAdapter.enable();
            Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
        }


    }
}