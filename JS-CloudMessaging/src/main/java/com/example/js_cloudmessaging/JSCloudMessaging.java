package com.example.js_cloudmessaging;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.core.content.ContextCompat;

public class JSCloudMessaging {

    static Context mContext;
    static JSCloudMessaging mInstance;
    private Class<?> mServiceClass;

    public static synchronized JSCloudMessaging getInstance()
    {
        if(mContext==null)
            throw new IllegalStateException("JS Cloud messaging not initialized");
        if(mInstance==null)
            mInstance=new JSCloudMessaging();
        return mInstance;
    }

    public static void init(Context context){
        mContext=context;
    }
    public void enable(Class<?> serviceClass)
    {
        mServiceClass=serviceClass;
        startService();
    }
    public void disable()
    {
        stopService();
    }

    public void subscribeTo(String identifier)
    {

    }

    private void startService() {
        Intent serviceIntent = new Intent(mContext, mServiceClass);
        ContextCompat.startForegroundService(mContext, serviceIntent);

    }
    private void stopService() {
        Intent serviceIntent = new Intent(mContext, mServiceClass);
        mContext.stopService(serviceIntent);
    }
}
