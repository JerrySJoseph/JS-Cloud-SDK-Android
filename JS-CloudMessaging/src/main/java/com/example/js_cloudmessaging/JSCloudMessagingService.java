package com.example.js_cloudmessaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

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
    static String TAG="Socket Service";

    //Client factory
    private DeviceFactory mDeviceFactory = new DeviceFactory() {
        @Override
        public Device newClient() {
            return Device.CreateClientForThisDevice(getApplicationContext(),"my-auth-token");
        }
    };

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


    //override this method to return server URL
    public abstract String getServerUrl();

    //Called when new custom message recieved from server
    public abstract void onNewMessage(Object... args);

    //called when new push messaged recieved from server
    public abstract void onNewPush(Object... args);

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
            mSocket.on(Socket.EVENT_CONNECT,onConnectListener);
            mSocket.on(Socket.EVENT_DISCONNECT,onDisconnectListener);
            mSocket.on(Socket.EVENT_CONNECT_ERROR,onConnectErrorListener);
            //Change the event name for custom events
            mSocket.on("cloud-event-other",onEventListener);

            //Event listenere for push notification
            mSocket.on("cloud-event-push",onPushNotificationListener);
            mSocket.connect();
        }catch (JSCloudServerException e)
        {
            onCloudException(e);
        }

    }

    //Fired on successful connection
    Emitter.Listener onConnectListener= args -> {
        Log.e(TAG,"Connected to server. Your socket id is "+ mSocket.id());
        performClientHandshake();
    };

    //Fired when disconnected from server
    Emitter.Listener onDisconnectListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG,"Disconnected from Server.");
            onCloudDisconnect();
        }
    };

    //Fired when error occurs while connecting
    Emitter.Listener onConnectErrorListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG,"Connection Error "+ mSocket.id());
            onCloudException(new JSCloudServerException((Exception)args[0]));
        }
    };

    //Fired when push notification recieved
    Emitter.Listener onPushNotificationListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            onNewPush(args);
        }
    };

    //Fired on other events
    Emitter.Listener onEventListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
           onNewMessage(args);
        }
    };


    //Client handshake for registering client in server
    private void performClientHandshake()
    {
        Device me= mDeviceFactory.newClient();
        mSocket.emit("client-handshake", me.toJSONString(), new Ack() {
            @Override
            public void call(Object... args) {

                //Handshake successfull. Register all push and other event listener for communication
                Log.e(TAG,(String)args[0]);

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.off();
        mSocket.disconnect();

        Log.e(TAG,"Service -on destroy");
        Toast.makeText(getApplicationContext(),"Service destroyed",Toast.LENGTH_SHORT).show();
    }


}
