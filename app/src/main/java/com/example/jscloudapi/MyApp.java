package com.example.jscloudapi;

import android.app.Application;
import android.widget.Toast;

import com.example.js_auth.JSCloudAuth;
import com.example.js_auth.interfaces.RevokedAccessListener;
import com.example.jscloud_core.interfaces.CloudServerConnectionCallback;
import com.example.jscloud_core.JSCloudApp;


public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        JSCloudApp.setCloudServerConnectionCallback(mCloudConnectionCallback);
        JSCloudApp.init(getApplicationContext(),"http://192.168.1.26:3001");
      /* JSCloudAuth.setGoogleClientID("629362459295-krpl7a5s8cgt5b96s25jabtiotlvpkq1.apps.googleusercontent.com");

        handleAccessRevoked();*/
    }

    CloudServerConnectionCallback mCloudConnectionCallback= new CloudServerConnectionCallback() {
        @Override
        public void onConnected() {
            Toast.makeText(getApplicationContext(),"Connected to server",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected() {
            Toast.makeText(getApplicationContext(),"Disconnected from server",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectionFailed(String reason) {
            Toast.makeText(getApplicationContext(),reason,Toast.LENGTH_SHORT).show();
        }
    };
    public void handleAccessRevoked()
    {
        JSCloudAuth.getInstance().addOnRevokeAccessListener(new RevokedAccessListener() {
            @Override
            public void onAccessRevoked(String message) {
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
            }
        });
    }

}
