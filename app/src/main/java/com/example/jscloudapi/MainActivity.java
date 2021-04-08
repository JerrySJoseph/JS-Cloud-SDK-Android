package com.example.jscloudapi;

import androidx.annotation.RequiresApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.example.js_auth.JSCloudAuth;
import com.example.js_auth.JSCloudAuthActivity;
import com.example.js_auth.Models.JSCloudUser;
import com.example.jscloud_core.JSCloudApp;


public class MainActivity extends JSCloudAuthActivity {


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startService(View view) {

       JSCloudAuth.getInstance().signInWithGoogle(this);
      // JSCloudAuth.getInstance().updateCurrentUser(this,new MyUser());
    }

    public void stopService(View view) {
        JSCloudApp.disconnect();
    }


    public void sendCommand(View view) {
        MyUser user=  new MyUser();
        user.setAccessToken("123");
        user.set_id(JSCloudAuth.getInstance().getCurrentUser().get_id());
        user.setRefreshToken("!@#");
        user.setYes(123);
        user.setName("Asim Khan");
        user.setEmail("jerin.123@gmail.com");
        JSCloudAuth.getInstance().updateCurrentUser(this,user);
    }

    @Override
    protected void onAuthResponse(boolean isAuthorized, JSCloudUser user) {
        Toast.makeText(this,user.getName(),Toast.LENGTH_SHORT).show();
    }

    public void currentUser(View view) {
        MyUser user=MyUser.fromJSON(JSCloudAuth.getInstance().getCurrentUserRaw());
        Toast.makeText(this,user.getName(),Toast.LENGTH_SHORT).show();
        Log.e("TAG",JSCloudAuth.getInstance().getCurrentUserRaw());
    }
}