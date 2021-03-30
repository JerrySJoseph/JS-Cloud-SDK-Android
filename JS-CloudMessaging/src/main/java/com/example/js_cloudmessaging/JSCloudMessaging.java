package com.example.js_cloudmessaging;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.strictmode.CleartextNetworkViolation;
import android.util.Log;

import androidx.annotation.RequiresApi;

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

public class JSCloudMessaging {

    Socket mSocket;

    String TAG="JS-CLOUD_MESSAGING";

    private static JSCloudMessaging mInstance=null;

    static String SERVER_URL;
    private static Context mContext;

    private ClientFactory mClientFactory= new ClientFactory() {
        @Override
        public Client newClient() {
            return Client.CreateClientForThisDevice(mContext,"auth-token");
        }
    };

    public static void init(Context context,String serverURL)
    {
        mContext=context;
        SERVER_URL=serverURL;
    }

    public static synchronized JSCloudMessaging getInstance()
    {
        if(mInstance==null)
            mInstance= new JSCloudMessaging(true);
        return mInstance;
    }
    JSCloudMessaging(boolean b)
    {
       Intent serviceIntent=new Intent(mContext,ServerConnectionService.class);
       serviceIntent.putExtra("SERVER_URL",SERVER_URL);

    }
    JSCloudMessaging()
    {
        if(SERVER_URL==null || SERVER_URL.isEmpty())
            throw new JSCloudServerException("Server URL is null or Empty. This happens when JSCloud is not properly initialized. Try calling JSCloudMessaging.init(context,ServerURL) " +
                    "before calling any other method.");
        try {
            mSocket= IO.socket(SERVER_URL);
            connectAndRegisterEvents();
        } catch (URISyntaxException e) {
            throw new JSCloudServerException(e);
        }
    }
    private void connectAndRegisterEvents()
    {

        mSocket.on(Socket.EVENT_CONNECT,onConnectListener);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnectListener);
        mSocket.on(Socket.EVENT_CONNECT_ERROR,onConnectErrorListener);

        mSocket.connect();

    }
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
            throw new JSCloudServerException((Exception)args[0]);
        }
    };

    private void performClientHandshake()
    {
        Client me=mClientFactory.newClient();
        mSocket.emit("client-handshake", me.toJSONString(), new Ack() {
            @Override
            public void call(Object... args) {
                Log.e(TAG,(String)args[0]);
            }
        });
    }

}
