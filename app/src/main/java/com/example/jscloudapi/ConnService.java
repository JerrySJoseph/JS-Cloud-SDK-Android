package com.example.jscloudapi;

import android.app.Notification;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.js_cloudmessaging.JSCloudServerException;
import com.example.js_cloudmessaging.ServerConnectionService;
import com.jstechnologies.notificationprovidermodule.NotificationProvider;

public class ConnService extends ServerConnectionService {

    @Override
    public void onNewMessage(Object... args) {
        showNotification((String)args[0],(String)args[1]);
    }

    @Override
    public void onNewPush(Object... args) {
        Log.e("SOCKET","New push");
        showNotification((String)args[0],(String)args[1]);
    }

    @Override
    public Notification getNotification() {
        return getDefaultNotification();
    }

    @Override
    public void onCloudException(JSCloudServerException exception) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ConnService.this,exception.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public String getServerUrl() {
        return "http://192.168.1.26:3000/";
    }

    private void showNotification(String title,String message)
    {
        new NotificationProvider(this)
                .setTitle(title)               //Required
                .setBody(message)      //Required
                .setAutocancel(true)
                .setNotificationIcon(R.mipmap.ic_launcher)
                .setChannelID("Channel_id")
                .setChannelName("Channel_name")
                .show();
    }
}
