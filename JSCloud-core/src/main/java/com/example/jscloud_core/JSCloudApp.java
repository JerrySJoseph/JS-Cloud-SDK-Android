package com.example.jscloud_core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.example.jscloud_core.interfaces.CloudServerConnectionCallback;
import com.example.jscloud_core.interfaces.InvokeResponse;
import com.example.jscloud_core.models.Device;
import com.example.jscloud_core.models.DeviceFactory;

import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class JSCloudApp {

    private static JSCloudApp mInstance;
    private static Context mContext;
    private static String TAG="JS-Cloud-App";
    private static Socket mSocket;
    private static String SHA_fingerprint=null;
    private static CloudServerConnectionCallback cloudServerConnectionCallback;

    //Client factory
    static private DeviceFactory mDeviceFactory = new DeviceFactory() {
        @Override
        public Device newClient() {
            return Device.CreateClientForThisDevice(mContext,SHA_fingerprint);
        }
    };

    public static void setCloudServerConnectionCallback(CloudServerConnectionCallback cloudServerConnectionCallback) {
        JSCloudApp.cloudServerConnectionCallback = cloudServerConnectionCallback;
    }

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
            getAppFingerPrint();
            //Connect to Server
            mSocket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
    }

    public static void invoke(String eventName, String data, InvokeResponse ack)
    {
        mSocket.emit(eventName, data, new Ack() {
            @Override
            public void call(Object... args) {
                post(()->{
                    if(ack!=null)
                        ack.onAck(args);
                });
            }
        });
    }
    private static void getAppFingerPrint()
    {
       // HITKuHdiF+OJL416iFFK/P2pcVE= HITKuHdiF+OJL416iFFK/P2pcVE=
        Log.e("KeyHash",mContext.getPackageName());
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(signature.toByteArray());
                SHA_fingerprint=Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("KeyHash", SHA_fingerprint);
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e("KeyHash Error",e.getMessage());
        }
        catch (NoSuchAlgorithmException e) {
            Log.e("KeyHash Error",e.getMessage());
        }
    }
    //Fired on successful connection
    static Emitter.Listener onConnectListener= args -> {
        //TODO: Encrypt user Data

        mSocket.emit("client-handshake", mDeviceFactory.newClient().toJSON(), new Ack() {
            @Override
            public void call(Object... args) {
                String message="";
                try{
                    boolean success=(Boolean)args[0];
                    message=(String)args[1];
                    if(!success)
                    {
                        getInstance().disconnect();
                        String finalMessage = message;
                        post(()->{
                            if(cloudServerConnectionCallback!=null)
                                cloudServerConnectionCallback.onConnectionFailed(finalMessage);
                        });

                    }
                    else
                        post(()->{
                            if(cloudServerConnectionCallback!=null)
                                cloudServerConnectionCallback.onConnected();
                        });
                }catch (Exception e){
                    message=e.getMessage();

                }finally {
                    Log.e(TAG,message);
                }

            }
        });
        Log.e(TAG,"Connected to server. Your socket id is "+ mSocket.id());
    };

    //Fired when disconnected from server
    static Emitter.Listener onDisconnectListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG,"Disconnected from Server.");
            post(()->{
                if(cloudServerConnectionCallback!=null)
                    cloudServerConnectionCallback.onDisconnected();
            });

        }
    };

    static void post(Runnable runnable)
    {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    //Fired when error occurs while connecting
    static Emitter.Listener onConnectErrorListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG,"Connection Error ");
            post(()->{
                if(cloudServerConnectionCallback!=null)
                    cloudServerConnectionCallback.onConnectionFailed("unknown error occured");
            });

        }
    };

    public Device getDevice()
    {
        return mDeviceFactory.newClient();
    }

    public void disconnect()
    {
        mSocket.off();
        mSocket.disconnect();
    }

    public void connect()
    {
        if(mSocket==null || mInstance==null || mContext==null)
            throw new IllegalStateException("JS cloud not initialised. Please Make sure you have Intialized JSCloudApp before calling this function");
        if(!mSocket.connected())
            mSocket.connect();
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
