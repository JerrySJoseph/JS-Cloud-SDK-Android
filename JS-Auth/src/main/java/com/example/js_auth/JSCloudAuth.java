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
import com.example.js_auth.Helpers.TokenUpdater;
import com.example.js_auth.Models.AuthMode;
import com.example.js_auth.Models.AuthType;
import com.example.js_auth.Models.JSCloudUser;
import com.example.js_auth.interfaces.AuthResponse;
import com.example.js_auth.interfaces.RevokedAccessListener;
import com.example.js_auth.interfaces.SignOutResponse;
import com.example.js_auth.interfaces.UpdateResponse;
import com.example.jscloud_core.JSCloudApp;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class JSCloudAuth {

    private static JSCloudAuth mInstance;
    private static Context mContext;
    private static String TAG="JS-Cloud-Auth";
    private static Socket mSocket;
    private JSCloudUser mUser=null;
    private RevokedAccessListener revokedAccessListener;
    private static boolean tokenUpdatebBegun=false;
    private static TokenUpdater tokenUpdater;
    private static String mGoogleClientID=null;
    private static int RC_SIGN_IN=1011;
    private static long defaultTokenUpdateInterval=1000*60*15; //Once in 15 Minutes
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    GoogleSignInOptions gso =null;
    GoogleSignInClient mGoogleSignInClient;

    private static  final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        public Thread newThread(Runnable r) {
            Log.e("THREAD","created new thread");
            return new Thread(r, "token-update-task #" + mCount.getAndIncrement());
        }
    };

    static ScheduledThreadPoolExecutor  threadPoolExecutor;

    public static void setGoogleClientID(String googleClientID) {
        mGoogleClientID = googleClientID;
    }

    public static synchronized JSCloudAuth getInstance(){
        if(mContext==null)
            mContext= JSCloudApp.getInstance().getContext();
        if(mSocket==null)
            mSocket=JSCloudApp.getInstance().getSocket();
        if(mInstance==null)
            mInstance=new JSCloudAuth();
        if(tokenUpdater==null)
            tokenUpdater=new TokenUpdater(defaultTokenUpdateInterval);


        return mInstance;
    }
    JSCloudAuth()
    {
        registerAuthEvents();
        sendAuthHandshake();

    }
    private void sendAuthHandshake() {
        if(getCurrentUser()==null)
            return;

       AuthRequest request= new AuthRequest();
       request.setUser(getCurrentUser());
        mSocket.emit("auth-handshake",request.toJSON(), new Ack() {
            @Override
            public void call(Object... args) {

                Log.e(TAG,(String)args[1]);
            }
        });
    }

    private static void registerAuthEvents() {
        mSocket.on("revoke-access",signOutListener);
        if(!tokenUpdatebBegun)
           startTokenUpdate();

    }

    public void setTokenRefreshInterval(long intervalms) {
        stopTokenUpdate();
        defaultTokenUpdateInterval = intervalms;
        startTokenUpdate();
    }
    private boolean validateUser(JSCloudUser user)
    {
        if(user.getName()==null || user.getName().isEmpty())
            throw new IllegalArgumentException("User name should not be null or empty");
        if(user.getEmail()==null || user.getEmail().isEmpty())
            throw new IllegalArgumentException("User email should not be null or empty");
        if(user.getPassword()==null || user.getPassword().isEmpty())
            throw new IllegalArgumentException("User password should not be null or empty");
        if(!user.getEmail().matches(emailPattern))
            throw new IllegalArgumentException("User email is not valid.");
        return true;
    }

    private static void startTokenUpdate() {
        String savedUser=JSCloudUserStore.getSavedUser(mContext);
        if(savedUser==null || savedUser.isEmpty() || tokenUpdatebBegun)
            return;
        threadPoolExecutor=(ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1,sThreadFactory);
        threadPoolExecutor.scheduleWithFixedDelay(updateTask,100,defaultTokenUpdateInterval, TimeUnit.MILLISECONDS);
        tokenUpdatebBegun=true;
    }

    private static void stopTokenUpdate() {
        if(!tokenUpdatebBegun)
            return;
        tokenUpdatebBegun=false;
        threadPoolExecutor.shutdown();
    }

    static Runnable updateTask= new Runnable() {
        @Override
        public void run() {
            JSCloudAuth.getInstance().refreshMyToken();
        }
    };

    static Emitter.Listener signOutListener= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG,"xxxxxxxxxxxxxxxxxxxxx Access Revoked by SERVER xxxxxxxxxxxxxxxxx");
            try{
                JSCloudUserStore.clearCache(mContext);
                stopTokenUpdate();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(getInstance().revokedAccessListener!=null)
                            getInstance().revokedAccessListener.onAccessRevoked((String)args[0]);
                    }
                });

            }catch (Exception e)
            {
                Log.e(TAG,"Error: "+e.getMessage());
            }

        }
    };

    public void addOnRevokeAccessListener(RevokedAccessListener revokedAccessListener){

        this.revokedAccessListener = revokedAccessListener;
    }
    public void invoke() {
        if(getCurrentUser()==null)
            return;
        mSocket.emit("invoke", getCurrentUser().get_id(), new Ack() {
            @Override
            public void call(Object... args) {

            }
        });
    }
    public void signInWithEmail(JSCloudAuthActivity authActivity,String email, String password){
        AuthRequest request= new AuthRequest(AuthType.Email,AuthMode.SIGN_IN);
        JSCloudUser user= new JSCloudUser();
        user.setEmail(email);
        user.setPassword(password);
        request.setUser(user);
        initiateAuthFlow(authActivity,request);
    }
    public void createUser(JSCloudAuthActivity authActivity,JSCloudUser user){
        if(!validateUser(user))
            return;
        if(user.getAuthType()==null)
            user.setAuthType(AuthType.Email);
        AuthRequest request= new AuthRequest(AuthType.Email,AuthMode.CREATE);
        request.setUser(user);
       initiateAuthFlow(authActivity,request);
    }

    public void signInWithFacebook(String email,String password, AuthResponse responseCallback){}
    public void signInWithPhone(String email,String password, AuthResponse responseCallback){}
    public void signInAsGuest(AuthResponse responseCallback){}

    public JSCloudUser getCurrentUser(){
        mUser=JSCloudUser.fromJSON(getCurrentUserRaw());
        return mUser;
    }
    public <T> T getCurrentUser(Class<T> className)
    {
        return new Gson().fromJson(getCurrentUserRaw(),className);
    }

    public String getCurrentUserRaw(){
        return JSCloudUserStore.getSavedUser(mContext);
    }

    public void updateCurrentUser(JSCloudUser user, UpdateResponse updateResponse) {
        String accessToken=JSCloudUserStore.getAccessToken(mContext);
        AuthRequest request= new AuthRequest(AuthType.Google,AuthMode.UPDATE,accessToken);
        request.setUser(user);
        mSocket.emit("js-cloud-user-update", request.toJSON(), new Ack() {
            @Override
            public void call(Object... args) {
                try{
                    boolean success=(Boolean)args[0];
                    String message=(String)args[1];
                    String response=(String)args[2];

                    //Caching User data
                    if(success)
                    {
                        mUser=JSCloudUser.fromJSON(response);
                        JSCloudUserStore.saveUser(mContext,response);
                        post(()->{
                            updateResponse.onUpdateResponse(true,message);
                        });
                    }
                    else
                    {
                        post(()->{
                            updateResponse.onUpdateResponse(false,message);
                        });
                    }

                    Log.e(TAG,message);
                }catch (Exception e)
                {
                    Log.e(TAG,e.getMessage());
                    post(()->{
                        updateResponse.onUpdateResponse(false,e.getMessage());
                    });
                }

            }
        });
    }
    private void post(Runnable r)
    {
        new Handler(Looper.getMainLooper()).post(r);
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
          //  Log.e(TAG,idToken);
            AuthRequest request=new AuthRequest(AuthType.Google, AuthMode.CREATE_OR_SIGNIN,idToken);
           initiateAuthFlow(authActivity,request);
    }

    private GoogleSignInOptions getGso() {
        if(mGoogleClientID==null || mGoogleClientID.isEmpty())
            throw new IllegalArgumentException("Invalid Google Client ID provided. Please set Google Client ID in Application class where you initiate JSCloudApp");
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(mGoogleClientID)
                .requestEmail()
                .build();
    }

    public void deleteCurrentUser()
    {
        AuthRequest authRequest= new AuthRequest();
        authRequest.setUser(getCurrentUser());
        authRequest.setIdToken(JSCloudUserStore.getAccessToken(mContext));
        authRequest.setAuthMode(AuthMode.DELETE);
        mSocket.emit("delete-user", authRequest.toJSON(), new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    boolean success=(Boolean) args[0];
                    String message=(String)args[1];
                    if(success)
                    {
                        JSCloudUserStore.clearCache(mContext);
                        post(()->{
                            if(revokedAccessListener!=null)
                                revokedAccessListener.onAccessRevoked(message);
                        });

                    }
                    Log.e(TAG,message);
                }catch (Exception e)
                {
                    Log.e(TAG,"Error: "+e.getMessage());
                }
            }
        });
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
                   if(status)
                       Log.e(TAG,"Tokens Synced at "+System.currentTimeMillis());
                }catch (Exception e)
                {
                    Log.e(TAG,"Token Sync Error: "+e.getMessage());
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
                        stopTokenUpdate();
                        post(()->{
                            if(signOutResponse!=null)
                                signOutResponse.onSignOutSuccess();
                        });

                    }
                    else
                    {
                        post(()->{
                            if(signOutResponse!=null)
                                signOutResponse.onSignOutFailed(message);
                        });

                    }

                }catch (Exception e)
                {
                   Log.e(TAG,e.getMessage());
                   post(()->{
                       if(signOutResponse!=null)
                           signOutResponse.onSignOutFailed(e.getMessage());
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
        else {
            JSCloudUserStore.clearCache(mContext);
        }


    }

    private void initiateAuthFlow(JSCloudAuthActivity authActivity,AuthRequest request)
    {
        mSocket.emit("auth-flow",request.toJSON() , new Ack() {
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
                    startTokenUpdate();

                }catch (Exception e) {
                    message=e.getMessage();
                }finally {
                    String finalMessage = message;
                    post(()->{
                        authActivity.onAuthResponse(finalMessage,mUser);
                    });

                }
            }
        });
    }

    public void sync()
    {
        String accessToken;
        if(getCurrentUser()==null)
        {
            Log.e(TAG,"No user signed In. No sync possible");
            return;
        }
        if((accessToken=JSCloudUserStore.getAccessToken(mContext))==null)
        {
            Log.e(TAG,"No valid access Token available. No sync possible");
            return;
        }
        mSocket.emit("cloud-sync", accessToken, new Ack() {
            @Override
            public void call(Object... args) {
                try{
                    boolean status=(Boolean)args[0];
                    String message=(String)args[1];
                    if(status)
                    {
                        JSCloudUserStore.saveUser(mContext,(String)args[2]);
                    }
                    Log.e(TAG,message);
                }catch (Exception e)
                {
                    Log.e(TAG,e.getMessage());

                }
            }
        });
    }
}
