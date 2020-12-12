package com.example.disastermanagement;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MapFragment extends Fragment implements CustomDialog.CustomDialogListener {

    GoogleMap map;
    ProgressDialog progressDialog;
    FusedLocationProviderClient fusedLocationProviderClient;
    Button permissionButton;
    AlertDialog.Builder alertBuilder;
    int DEFAULT_ZOOM = 12;
    Button sendLocationButton;
    Button cancelDialog;
    Button send;
    EditText fullname;
    EditText ageText;
    EditText bloodGroup;
    LatLng presentLocation, presentLocation1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_map, container, false);

        alertBuilder = new AlertDialog.Builder(getContext());
        View alertView = getLayoutInflater().inflate(R.layout.dialog, null);
        alertBuilder.setView(alertView);
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        permissionButton = alertView.findViewById(R.id.permissionButton);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        progressDialog = new ProgressDialog(getContext(), R.style.MyAlertDialogStyle);
        progressDialog.setTitle("Sending your details and location");
        progressDialog.setMessage("Please wait.");
        progressDialog.setCanceledOnTouchOutside(false);

        sendLocationButton = view.findViewById(R.id.sendLocationButton);
        send = alertView.findViewById(R.id.sendButton);
        fullname = alertView.findViewById(R.id.edit_fullname);
        ageText = alertView.findViewById(R.id.edit_age);
        bloodGroup = alertView.findViewById(R.id.edit_bloog_group);
        cancelDialog = alertView.findViewById(R.id.cancelButton);

        sendLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.show();
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String person_name = fullname.getText().toString();
                        String age = ageText.getText().toString();
                        String blood_group = bloodGroup.getText().toString();
                        String location = getLocationLatLng(presentLocation).toString();

                        if(!person_name.isEmpty() && !age.isEmpty() && !blood_group.isEmpty()){

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("Name", person_name);
                            map.put("Age", age);
                            map.put("blood_group", blood_group);
                            map.put("Location", location);
                            progressDialog.show();
                            FirebaseDatabase.getInstance().getReference("Locations").push()
                                    .setValue(map)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            alertDialog.dismiss();
                                            progressDialog.dismiss();
                                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                                            builder1.setMessage("Successfully sent your details and location.")
                                                    .setPositiveButton("Ok", null);
                                            AlertDialog alertDialog = builder1.create();
                                            alertDialog.show();
                                        }
                                    });
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Please fill all the details")
                                    .setPositiveButton("Ok", null);
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }


                    }
                });

            }
        });

        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });



        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                map = googleMap;
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    progressDialog.show();
                    getUserLocation();

                } else {
                    //request permission
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                            , Manifest.permission.ACCESS_COARSE_LOCATION}, 100 );
                }
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                    }
                });
            }
        });


        return view;
    }

    private void openDialog() {


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && grantResults.length > 0
                && (grantResults[0] + grantResults[1]) == PackageManager.PERMISSION_GRANTED){
            progressDialog.dismiss();
            getUserLocation();
        }else{
            Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        progressDialog.dismiss();
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                Location location = task.getResult();
                if(location!=null){
                    presentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    getLocationLatLng(presentLocation);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location");
                    map.addMarker(markerOptions);
                }else{
                    LocationRequest locationRequest = new LocationRequest()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1);

                    LocationCallback locationCallback = new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            Location location1 = locationResult.getLastLocation();
                            presentLocation = new LatLng(location1.getLatitude(), location1.getLongitude());
                            getLocationLatLng(new LatLng(location1.getLatitude(), location1.getLongitude()));
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location1.getLatitude(), location1.getLongitude()), DEFAULT_ZOOM));
                            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(location1.getLatitude(), location1.getLongitude())).title("Your Location");
                            map.addMarker(markerOptions);
                        }
                    };

                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            }
        });
        progressDialog.dismiss();
    }

    @SuppressLint("MissingPermission")
    private LatLng getLocationLatLng(LatLng location){
        return location;
    }

    private String getFullname(String fullname){
        return fullname;
    }
    private String getAge(String age){
        return age;
    }


    @Override
    public void applyTexts(String fullname, String age) {
        getFullname(fullname);
        getAge(age);
        if(fullname.length() > 0 && age.length() > 0){
            Toast.makeText(getContext(), "I am here", Toast.LENGTH_SHORT).show();
        }
    }
}
