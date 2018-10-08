package com.example.nikol.growdsourcer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.support.v4.content.ContextCompat;
import android.os.IBinder;
import android.content.Intent;
import android.Manifest;
import android.location.Location;
import android.content.pm.PackageManager;
import android.app.Service;

public class LocationTracker extends Service {

    private static final String TAG = LocationTracker.class.getSimpleName();
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private DatabaseReference mDatabase;
    private DatabaseReference usersRef;
    private String UserID;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        requestLocationUpdates();
    }



    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        System.out.println("TESTI13");
        //Check permission
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            System.out.println("TESTI12");
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    System.out.println("TESTI14");
                    //Get UserID:
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(user != null){
                        String UserID = user.getUid();

                        //get database reference
                        myRef = FirebaseDatabase.getInstance().getReference().child("users");

                        //Get a reference to the database, so your app can perform read and write operations//
                        //DatabaseReference ref = mFirebaseDatabase.getInstance().getReference().child(UserID).child("Location");
                        Location location = locationResult.getLastLocation();
                        System.out.println("TESTI: " + location);
                        if (location != null) {
                            System.out.println("TESTI10");
                            //Save the location data to the database//
                            //For Extra information:
                            //myRef.child(UserID).child("Location Info").setValue(location);

                            //For only coordinates:
                            double Longitude = location.getLongitude();
                            double Latitude = location.getLatitude();

                            myRef.child(UserID).child("Location Info").child("Longitude").setValue(Longitude);
                            myRef.child(UserID).child("Location Info").child("Latitude").setValue(Latitude);

                        }
                    }
                    else
                    {
                        System.out.println("Auth not found");

                    }
                }
            }, null);
        }
    }
}
