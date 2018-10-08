package com.example.nikol.growdsourcer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {


    private Button StartBtn;


    @Override
    protected void onResume(){
        super.onResume();
        StartBtn.setEnabled(true);
        //Check if user is logged in, if so, open menuactivity, otherwise do nothing
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            System.out.println("Login test print");
            Intent i = new Intent(this, MenuActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StartBtn = findViewById(R.id.button4);
        //Check if user is logged in, if so, open Menuactivity, otherwise do nothing
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            System.out.println("Login test print");
            Intent i = new Intent(this, MenuActivity.class);
            startActivity(i);
            finish();
        }
    }
    //Open Register activity
    //public void OpenRegister(View v){
    //    Intent i = new Intent(this, RegisterActivity.class);
    //    startActivity(i);
    //    StartBtn.setEnabled(false);
    //}
    //Open Login activity

    public void OpenLogin(View v){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}
