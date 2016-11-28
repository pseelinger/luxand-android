package com.luxand.facerecognition;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.Manifest;


public class HomeActivity extends AppCompatActivity {

    Intent intent = getIntent();

    public Button but1;
    public Button but2;

    public void init() {
        but1=(Button) findViewById(R.id.but1);
        but2=(Button) findViewById(R.id.but2);
        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toy = new Intent(HomeActivity.this, CreateAccount.class);

                startActivity(toy);

            }
        });


        but2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent toy2 = new Intent(HomeActivity.this, login_activity.class);
                startActivity(toy2);
            }
        });


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        init();

    }





}
