package com.example.jscloudapi;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     //   startService(new Intent(this,ConnService.class));

        int i=0;
    }
    public void startService(View view) {
        Intent serviceIntent = new Intent(this, ConnService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

    }
    public void stopService(View view) {
        Intent serviceIntent = new Intent(this, ConnService.class);
        stopService(serviceIntent);
    }


    public void sendCommand(View view) {

    }
}