package com.example.js_cloudmessaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.jscloud_core.JSCloudApp;
import com.jstechnologies.notificationprovidermodule.NotificationProvider;

import java.net.URISyntaxException;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public abstract class JSCloudMessagingService extends Service
{
    static Socket mSocket=null;
    static String SERVER_URL=null;
    static String TAG="JS-Cloud-Messaging-Service";

    public JSCloudMessagingService() {
        mSocket= JSCloudApp.getInstance().getSocket();
        if(mSocket==null)
            throw new JSCloudServerException("JS Cloud App not initialized. Make sure you initialize JSCloudApp in application class and then call the service");
    }

    //Notification factory
    private NotificationFactory mNotificationFactory= new NotificationFactory(){
        @Override
        public Notification newNotification() {

            return new NotificationProvider(getApplicationContext())
                    .setTitle(getString(R.string.sDefaultNotifTitle))
                    .setBody(getString(R.string.sDefaultNotifContent))
                    .setChannelID(getString(R.string.sDefaultNotifChannel))
                    .setChannelName(getString(R.string.sDefaultNotifChannel))
                    .setNotificationIcon(R.drawable.ic_iot)
                    .setImportance(NotificationManager.IMPORTANCE_HIGH)
                    .getNotificationInstance();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if(!mSocket.connected())
                mSocket.connect();
            connectAndRegisterEvents();
            Log.e(TAG,"Service - created");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }


    //Called when new custom message recieved from server
    public abstract void onNewMessage(Object... args);

    //called when new push messaged recieved from server
    public abstract void onNewPush(NotificationPayload notificationPayload);

    //set custom foreground Notification (compulsory)
    public abstract Notification getNotification();

    //called when any exception occurs
    public abstract void onCloudException(JSCloudServerException exception);

    //optional overrides
    public void onCloudDisconnect(){ }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(101,getNotification());
        Log.e(TAG,"Service -on start");
        return START_STICKY;
    }

    //returns default notification object
    public Notification getDefaultNotification()
    {
        return mNotificationFactory.newNotification();
    }

    //Register socket Events
    private void connectAndRegisterEvents(){
        try{
            //Change the event name for custom events
            mSocket.on("cloud-event-other",onEventListener);

            //Event listenere for push notification
            mSocket.on("js-cloud-messaging-notify",onPushNotificationListener);
            mSocket.connect();
        }catch (JSCloudServerException e)
        {
            onCloudException(e);
        }

    }


    //Fired when push notification received
    Emitter.Listener onPushNotificationListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String json=(String)args[0];
            onNewPush(NotificationPayload.fromJSON(json));
        }
    };

    //Fired on other events
    Emitter.Listener onEventListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
           onNewMessage(args);
        }
    };


}
