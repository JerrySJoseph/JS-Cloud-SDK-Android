package com.example.jscloudapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.example.js_auth.JSCloudAuth;
import com.example.js_auth.JSCloudAuthActivity;
import com.example.js_auth.Models.AuthType;
import com.example.js_auth.Models.JSCloudUser;
import com.example.js_auth.interfaces.SignOutResponse;
import com.example.js_cloudmessaging.JSCloudMessaging;
import com.example.jscloud_core.JSCloudApp;
import com.example.jscloud_core.interfaces.InvokeResponse;

import java.util.Arrays;


public class MainActivity extends JSCloudAuthActivity  {


    String TAG="Auth Activity";
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JSCloudAuth.getInstance().setTokenRefreshInterval(1000*5);
        JSCloudMessaging.init(this);
        JSCloudMessaging.getInstance().enable(CloudMess.class);
    }

    public void startService(View view) {
      JSCloudAuth.getInstance().signInWithEmail(this,"astala@somedomain.com","astala");
    }


    public void stopService(View view) {
        JSCloudApp.getInstance().disconnect();
    }


    public void sendCommand(View view) {
    try{
        MyUser user= new MyUser();
        user.setEmail("astala@somedomain.com");
        user.setPassword("astala");
        user.setName("Astala Khan");
        user.setAccessToken("Some wonderful access Token");
        user.setRefreshToken("Some wonderful refresh Token");
        JSCloudAuth.getInstance().createUser(this,user);
    }catch (Exception e)
    {
        Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
    }


    }

    @Override
    protected void onAuthResponse(String message, JSCloudUser user) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void currentUser(View view) {
        JSCloudUser user=JSCloudAuth.getInstance().getCurrentUser();
        String msg=JSCloudAuth.getInstance().getCurrentUser()==null
                ?"no user logged in"
                :MyUser.fromJSON(JSCloudAuth.getInstance().getCurrentUserRaw()).getName();

        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();

    }

    public void signInGoogle(View view) {
        JSCloudAuth.getInstance().signInWithGoogle(this);
    }

    public void refreshToken(View view) {
        JSCloudAuth.getInstance().refreshMyToken();
    }

    public void signOut(View view) {
        JSCloudAuth.getInstance().signOut();
    }

    public void updateUser(View view) {
        MyUser user= new MyUser();
        user.setAuthType(AuthType.Google);
        user.setName("User_"+System.currentTimeMillis());
        user.setSomeToken(45612);
        user.setDesignation("Chacha jaan");
        user.setPhone("+9178456213643");
        user.setAccessToken("Some wonderful access Token");
        user.setRefreshToken("Some wonderful refresh Token");
        JSCloudAuth.getInstance().updateCurrentUser(user);

    }

    public void sync(View view) {
        JSCloudAuth.getInstance().sync();
    }

    public void invoke(View view) {
        JSCloudApp.invoke("some", JSCloudAuth.getInstance().getCurrentUser().get_id(), new InvokeResponse() {
            @Override
            public void onAck(Object... args) {
                Log.e(TAG,(String)args[0]);
            }
        });
    }

    public void deleteCurrentuser(View view) {
        JSCloudAuth.getInstance().deleteCurrentUser();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Facebook handling Response

    }


    public void makeGraphRequest()
    {

    }

}