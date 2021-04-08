package com.example.jscloud_core;

import android.content.Context;
import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class JSCloudApp {

    private static JSCloudApp mInstance;
    private static Context mContext;
    private static String TAG="JS-Cloud-Auth";
    private static Socket mSocket;


    public static synchronized JSCloudApp getInstance()
    {
        if(mContext==null)
            throw new IllegalStateException("JS Cloud App not initialized");
        if(mInstance==null)
            mInstance=new JSCloudApp();
        return mInstance;
    }

    public static void init(Context context,String SERVER_URL){

        try {
            mContext=context;
            mSocket= IO.socket(SERVER_URL);

            //register all events
            mSocket.on(Socket.EVENT_CONNECT,onConnectListener);
            mSocket.on(Socket.EVENT_DISCONNECT,onDisconnectListener);
            mSocket.on(Socket.EVENT_CONNECT_ERROR,onConnectErrorListener);

            //Connect to Server
            mSocket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    //Fired on successful connection
    static Emitter.Listener onConnectListener= args -> {
        Log.e(TAG,"Connected to server. Your socket id is "+ mSocket.id());
    };

    //Fired when disconnected from server
    static Emitter.Listener onDisconnectListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG,"Disconnected from Server.");
        }
    };

    //Fired when error occurs while connecting
    static Emitter.Listener onConnectErrorListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG,"Connection Error "+ mSocket.id());
        }
    };


    public static void disconnect()
    {
        mSocket.off();
        mSocket.disconnect();
    }

    public Socket getSocket()
    {
        return mSocket;
    }
    public Context getContext()
    {
        return mContext;
    }
}
