package com.example.js_dynamicdatabase.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class EventStore {
    public static void addEvent(Context context, String event)
    {
        Set<String> newSet=getEvents(context);
        newSet.add(event);
        SharedPreferences sharedPreferences=context.getSharedPreferences("js_cloud_dynamic_db_event_store",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putStringSet("registered_events",newSet);
        editor.commit();

    }
    public static void removeEvent(Context context, String event)
    {
        Set<String> newSet=getEvents(context);
        newSet.remove(event);
        SharedPreferences sharedPreferences=context.getSharedPreferences("js_cloud_dynamic_db_event_store",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putStringSet("registered_events",newSet);
        editor.commit();

    }
    public static Set<String> getEvents(Context context)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences("js_cloud_dynamic_db_event_store",Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet("registered_events",null);
    }
}
