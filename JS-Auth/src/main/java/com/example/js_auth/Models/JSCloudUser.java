package com.example.js_auth.Models;

import com.google.gson.Gson;

public class JSCloudUser {

    String _id,name,email,phone,designation,password,photoUrl;
    boolean isVerified;
    AuthType authType;

    public String getUid() {
        return _id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    public String toJSON()
    {
        return new Gson().toJson(this);
    }
    public static JSCloudUser fromJSON(String jsonString)
    {
        return new Gson().fromJson(jsonString,JSCloudUser.class);
    }
}
