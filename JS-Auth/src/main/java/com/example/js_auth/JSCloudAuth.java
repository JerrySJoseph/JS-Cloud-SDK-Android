package com.example.js_auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.js_auth.Helpers.AuthRequest;
import com.example.js_auth.Models.AuthMode;
import com.example.js_auth.Models.AuthType;
import com.example.js_auth.Models.JSCloudUser;
import com.example.js_auth.interfaces.AuthResponse;
import com.example.jscloud_core.JSCloudApp;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.io.InputStream;

import io.socket.client.Ack;
import io.socket.client.Socket;


public class JSCloudAuth {

    private static JSCloudAuth mInstance;
    private static Context mContext;
    private static String TAG="JS-Cloud-Auth";
    private static Socket mSocket;
    private JSCloudUser mUser=null;

    private static int RC_SIGN_IN=1011;

    GoogleSignInOptions gso =null;
    GoogleSignInClient mGoogleSignInClient;

    public static synchronized JSCloudAuth getInstance()
    {
        if(mContext==null)
            mContext= JSCloudApp.getInstance().getContext();
        if(mSocket==null)
            mSocket=JSCloudApp.getInstance().getSocket();
        if(mInstance==null)
            mInstance=new JSCloudAuth();
        return mInstance;
    }

    public void signInWithEmail(String email, String password, AuthResponse responseCallback){}

    public void signInWithFacebook(String email,String password, AuthResponse responseCallback){}
    public void signInWithPhone(String email,String password, AuthResponse responseCallback){}
    public void signInAsGuest(AuthResponse responseCallback){}

    public JSCloudUser getCurrentUser()
    {
        return mUser;
    }

    //Google Sign In routines
    public void signInWithGoogle(JSCloudAuthActivity activity){

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("629362459295-krpl7a5s8cgt5b96s25jabtiotlvpkq1.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    public void handleGoogleSignActivityResult(JSCloudAuthActivity authActivity,int requestCode,int resultCode,Intent data)
    {
        if (requestCode == RC_SIGN_IN && resultCode==Activity.RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(authActivity,task);
        }
    }
    private void handleSignInResult(JSCloudAuthActivity authActivity,@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            Log.e(TAG,"idToken");
            AuthRequest request=new AuthRequest(AuthType.Google, AuthMode.CREATE_OR_SIGNIN,idToken);
            mSocket.emit("auth-flow", request.toJSON(), new Ack() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG,"Acknowledgement Received");
                    JSCloudUser user=JSCloudUser.fromJSON((String)args[0]);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            authActivity.onAuthResponse(user!=null,user);
                        }
                    });
                }
            });
        } catch (ApiException e) {
            Log.w(TAG, "handleSignInResult:error", e);
            Toast.makeText(mContext,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

}
