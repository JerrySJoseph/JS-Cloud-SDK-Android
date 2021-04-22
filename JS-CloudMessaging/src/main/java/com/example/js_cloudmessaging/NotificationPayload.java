package com.example.js_cloudmessaging;

import com.google.gson.Gson;

public class NotificationPayload {
    String title,message;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public static NotificationPayload fromJSON(String jsonString){
        return new Gson().fromJson(jsonString,NotificationPayload.class);
    }
    public String toJSON()
    {
        return new Gson().toJson(this);
    }
}
