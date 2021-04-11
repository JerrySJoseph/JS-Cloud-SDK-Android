package com.example.js_auth.Helpers;

import com.example.js_auth.Models.AuthMode;
import com.example.js_auth.Models.AuthType;
import com.example.js_auth.Models.JSCloudUser;
import com.google.gson.Gson;

public class AuthRequest {
    AuthType authType;
    AuthMode authMode;
    String idToken;
    long iat;
    JSCloudUser user;

    public JSCloudUser getUser() {
        return user;
    }

    public void setUser(JSCloudUser user) {
        this.user = user;
    }

    public AuthRequest(AuthType authType, AuthMode authMode, String idToken) {
        this.authType = authType;
        this.authMode = authMode;
        this.idToken = idToken;
    }
    public AuthRequest(AuthType authType, AuthMode authMode) {
        this.authType = authType;
        this.authMode = authMode;
    }
    public AuthMode getAuthMode() {
        return authMode;
    }

    public void setAuthMode(AuthMode authMode) {
        this.authMode = authMode;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public long getIat() {
        return iat;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }
    public String toJSON()
    {
        return new Gson().toJson(this);
    }
    public static AuthRequest fromJSON(String jsonString)
    {
        return new Gson().fromJson(jsonString,AuthRequest.class);
    }
}
