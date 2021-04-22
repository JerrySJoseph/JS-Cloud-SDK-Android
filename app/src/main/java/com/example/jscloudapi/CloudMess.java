package com.example.jscloudapi;

import android.app.Notification;
import android.util.Log;

import com.example.js_cloudmessaging.JSCloudMessagingService;
import com.example.js_cloudmessaging.JSCloudServerException;
import com.example.js_cloudmessaging.NotificationPayload;

public class CloudMess extends JSCloudMessagingService {
    @Override
    public void onNewMessage(Object... args) {

    }

    @Override
    public void onNewPush(NotificationPayload payload) {
        Log.e("rec","rec");
    }

    @Override
    public Notification getNotification() {
        return getDefaultNotification();
    }

    @Override
    public void onCloudException(JSCloudServerException exception) {

    }
}
