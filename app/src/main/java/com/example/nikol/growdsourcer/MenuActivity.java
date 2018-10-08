package com.example.nikol.growdsourcer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "AddUserToDatabase";
    private String UserID;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private Button LogOutBtn;
    private Button googleMapBtn;
    private Switch statusSwitch;
    private Button TaskBtn;

    private boolean status;

    private String Title;
    private String TaskBody;
    private String TaskLocation;
    private String Url;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onResume(){
        super.onResume();

        LogOutBtn = findViewById(R.id.LogOutBtn);
        googleMapBtn = findViewById(R.id.googleMapBtn);
        statusSwitch = findViewById(R.id.statusSwitch);
        TaskBtn = findViewById(R.id.TaskBtn);

        LogOutBtn.setEnabled(true);
        googleMapBtn.setEnabled(true);
        statusSwitch.setEnabled(true);
        TaskBtn.setEnabled(true);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //Init Onesignal function
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new TappedNotificationHandler())
                .init();

        //Ask GPS permission
        enableLocationIfGranted();

        //Buttons
        LogOutBtn = findViewById(R.id.LogOutBtn);
        googleMapBtn = findViewById(R.id.googleMapBtn);
        statusSwitch = findViewById(R.id.statusSwitch);
        TaskBtn = findViewById(R.id.TaskBtn);

        //Firebase reference variables
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference().child("users");
        FirebaseUser user = mAuth.getCurrentUser();
        status = statusSwitch.isChecked();

        if(user != null)
        {
            UserID = user.getUid();
            System.out.println("CURRENT USER ID: " + UserID);
            myRef.child(UserID).child("Ready To Work").setValue(status);
            myRef.child(UserID).child("Token Lifespan").setValue("Alive");
            myRef.child(UserID).child("Location Info").child("Latitude").setValue(65.055499778);
            myRef.child(UserID).child("Location Info").child("Longitude").setValue(25.459664828);
        }
        else
        {
            System.out.println("Auth not found");
            finish();
        }

        //Create User Object and send it to Database
        System.out.println("FIREBASE AUTH: " + FirebaseAuth.getInstance());
        //Sign out method
        LogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
                    LogOutBtn.setEnabled(false);
                    Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    myRef.child(UserID).child("Token Lifespan").setValue("Killed");
                    mAuth.signOut();
                    finish();
                }
                catch(Exception e)
                {
                    System.out.println("Exception occurred: " + e);
                    finish();
                }

            }
        });

        //Status switch button update to db
        statusSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean on = ((Switch) v).isChecked();
                if(on) {
                    //Do something when switch is on/checked
                    //Turn on Notifications
                    OneSignal.setSubscription(true);

                    //Update status to DB
                    myRef.child(UserID).child("Ready To Work").setValue(true);

                } else{
                //Do something when switch is off/unchecked

                    //Turn off OneSignal Notifications
                    OneSignal.setSubscription(false);

                    //Save switch status to DB
                    myRef.child(UserID).child("Ready To Work").setValue(false);

                }
            }
        });

        //Sign out pop-up:
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user == null) {
                    Toast.makeText(MenuActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
                }
            }
        };

        //Read from DB:

        myRef.addValueEventListener((new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                Log.d(TAG, "onDataChange: Added info to DB: \n" + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value." + databaseError.toException());
            }
        }));

        //GoogleMaps button
        googleMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                //When user presses the map button, open map if permission granted for gps.
                if (permission == PackageManager.PERMISSION_GRANTED)
                {
                    //Bring Working status info for maps activity
                    Switch statusSwitch = findViewById(R.id.statusSwitch);
                    Boolean status = statusSwitch.isChecked();
                    googleMapBtn.setEnabled(false);
                    Intent startIntent = new Intent(getApplicationContext(), MapsActivity.class);
                    startIntent.putExtra("STATUS", status);
                    startActivity(startIntent);
                }
                //If permission is not granted, show dialog window and ask permission.
                else
                {
                    showDialog();
                }
            }
        });

        //Current Task button
        TaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child(UserID).child("Task Data").addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        TaskBtn.setEnabled(false);
                        Intent startIntent = new Intent(getApplicationContext(), TaskActivity.class);
                        Title = dataSnapshot.child("Title").getValue(String.class);
                        TaskBody = dataSnapshot.child("Task Body").getValue(String.class);
                        TaskLocation = dataSnapshot.child("Task Location").getValue(String.class);
                        Url = dataSnapshot.child("Url").getValue(String.class);
                        System.out.println("Body: " + TaskBody);
                        startIntent.putExtra("Title", Title);
                        startIntent.putExtra("TaskBody", TaskBody);
                        startIntent.putExtra("Url", Url);
                        startIntent.putExtra("TaskLocation", TaskLocation);
                        startActivity(startIntent);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("ERR");
                    }
                });
            }
        });
    }

    //If location permission is not enabled, ask it
    private void enableLocationIfGranted() {
        //If fine location access is NOT "PERMISSION GRANTED"...
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // SHOW MESSAGE FOR DENYING THE PERMISSION!
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
            else{
                //...Request permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    //Class for notification handler
    public class TappedNotificationHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        //OneSignal data init
        private String Url;
        private String Body;
        private String Title;
        private String Location;

        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            OSNotificationAction.ActionType actionType = result.action.type;
            JSONObject data = result.notification.payload.additionalData;
            Title = result.notification.payload.title;
            Body = result.notification.payload.body;
            System.out.println("TESTI1");
            if (data != null) {
                System.out.println("TESTI2");
                System.out.println("ONESIGNAL DATA: " + data.toString());
                Url = data.optString("Url", null);
                Location = data.optString("Location", null);
                System.out.println("TITLE: " + Title);
                System.out.println("BODY: " + Body);
            }
            System.out.println("TESTI3");
            if (actionType == OSNotificationAction.ActionType.ActionTaken) {
                System.out.println("TESTI6");
                Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);
            }

            String customKey;

            if (data != null) {
                customKey = data.optString("customkey", null);
                if (customKey != null)
                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
            }

            if (actionType == OSNotificationAction.ActionType.ActionTaken)
                Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);

            // The following can be used to open an Activity of your choice.
            // Replace - getApplicationContext() - with any Android Context.
            Intent menuintent = new Intent(getApplicationContext(), MenuActivity.class);
            startActivity(menuintent);
            Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            System.out.println("TESTI4");
            if(data!=null) {
                System.out.println("TESTI5");
                //Bring OneSignal data to Taskactivity screen
                intent.putExtra("Url", Url);
                intent.putExtra("TaskBody", Body);
                intent.putExtra("Title", Title);
                intent.putExtra("TaskLocation", Location);
                //Update Task data to Database
                myRef.child(UserID).child("Task Data").child("Url").setValue(Url);
                myRef.child(UserID).child("Task Data").child("Title").setValue(Title);
                myRef.child(UserID).child("Task Data").child("Task Body").setValue(Body);
                myRef.child(UserID).child("Task Data").child("Task Location").setValue(Location);
                startActivityForResult(intent, 10);
            }
            System.out.println("TESTI6");
        }
    }

    public void showDialog(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS permission needed.");

        // Setting Dialog Message
        alertDialog.setMessage("Worker Map uses your location information to track your location for others. To use this feature, you need to enable GPS for your phone.");

        // Setting Positive "Yes" Btn
        alertDialog.setPositiveButton("Allow GPS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        ActivityCompat.requestPermissions(MenuActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    }
        });
        // Setting Negative "NO" Btn
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        Toast.makeText(getApplicationContext(),"GPS not permitted.", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

        // Showing Alert Dialog
        alertDialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
        }
    }
}

//REFERENCES: https://www.protechtraining.com/blog/post/618