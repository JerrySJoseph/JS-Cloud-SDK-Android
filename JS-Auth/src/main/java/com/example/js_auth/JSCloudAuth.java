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
import com.example.js_auth.Helpers.JSCloudUserStore;
import com.example.js_auth.Models.AuthMode;
import com.example.js_auth.Models.AuthType;
import com.example.js_auth.Models.JSCloudUser;
import com.example.js_auth.interfaces.AuthResponse;
import com.example.js_auth.interfaces.SignOutResponse;
import com.example.jscloud_core.JSCloudApp;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class JSCloudAuth {

    private static JSCloudAuth mInstance;
    private static Context mContext;
    private static String TAG="JS-Cloud-Auth";
    private static Socket mSocket;
    private JSCloudUser mUser=null;

    private static int RC_SIGN_IN=1011;

    GoogleSignInOptions gso =null;
    GoogleSignInClient mGoogleSignInClient;

    public static synchronized JSCloudAuth getInstance(){
        if(mContext==null)
            mContext= JSCloudApp.getInstance().getContext();
        if(mSocket==null)
            mSocket=JSCloudApp.getInstance().getSocket();
        if(mInstance==null)
            mInstance=new JSCloudAuth();

        registerAuthEvents();
        return mInstance;
    }

    private static void registerAuthEvents() {
        mSocket.on("sign-out",signOutListener);
    }

    static Emitter.Listener signOutListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getInstance().handleSignOut();
        }
    };

    public void signInWithEmail(JSCloudAuthActivity authActivity,String email, String password){
        AuthRequest request= new AuthRequest(AuthType.Email,AuthMode.SIGN_IN);
        JSCloudUser user= new JSCloudUser();
        user.setEmail(email);
        user.setPassword(password);
        request.setUser(user);
        initiateAuthFlow(authActivity,request);
    }
    public void createUserWithEmail(JSCloudAuthActivity authActivity,JSCloudUser user){
        AuthRequest request= new AuthRequest(AuthType.Email,AuthMode.CREATE);
        request.setUser(user);
       initiateAuthFlow(authActivity,request);
    }

    public void signInWithFacebook(String email,String password, AuthResponse responseCallback){}
    public void signInWithPhone(String email,String password, AuthResponse responseCallback){}
    public void signInAsGuest(AuthResponse responseCallback){}

    public JSCloudUser getCurrentUser(){
        mUser=JSCloudUser.fromJSON(JSCloudUserStore.getSavedUser(mContext));
        return mUser;
    }

    public String getCurrentUserRaw(){
        return JSCloudUserStore.getSavedUser(mContext);
    }

    public void updateCurrentUser(JSCloudUser user) {
        String accessToken=JSCloudUserStore.getAccessToken(mContext);
        AuthRequest request= new AuthRequest(AuthType.Google,AuthMode.UPDATE,accessToken);
        request.setUser(user);
        mSocket.emit("js-cloud-user-update", request.toJSON(), new Ack() {
            @Override
            public void call(Object... args) {
                try{
                    String message=(String)args[0];
                    String response=(String)args[1];

                    //Caching User data
                    mUser=JSCloudUser.fromJSON(response);
                    JSCloudUserStore.saveUser(mContext,response);
                    Log.e(TAG,message);
                }catch (Exception e)
                {
                    Log.e(TAG,e.getMessage());
                }

            }
        });
    }

    //Google Sign In flow
    public void signInWithGoogle(JSCloudAuthActivity activity){
        GoogleSignInAccount account=GoogleSignIn.getLastSignedInAccount(activity);
        if(account!=null && !account.isExpired())
        {
            handleGoogleSignInResult(activity,account);
        }
        else
        {
            gso =getGso();

            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        }


    }

    public void handleGoogleSignActivityResult(JSCloudAuthActivity authActivity,int requestCode,int resultCode,Intent data) {
        try {
            if (requestCode == RC_SIGN_IN && resultCode==Activity.RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleGoogleSignInResult(authActivity,task.getResult(ApiException.class));
            }
        }catch (Exception e)
        {
            Log.w(TAG, "handleSignInResult:error", e);
            Toast.makeText(mContext,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    private void handleGoogleSignInResult(JSCloudAuthActivity authActivity,@NonNull GoogleSignInAccount account) {

            String idToken = account.getIdToken();
            Log.e(TAG,idToken);
            AuthRequest request=new AuthRequest(AuthType.Google, AuthMode.CREATE_OR_SIGNIN,idToken);
           initiateAuthFlow(authActivity,request);
    }

    private GoogleSignInOptions getGso()
    {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("629362459295-krpl7a5s8cgt5b96s25jabtiotlvpkq1.apps.googleusercontent.com")
                .requestEmail()
                .build();
    }

    public void refreshMyToken()
    {
        String refreshToken=JSCloudUserStore.getRefreshToken(mContext);
        mSocket.emit("refresh-token", refreshToken, new Ack() {
            @Override
            public void call(Object... args) {

                try {
                    boolean status=(Boolean) args[0];
                    String message=(String)args[1];
                    String accessToken=(String)args[2];
                    String refreshToken=(String)args[3];
                    if(status && accessToken!=null)
                        JSCloudUserStore.saveAccessToken(mContext,accessToken);
                    if(status && refreshToken!=null)
                        JSCloudUserStore.saveRefreshToken(mContext,refreshToken);
                    Log.e(TAG,"Ack received : "+message);
                    Log.e(TAG,"Access-Token : "+accessToken);
                    Log.e(TAG,"Refresh-Token : "+refreshToken);
                }catch (Exception e)
                {
                    Log.e(TAG,"Error: "+e.getMessage());
                }


            }
        });
    }


    public void signOut(SignOutResponse signOutResponse)
    {
        if(getCurrentUser()==null)
            return;

        String _id=JSCloudAuth.getInstance().getCurrentUser().get_id();
       
        mSocket.emit("sign-out", _id, new Ack() {
            @Override
            public void call(Object... args) {
                //if ack is success
                try{
                    String message=(String)args[0];
                    Log.e(TAG,"Ack received : "+message);
                    Boolean success=(Boolean)args[1];
                    if(success)
                    {
                        handleSignOut();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if(signOutResponse!=null)
                                    signOutResponse.onSignOutSuccess();
                            }
                        });
                    }
                    else
                    {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if(signOutResponse!=null)
                                    signOutResponse.onSignOutFailed(message);
                            }
                        });
                    }

                }catch (Exception e)
                {
                   Log.e(TAG,e.getMessage());
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(signOutResponse!=null)
                                signOutResponse.onSignOutFailed(e.getMessage());
                        }
                    });
                }

            }
        });
    }

    public void signOut()
    {
        signOut(null);
    }

    private void handleSignOut()
    {
        //handle Google Signout if User is logged in via Google
        if(getCurrentUser()==null)
            return;
        if(getCurrentUser().getAuthType()==AuthType.Google) {

            gso = getGso();

            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);

            mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                        JSCloudUserStore.clearCache(mContext);
                }
            });
        }
        else if(getCurrentUser().getAuthType()==AuthType.Email){
            JSCloudUserStore.clearCache(mContext);
        }


    }

    private void initiateAuthFlow(JSCloudAuthActivity authActivity,AuthRequest request)
    {
        mSocket.emit("auth-flow", request.toJSON(), new Ack() {
            @Override
            public void call(Object... args) {
                String message,response,accToken,refToken;
                message=(String)args[0];
                Log.e(TAG,"Acknowledgement Received : "+message);
                mUser=null;

                try{
                    response=(String)args[1];
                    message=(String)args[0];
                    accToken=(String)args[2];
                    refToken=(String)args[3];

                    //Caching User data
                    mUser=JSCloudUser.fromJSON(response);
                    JSCloudUserStore.saveUser(authActivity,response);
                    JSCloudUserStore.saveAccessToken(authActivity,accToken);
                    JSCloudUserStore.saveRefreshToken(authActivity,refToken);
                }catch (Exception e) {
                    message=e.getMessage();
                }finally {
                    String finalMessage = message;
                    new Handler(Looper.getMainLooper()).post(() -> authActivity.onAuthResponse(finalMessage,mUser));
                }
            }
        });
    }

}
