package com.example.jscloud_core.models;

import android.content.Context;
import android.provider.Settings;

import com.google.gson.GsonBuilder;

public class Device {
    //If implementing authentication, auth-token might be useful
    String SHA_fingerprint;
    String packageName;
    //unique device ID
    String clientID;

    public Device(){
    }

    public Device(String SHA_fingerprint, String clientID,String packageName) {
        this.SHA_fingerprint = SHA_fingerprint;
        this.clientID = clientID;
        this.packageName= packageName;
    }

    public String getSHA_fingerprint() {
        return SHA_fingerprint;
    }


    public String getClientID() {
        return clientID;
    }

    public static Device CreateClientForThisDevice(Context context, String SHA_fingerprint)
    {
        String uniqueDeviceID= Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return new Device(SHA_fingerprint,uniqueDeviceID,context.getPackageName());
    }

    public String toJSON()
    {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(this).replaceAll("\\\\n", "");
    }


}
