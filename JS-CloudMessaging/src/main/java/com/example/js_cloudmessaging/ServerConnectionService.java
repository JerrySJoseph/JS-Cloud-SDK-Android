package com.example.js_cloudmessaging;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.jstechnologies.notificationprovidermodule.NotificationProvider;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;
import io.socket.engineio.client.transports.WebSocket;

public abstract class ServerConnectionService extends Service
{
    static Socket mSocket=null;
    static String SERVER_URL=null;
    static String TAG="Socket Service";
    static String sDefaultNotifTitle="IoT Service";
    static String sDefaultNotifContent="IoT Connected Service is active now. You will receive realtime notifications when this service is active";
    static String sDefaultChannel="js-iot-notif-service-channel";


    private ClientFactory mClientFactory= new ClientFactory() {
        @Override
        public Client newClient() {
            return Client.CreateClientForThisDevice(getApplicationContext(),"auth-token");
        }
    };

    private NotificationFactory mNotificationFactory= new NotificationFactory(){
        @Override
        public Notification newNotification() {

            return new NotificationProvider(getApplicationContext())
                    .setTitle(sDefaultNotifTitle)
                    .setBody(sDefaultNotifContent)
                    .setChannelID(sDefaultChannel)
                    .setChannelName(sDefaultChannel)
                    .setNotificationIcon(R.drawable.ic_iot)
                    .setImportance(NotificationManager.IMPORTANCE_HIGH)
                    .getNotificationInstance();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if(SERVER_URL==null || SERVER_URL.isEmpty())
            SERVER_URL=getServerUrl();

        try {
            if(mSocket==null)
                mSocket= IO.socket(SERVER_URL);
            connectAndRegisterEvents();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Log.e(TAG,"Service - created");
        Toast.makeText(getApplicationContext(),"Service created",Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    public abstract String getServerUrl();
    public abstract void onNewMessage(Object... args);
    public abstract void onNewPush(Object... args);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(101,getNotification());

        Log.e(TAG,"Service -on start");

        return START_STICKY;
    }

    public abstract Notification getNotification();

    public Notification getDefaultNotification()
    {
        return mNotificationFactory.newNotification();
    }

    private void connectAndRegisterEvents(){
        try{
            mSocket.on(Socket.EVENT_CONNECT,onConnectListener);
            mSocket.on(Socket.EVENT_DISCONNECT,onDisconnectListener);
            mSocket.on(Socket.EVENT_CONNECT_ERROR,onConnectErrorListener);
            mSocket.connect();
        }catch (JSCloudServerException e)
        {
            onCloudException(e);
        }

    }
    public abstract void onCloudException(JSCloudServerException exception);
    public void onCloudDisconnect(){ }

    Emitter.Listener onConnectListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG,"Connected to server. Your socket id is "+ mSocket.id());
            performClientHandshake();
        }
    };
    Emitter.Listener onDisconnectListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG,"Disconnected from Server.");
        }
    };
    Emitter.Listener onConnectErrorListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG,"Connection Error "+ mSocket.id());
            onCloudException(new JSCloudServerException((Exception)args[0]));
        }
    };
    Emitter.Listener onEventListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
           onNewMessage(args);
        }
    };
    Emitter.Listener onPushNotificationListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            onNewPush(args);
        }
    };

    private void performClientHandshake()
    {
        Client me=mClientFactory.newClient();
        mSocket.emit("client-handshake", me.toJSONString(), new Ack() {
            @Override
            public void call(Object... args) {
                Log.e(TAG,(String)args[0]);
                mSocket.on("cloud-event-other",onEventListener);
                mSocket.on("cloud-event-push",onPushNotificationListener);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        Log.e(TAG,"Service -on destroy");
        Toast.makeText(getApplicationContext(),"Service destroyed",Toast.LENGTH_SHORT).show();
    }


}
