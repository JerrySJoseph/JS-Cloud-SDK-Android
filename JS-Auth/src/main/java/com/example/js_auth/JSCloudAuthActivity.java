package com.example.js_auth;

import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.js_auth.Models.JSCloudUser;

public abstract class JSCloudAuthActivity extends AppCompatActivity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JSCloudAuth.getInstance().handleGoogleSignActivityResult(this,requestCode,resultCode,data);
    }
    protected abstract void onAuthResponse(String responseMessage, JSCloudUser user);


}
