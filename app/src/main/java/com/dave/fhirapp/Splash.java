package com.dave.fhirapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.dave.fhirapp.patient.add.MainActivity;
import com.dave.fhirapp.patient.list_data.PatientList;

public class Splash extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler=new Handler();
        handler.postDelayed(() -> {
            Intent intent=new Intent(Splash.this, PatientList.class);
            startActivity(intent);
            finish();
        },3000);
    }
}