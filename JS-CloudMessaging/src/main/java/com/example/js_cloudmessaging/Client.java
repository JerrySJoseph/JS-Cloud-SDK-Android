package com.example.js_cloudmessaging;

import android.content.Context;
import android.provider.Settings;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class Client {
    //If implementing authentication, auth-token might be useful
    String authToken;

    //unique device ID
    String clientID;

    public Client(){
    }

    public Client(String authToken, String clientID) {
        this.authToken = authToken;
        this.clientID = clientID;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public static Client CreateClientForThisDevice(Context context, String authToken)
    {
        String uniqueDeviceID= Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return new Client(authToken,uniqueDeviceID);
    }

    public String toJSONString()
    {
        return new Gson().toJson(this);
    }

    public JSONObject toJSONObject()
    {
        JSONObject jsonObject = null;

        try {
            jsonObject=new JSONObject();
            jsonObject.put("authToken",authToken);
            jsonObject.put("clientID",clientID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;

    }

}
