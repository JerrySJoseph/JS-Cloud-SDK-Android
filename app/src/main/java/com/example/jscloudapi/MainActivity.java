package com.example.jscloudapi;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.js_auth.JSCloudAuth;
import com.example.js_auth.JSCloudAuthActivity;
import com.example.js_auth.Models.JSCloudUser;
import com.example.js_auth.interfaces.AuthResponse;
import com.example.jscloud_core.JSCloudApp;


public class MainActivity extends JSCloudAuthActivity {


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startService(View view) {
       JSCloudAuth.getInstance().signInWithGoogle(this);
    }

    public void stopService(View view) {
        JSCloudApp.disconnect();
    }


    public void sendCommand(View view) {

    }

    @Override
    protected void onAuthResponse(boolean isAuthorized, JSCloudUser user) {
        Toast.makeText(this,user.getName(),Toast.LENGTH_SHORT).show();
    }
}