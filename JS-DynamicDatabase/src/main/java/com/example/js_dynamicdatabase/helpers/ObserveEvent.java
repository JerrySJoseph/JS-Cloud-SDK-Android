package com.example.js_dynamicdatabase.helpers;

import android.util.Log;

import com.google.gson.Gson;

public class ObserveEvent {
    public enum EventType{
        insert,
        delete,
        update
    }
    EventType observeType;
    String collectionName;
    Object result;

    public ObserveEvent(EventType observeType, String collectionName) {
        this.observeType = observeType;
        this.collectionName = collectionName;
    }

    public Object getResult() {
        return result;
    }

    public <T>T getResult(Class<T> className) {
        return new Gson().fromJson((String)result,className);
    }

    public EventType getObserveType() {
        return observeType;
    }

    public void setObserveType(EventType observeType) {
        this.observeType = observeType;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public static ObserveEvent parseFromArgs(Object... args)
    {
        ObserveEvent event=null;
        if((Boolean)args[0])
        {
            Log.e("Observe event",(String)args[1]);
            event=new Gson().fromJson((String)args[2],ObserveEvent.class);
        }
        return event;
    }
}
