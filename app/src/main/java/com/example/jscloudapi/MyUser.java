package com.example.jscloudapi;

import com.example.js_auth.Models.JSCloudUser;
import com.google.gson.Gson;

public class MyUser extends JSCloudUser {
    String AccessToken;
    String refreshToken;
    int SomeToken;
    double Yes;

    public String getAccessToken() {
        return AccessToken;
    }

    public void setAccessToken(String accessToken) {
        AccessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public int getSomeToken() {
        return SomeToken;
    }

    public void setSomeToken(int someToken) {
        SomeToken = someToken;
    }

    public double getYes() {
        return Yes;
    }

    public void setYes(double yes) {
        Yes = yes;
    }
    public String toJSON()
    {
        return new Gson().toJson(this);
    }
    public static MyUser fromJSON(String jsonString)
    {
        return new Gson().fromJson(jsonString,MyUser.class);
    }
}
