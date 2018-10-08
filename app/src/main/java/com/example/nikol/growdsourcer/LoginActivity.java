package com.example.nikol.growdsourcer;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    //Auth reference:
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private Button btnAnon;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
        //Check if user is logged in, if so, open menuactivity, otherwise do nothing
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            System.out.println("Login test print");
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnAnon = findViewById(R.id.btnAnon);
        btnAnon.setEnabled(true);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            System.out.println("Login test print");
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Check if user is logged in, if so, open menuactivity, otherwise do nothing
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            System.out.println("Login test print");
            finish();
        }

        btnAnon = findViewById(R.id.btnAnon);
        mAuth = FirebaseAuth.getInstance();

        //Check if user is already logged in:

        //Sign in anonymously
        btnAnon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Task<AuthResult> resultTask = mAuth.signInAnonymously();
                btnAnon.setEnabled(false);
                resultTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        startActivity(new Intent(LoginActivity.this, MenuActivity.class));

                    }

                });
            }
        });

        //Sign in pop-up:
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    //Toast.makeText(LoginActivity.this, "Signed In Anonymously", Toast.LENGTH_SHORT).show();
                    System.out.println("Signed in");
                }
            }
        };
    }
}
