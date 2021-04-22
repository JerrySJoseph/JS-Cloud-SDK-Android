package com.example.js_dynamicdatabase.models;

import com.google.gson.Gson;

public class TaskResult {
    boolean isSuccess;
    String message;
    Object result;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public Object getResult() {
        return result;
    }
    public <T>T getResult(Class<T> className) {
        return new Gson().fromJson((String)result,className);
    }
    public static TaskResult parseFrom(Object... args)
    {
        TaskResult response=new TaskResult();
        response.isSuccess=(Boolean)args[0];
        response.message=(String)args[1];
        response.result=args[2];
        return response;
    }
}
