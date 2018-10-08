package com.example.nikol.growdsourcer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class TaskActivity extends AppCompatActivity {
    TextView BodyTv;
    TextView LabelTv;
    TextView UrlTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        System.out.println("TESTI7");
        //unfold notification info from MenuActivity
        Bundle extras = getIntent().getExtras();
        System.out.println("Extras: " + extras);
        if(extras!=null) {

            System.out.println("TESTI8");

            //Unpack extras bundle
            String Url = extras.getString("Url");
            String Body = extras.getString("TaskBody");
            String Title = extras.getString("Title");
            String Location = extras.getString("TaskLocation");
            System.out.println("Tasklocation: " + Location);
            System.out.println("url: " + Url);
            System.out.println("TaskBody: " + Body);
            System.out.println("Title: " + Title);

            //Set Task screen textviews
            BodyTv = (TextView) findViewById(R.id.BodyTv);
            LabelTv = (TextView) findViewById(R.id.LabelTv);
            UrlTv = (TextView) findViewById(R.id.UrlTv);
            UrlTv.setText(Url);
            BodyTv.setText(Body);
            LabelTv.setText(Title);

            //Pass values back to MenuActivity
            System.out.println("TESTI9");

        }
    }
}
