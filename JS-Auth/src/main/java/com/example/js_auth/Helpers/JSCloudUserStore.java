package com.example.js_auth.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.js_auth.Models.JSCloudUser;

public class JSCloudUserStore {
    public static void saveUser(Context context,String userJsonString)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences("js_cloud_user_save_data",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putString("cached_user_data",userJsonString);
        editor.commit();

    }
    public static String getSavedUser(Context context)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences("js_cloud_user_save_data",Context.MODE_PRIVATE);
        return sharedPreferences.getString("cached_user_data",null);
    }

}
