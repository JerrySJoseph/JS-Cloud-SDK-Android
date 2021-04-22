package com.example.js_dynamicdatabase;

import android.content.Context;

import com.example.js_dynamicdatabase.models.Collection;
import com.example.jscloud_core.JSCloudApp;

import io.socket.client.Socket;

public class JSCloudDynamicDB {

    private static JSCloudDynamicDB mInstance;
    private static Context mContext;
    private static Socket mSocket;

    public static synchronized JSCloudDynamicDB getInstance(){
        if(mContext==null)
            mContext= JSCloudApp.getInstance().getContext();
        if(mSocket==null)
            mSocket=JSCloudApp.getInstance().getSocket();
        if(mInstance==null)
            mInstance=new JSCloudDynamicDB();

        return mInstance;
    }
    JSCloudDynamicDB()
    {

    }
    public Collection collection(String name)
    {
        return new Collection(name);
    }


}
