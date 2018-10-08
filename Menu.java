package com.example.nikol.growdsourcer;

import android.Manifest;
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

    @Override
    protected void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //Init Onesignal function
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new TappedNotificationHandler())
                .init();
        //CREATE USER TO DATABASE



        //Buttons
        LogOutBtn = (Button) findViewById(R.id.LogOutBtn);
        googleMapBtn = (Button) findViewById(R.id.googleMapBtn);
        statusSwitch = (Switch) findViewById(R.id.statusSwitch);
        TaskBtn = (Button) findViewById(R.id.TaskBtn);

        //"Ready to Work" Status "False" as default
        statusSwitch.setChecked(false);
        status = statusSwitch.isChecked();

        System.out.println("READY TO WORK: " + status);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference().child("users");
        FirebaseUser user = mAuth.getCurrentUser();
        UserID = user.getUid();
        System.out.println("CURRENT USER ID: " + UserID);
        myRef.child(UserID).child("Ready To Work").setValue(status);
        myRef.child(UserID).child("Token Lifespan").setValue("Alive");
        //Create User Object and send it to Database
        //UserObject userObject = new UserObject(UserID);



        //Sign out method
        LogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child(UserID).child("Token Lifespan").setValue("Killed");
                mAuth.signOut();
                finish();
            }
        });
        //Status update to db
        statusSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean on = ((Switch) v).isChecked();
                if(on) {
                    //Do something when switch is on/checked
                    myRef.child(UserID).child("Ready To Work").setValue(true);
                } else{
                //Do something when switch is off/unchecked
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
				if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
					//Bring Working status info for maps activity
					Switch statusSwitch = (Switch) findViewById(R.id.statusSwitch);
					Boolean status = statusSwitch.isChecked();
					Intent startIntent = new Intent(getApplicationContext(), MapsActivity.class);
					startIntent.putExtra("STATUS", status);
					startActivity(startIntent);
				}
				else
				{	
					if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity, Manifest.permission.READ_CONTACTS))
					{
						// Show an explanation to the user *asynchronously* -- don't block
						// this thread waiting for the user's response! After the user
						// sees the explanation, try again to request the permission.
					}
					else
					{
						// No explanation needed; request the permission
						Toast.makeText(MenuActivity.this, "GPS Permission needed for map feature!", Toast.LENGTH_SHORT).show();
						ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
					}			
				}
            }
        });

        //Current Task button
        TaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent startIntent = new Intent(getApplicationContext(), TaskActivity.class);
                startActivity(startIntent);
            }
        });
    }

    //Class for notification handler

    public class TappedNotificationHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.

        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            OSNotificationAction.ActionType actionType = result.action.type;
            JSONObject data = result.notification.payload.additionalData;
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
            Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
            //   if you are calling startActivity above.
     /*
        <application ...>
          <meta-data android:name="com.onesignal.NotificationOpened.DEFAULT" android:value="DISABLE" />
        </application>
     */
        }
    }

    @Override public void onStop() {
        super.onStop();
        if (authListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
        }
    }
}

    © 2018 GitHub, Inc.
    Terms
    Privacy
    Security
    Status
    Help

    Contact GitHub
    Pricing
    API
    Training
    Blog
    About

