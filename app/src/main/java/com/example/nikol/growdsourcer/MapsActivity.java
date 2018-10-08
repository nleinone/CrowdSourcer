package com.example.nikol.growdsourcer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private DatabaseReference mDatabase;
    private String UserID;

    public GoogleMap mMap;
    double Latitude;
    double Longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //ENABLE GPS PERMISSION:
        mMap = googleMap;
        enableLocationIfGranted();
        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);

        //Start updating locations to FireBase database:
        LocationUpdater();
        System.out.println("TESTI9");
        //MAP SETUPS:
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.setMinZoomPreference(11);

        //Get UserID:
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String UserID = user.getUid();

        //get database reference
        myRef = FirebaseDatabase.getInstance().getReference().child("users");
        //Get location from database, and move the camera in that specific location.
        myRef.child(UserID).child("Location Info").addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null) {

                    Latitude = dataSnapshot.child("Latitude").getValue(double.class);
                    Longitude = dataSnapshot.child("Longitude").getValue(double.class);

                    //Update location info to Firebase database
                    //DEBUG PRINTS
                    System.out.println("LATITUDE: " + Latitude);
                    System.out.println("LONGITUDE: " + Longitude);


                    LatLng curloc = new LatLng(Latitude, Longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(curloc));
                    //SET ZOOM
                    //mMap.setMinZoomPreference(15);
                }
                else{
                    System.out.println("ERROR WITH GPS!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("ERR");
            }
        });

        //Get status from main activity
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            boolean status = extras.getBoolean("STATUS");
            System.out.println("READY TO WORK: " + status);
        }

        // Attach an listener to listen data changes for marker update
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UpdateMarkers(snapshot);
                TaskLocationMarker();
                System.out.println("TESTIX");


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Error Occurred: " + databaseError);
            }
        });

    }

    //MAP AND PERMISSION FUNCTIONS

    //Map self location button
    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
            new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    //mMap.setMinZoomPreference(15);

                    return false;
                }
            };

    //If location permission is not enabled, ask it
    private void enableLocationIfGranted() {
        //If fine location access is NOT "PERMISSION GRANTED"...
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //...Request this permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
        else if (mMap != null) {

            //Create blue "dot" in your location on the google map
            mMap.setMyLocationEnabled(true);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    //Update Location information to Firebase database
    private void LocationUpdater(){
        System.out.println("TESTI9");
        startService(new Intent(this, LocationTracker.class));
        //Toast notification about location being tracked!
        Toast.makeText(this, "Your GPS location is now shared!", Toast.LENGTH_SHORT).show();
    }

    //Draw Green and Red Circles on the map, according to the user locations
    public void DrawCircles(String lat, String lon, String status, String token_status){

        CircleOptions circleOptions = new CircleOptions();
        double latitude = Double.parseDouble(lat);
        double longitude = Double.parseDouble(lon);
        circleOptions.center(new LatLng(latitude,longitude));
        circleOptions.radius(10);


        //DEBUGGING SECTION
        boolean test_value = (status.equals("true")) && (!token_status.equals("Killed"));
        System.out.println("if statement boolean:" + test_value);
        System.out.println("status boolean:" + status.equals("true"));
        System.out.println("token_status boolean:" + !token_status.equals("Killed"));
        //DEBUGGING SECTION END

        if((status.equals("true")) && (!token_status.equals("Killed"))) {
            System.out.println("if");
            circleOptions.fillColor(Color.GREEN);
            circleOptions.strokeWidth(6);
            mMap.clear();
            mMap.addCircle(circleOptions);
        }else if ((status.equals("false")) && (!token_status.equals("Killed"))) {
            System.out.println("Else if");
            circleOptions.fillColor(Color.RED);
            circleOptions.strokeWidth(6);
            mMap.clear();
            mMap.addCircle(circleOptions);
        }else{
            System.out.println("Killed token iterated!");
            //mMap.clear();
        }
    }

    //Update other users markers:
    private void UpdateMarkers(DataSnapshot snapshot){

        //User ID for Self flag
        mAuth = FirebaseAuth.getInstance();

        //Iterate through Datasnapshot (database), users (all the userIDs and user (Single userID).

        Iterator<DataSnapshot> users = snapshot.getChildren().iterator();
        while(users.hasNext()){
            DataSnapshot user = users.next();
            try {
                String token_status = user.child("Token Lifespan").getValue().toString();
                String otherUser = user.getKey();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    UserID = currentUser.getUid();
                    System.out.println("OTHER USER: " + otherUser + "CURRENT USER: " + UserID);
                    if (!otherUser.equals(UserID) && (token_status != "Killed")) {
                        String status;
                        String longitude, latitude;
                        longitude = user.child("Location Info").child("Longitude").getValue().toString();
                        latitude = user.child("Location Info").child("Latitude").getValue().toString();
                        status = user.child("Ready To Work").getValue().toString();
                        System.out.println("GOT VALUES! LONGITUDE: " + longitude + "\n" + "LATITUDE: " + latitude + "\n" + "STATUS: " + status);
                        //Draw circles on the map
                        DrawCircles(latitude, longitude, status, token_status);

                    }
                }
            }
            catch(Exception e){
                System.out.println("User data not ready");
            }



        }
    }
    public void TaskLocationMarker(){

        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference().child("users");
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            String UserID = user.getUid();
            //Get task location from db and mark the map.
                myRef.child(UserID).child("Task Data").child("Task Location").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String TaskLocation;
                        double Latitude;
                        double Longitude;
                        String[] location_split;

                        try {
                            System.out.println("TaskLocationMarker ERR4");
                            //ASSUME THE ONESIGNAL FORMAT TO BE "LATITUDE,LONGITUDE" in Notification metadata
                            TaskLocation = dataSnapshot.getValue(String.class);
                            location_split = TaskLocation.split(",");
                            Latitude = Double.parseDouble(location_split[0]);
                            Longitude = Double.parseDouble(location_split[1]);
                            LatLng current_location = new LatLng(Latitude, Longitude);

                            //mMap.clear();

                            mMap.addMarker(new MarkerOptions().position(current_location).title("Task Location!"));
                        }
                        catch (Exception e) {
                            //If format is wrong, set task location to null
                            FirebaseUser user = mAuth.getCurrentUser();
                            mAuth = FirebaseAuth.getInstance();
                            String UserID = user.getUid();
                            myRef.child(UserID).child("Task Data").child("Task Location").setValue(null);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println("TaskLocationMarker ERR6");
                }
            });
        }
        else{
            System.out.println("TaskLocationMarker ERR7");
        }
    }
}

//REFERENCES: http://www.zoftino.com/android-show-current-location-on-map-example
//            https://stackoverflow.com/questions/18425141/android-google-maps-api-v2-zoom-to-current-location
//            https://stackoverflow.com/questions/23104089/googlemap-getmylocation-cannot-get-current-location
//            https://www.mytrendin.com/periodically-send-gps-location-firebase-custom-time-frame/
//            https://www.androidauthority.com/create-a-gps-tracking-application-with-firebase-realtime-databse-844343/