package com.example.jscloudapi;

import androidx.annotation.RequiresApi;
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
import com.example.jscloud_core.JSCloudApp;


public class MainActivity extends JSCloudAuthActivity {


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startService(View view) {
       //
      JSCloudAuth.getInstance().signInWithEmail(this,"astala@123.com","astala");

    }

    public void stopService(View view) {
        JSCloudApp.disconnect();
    }


    public void sendCommand(View view) {
       MyUser user= new MyUser();
       user.setEmail("astala@123.com");
       user.setPassword("astala");
       user.setAuthType(AuthType.Email);
       user.setName("Astala Khan");
       user.setAccessToken("Some wonderful access Token");
       user.setRefreshToken("Some wonderful refresh Token");
       JSCloudAuth.getInstance().createUserWithEmail(this,user);
    }

    @Override
    protected void onAuthResponse(String message, JSCloudUser user) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
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
}