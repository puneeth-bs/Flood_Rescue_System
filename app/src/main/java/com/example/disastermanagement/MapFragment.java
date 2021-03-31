package com.example.disastermanagement;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.disastermanagement.directionhelpers.FetchURL;
import com.example.disastermanagement.directionhelpers.TaskLoadedCallback;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MapFragment extends Fragment implements CustomDialog.CustomDialogListener, AdapterView.OnItemSelectedListener {

    GoogleMap map;
    private String user_unique_identification_id = null;
    ProgressDialog progressDialog, routingDialog;
    FusedLocationProviderClient fusedLocationProviderClient;
    Button permissionButton;
    AlertDialog.Builder alertBuilder;
    int DEFAULT_ZOOM = 15;
    Button sendLocationButton;
    Button cancelDialog;
    Button send;
    EditText fullname;
    EditText ageText;
    EditText bloodGroup;
    TextView userIDTextView;
    LatLng presentLocation, presentLocation1, destinationLocation, start, end;
    String designation;
    String person_name;
    ImageButton getDirections;
    int locationSentTemp = 0;
    String rescue_latitude, rescue_longitude, rescue_id, rescue_person_name;
    Double rescue_lat, rescue_lng;
    String user_id;
    int current_user_id;
    char curr_user_id_char;
    HashMap<String, Object> userIDMap = new HashMap<>();
    HashMap<String, Object> map1 = new HashMap<>();
    MarkerOptions place1, place2;
    AlertDialog.Builder routingAlert;
    private List<Polyline> polylines = null;
    private Polyline currentPolyline;
    private Polyline polyline = null;
    List<LatLng> latLngList = new ArrayList<>();
    List<Marker> markerOptionsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_map, container, false);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);


        alertBuilder = new AlertDialog.Builder(getContext());
        routingAlert = new AlertDialog.Builder(getContext());
        routingDialog = new ProgressDialog(getContext(), R.style.MyAlertDialogStyle);
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
        getDirections = view.findViewById(R.id.getRescueDirections);
        userIDTextView = view.findViewById(R.id.userID);
        send = alertView.findViewById(R.id.sendButton);
        fullname = alertView.findViewById(R.id.edit_fullname);
        ageText = alertView.findViewById(R.id.edit_age);
        bloodGroup = alertView.findViewById(R.id.edit_bloog_group);
        cancelDialog = alertView.findViewById(R.id.cancelButton);

        DatabaseReference mUser_ids = FirebaseDatabase.getInstance().getReference().child("user_ids");
        mUser_ids.orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //Toast.makeText(getContext(), ""+snapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
                user_id = snapshot.getValue().toString();
                if (!user_id.isEmpty()) {
                    for (int i = 0; i < user_id.length(); i++) {

                        Boolean flag = Character.isDigit(user_id.charAt(i));
                        if (flag) {
                            curr_user_id_char = user_id.charAt(i);
                            current_user_id = Integer.parseInt(String.valueOf(curr_user_id_char));
                            current_user_id = current_user_id;
                            //Toast.makeText(getContext(), ""+current_user_id, Toast.LENGTH_SHORT).show();
                            userIDMap.put("current_id", (current_user_id + 1));
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        sendLocationButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                alertDialog.show();
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        user_unique_identification_id = UUID.randomUUID().toString();
                        userIDTextView.setText(""+user_unique_identification_id);
                        person_name = fullname.getText().toString();
                        String age = ageText.getText().toString();
                        String blood_group = bloodGroup.getText().toString();
                        String location = getLocationLatLng(presentLocation).toString();


                        if (!person_name.isEmpty() && !age.isEmpty() && !blood_group.isEmpty()) {

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("Name", person_name);
                            map.put("Age", age);
                            map.put("blood_group", blood_group);
                            map.put("Location", location);

                            map1.put("user_unique_identification_id", user_unique_identification_id);
                            map1.put("Name", person_name);
                            map1.put("Age", age);
                            map1.put("Location", location);
                            map1.put("isRescued", false);
                            map1.put("rescued_By", "");
                            map1.put("user_id", (current_user_id + 1));

                            progressDialog.show();


                            FirebaseDatabase.getInstance().getReference("Locations").push()
                                    .setValue(map)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            alertDialog.dismiss();
                                        }
                                    });
                            FirebaseDatabase.getInstance().getReference("flask_app").push()
                                    .setValue(map1)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            alertDialog.dismiss();
                                            locationSentTemp = 1;
                                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
                                            builder1.setMessage("Successfully sent your details and location!!")
                                                    .setPositiveButton("Ok", null);
                                            AlertDialog alertDialog1 = builder1.create();
                                            alertDialog1.show();
                                        }
                                    });


                            FirebaseDatabase.getInstance().getReference("user_ids")
                                    .push()
                                    .setValue(userIDMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });

                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
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
                            , Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                }
                ////*****Assigning rescue team function*****/////
                getDirections.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        progressDialog.setTitle("Getting your rescue team");
                        progressDialog.setMessage("Please wait...");
                        progressDialog.show();

                        //drawPolyLine(start, end);
                        if (userIDTextView.getText().toString().length() != 0) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("flask_app");
                            reference.orderByChild("user_unique_identification_id").equalTo(""+userIDTextView.getText().toString()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        String rescuedBy = dataSnapshot.child("rescued_By").getValue(String.class);
                                        //Toast.makeText(getContext(), ""+rescuedBy, Toast.LENGTH_SHORT).show();
                                        if (rescuedBy.length() != 0) {
                                            getUserRescueTeamThroughName(user_unique_identification_id);
                                        }else{
                                            progressDialog.dismiss();
                                            Toast.makeText(getContext(), "Rescue team not assigned", Toast.LENGTH_SHORT).show();
                                            AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
                                            builder2.setMessage("Rescue team is not yet assigned to you.")
                                                    .setPositiveButton("Ok", null);
                                            AlertDialog alertDialog = builder2.create();
                                            alertDialog.show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //getUserRescueTeamThroughName(user_unique_identification_id);
                        } else {
                            progressDialog.dismiss();
                            //Toast.makeText(getContext(), "Enter details", Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
                            builder1.setMessage("Please send your location.")
                                    .setPositiveButton("Ok", null);
                            AlertDialog alertDialog = builder1.create();
                            alertDialog.show();
                        }


                    }
                });


                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                    }
                });
            }
        });
        showBaseCamps();

        return view;
    }

    private void get_rescueid_using_UUID() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("flask_app");
        reference.orderByChild("user_unique_identification_id").equalTo("" + user_unique_identification_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String location = dataSnapshot.child("Location").getValue(String.class);
                    if (location != null) {
                        location = location.substring(10, location.length() - 1);
                        String[] locationCoordinates = location.split(",", 2);
                        double latitude = Double.parseDouble(locationCoordinates[0]);
                        double longitude = Double.parseDouble(locationCoordinates[1]);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showBaseCamps() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Base_camps");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String location = dataSnapshot.child("Location").getValue(String.class);
                    String supplies = dataSnapshot.child("Supplies").getValue(String.class);
                    String base_camp_name = dataSnapshot.child("Name").getValue(String.class);
                    if (location != null && base_camp_name != null) {
                        location = location.substring(10, location.length() - 1);
                        String[] locationCoordinates = location.split(",", 2);
                        double latitude = Double.parseDouble(locationCoordinates[0]);
                        double longitude = Double.parseDouble(locationCoordinates[1]);
                        markBaseCamps(latitude, longitude, base_camp_name, supplies);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void markBaseCamps(double latitude, double longitude, String base_camp_name, String supplies) {
        MarkerOptions markerOptions1 = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Base camp: " + base_camp_name).snippet("Supplies: "+supplies).icon(BitmapDescriptorFactory.fromResource(R.drawable.base_camp_60x60));
        Marker marker = map.addMarker(markerOptions1);
        marker.showInfoWindow();
    }

    private void getUserRescueTeamThroughName(String UUID) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("flask_app");
        reference.orderByChild("user_unique_identification_id").equalTo(""+UUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String rescueTeamID = dataSnapshot.child("rescued_By").getValue(String.class);
                    if (rescueTeamID != null) {
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Rescue_staff");
                        reference1.orderByKey().equalTo("" + rescueTeamID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                for (DataSnapshot dataSnapshot1 : snapshot1.getChildren()) {
                                    String rescueStaffName = dataSnapshot1.child("Name").getValue(String.class);
                                    String rescueTeamCoordinates = dataSnapshot1.child("Location").getValue(String.class);
                                    if (rescueStaffName != null && rescueTeamCoordinates != null) {
                                        rescueTeamCoordinates = rescueTeamCoordinates.substring(10, rescueTeamCoordinates.length() - 1);
                                        String[] rescueTeamCoordinatesArray = rescueTeamCoordinates.split(",", 2);

                                        double latitude = Double.parseDouble(rescueTeamCoordinatesArray[0]);
                                        double longitude = Double.parseDouble(rescueTeamCoordinatesArray[1]);
                                        LatLng latLngend = new LatLng(latitude, longitude);
                                        markLocation(latitude, longitude, rescueStaffName, latLngend);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();
                                            }
                                        }, 500);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void markLocation(double latitude, double longitude, String name, LatLng end) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 14));
        //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.account);
        //MarkerOptions markerOptions1 = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Rescue team: " + name).icon(BitmapDescriptorFactory.fromResource(R.drawable.rescue_team_4_60x60));
        //Marker marker = map.addMarker(markerOptions1);
        //marker.showInfoWindow();
        if (presentLocation != null && end != null) {
            drawPolyLine(presentLocation, new LatLng(latitude, longitude), name);
        }
    }

    private void drawPolyLine(LatLng start, LatLng end, String name) {

        MarkerOptions startMarker = new MarkerOptions().position(start).title("Your location");
        MarkerOptions endMarker = new MarkerOptions().position(end).title("Rescue team: " + name).icon(BitmapDescriptorFactory.fromResource(R.drawable.rescue_team_4_60x60));
        Marker marker = map.addMarker(startMarker);
        Marker marker1 = map.addMarker(endMarker);
        marker1.showInfoWindow();

        latLngList.add(start);
        latLngList.add(end);
        markerOptionsList.add(marker);
        markerOptionsList.add(marker1);

        if (polyline != null) {
            polyline.remove();
        }
        PolylineOptions polylineOptions = new PolylineOptions().addAll(latLngList).clickable(true);
        polyline = map.addPolyline(polylineOptions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100 && grantResults.length > 0
                && (grantResults[0] + grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
            progressDialog.dismiss();
            getUserLocation();
        } else {
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
                destinationLocation = new LatLng(12.3073857, 76.6226786);
                Location location = task.getResult();
                if (location != null) {
                    presentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    getLocationLatLng(presentLocation);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location");
                    map.addMarker(markerOptions);
                } else {
                    LocationRequest locationRequest = new LocationRequest()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1);

                    LocationCallback locationCallback = new LocationCallback() {
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
    private LatLng getLocationLatLng(LatLng location) {
        return location;
    }

    private String getFullname(String fullname) {
        return fullname;
    }

    private String getAge(String age) {
        return age;
    }


    @Override
    public void applyTexts(String fullname, String age) {
        getFullname(fullname);
        getAge(age);
        if (fullname.length() > 0 && age.length() > 0) {
            //Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        designation = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
