package com.example.jscloudapi;

import android.app.Application;

import com.example.jscloud_core.JSCloudApp;


public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JSCloudApp.init(getApplicationContext(),"http://192.168.1.26:3001");
    }

}
